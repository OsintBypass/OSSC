/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import com.mojang.blaze3d.platform.NativeImage;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.CapesModule;

public class Capes {
    private static final String CAPE_OWNERS_URL = "https://meteorclient.com/api/capeowners";
    private static final String CAPES_URL = "https://meteorclient.com/api/capes";
    private static final Path LOCAL_CAPES_DIR = MeteorClient.FOLDER.toPath().resolve("capes");

    private static final Map<UUID, String> OWNERS = new HashMap<>();
    private static final Map<String, String> URLS = new HashMap<>();
    private static final Map<String, Cape> TEXTURES = new HashMap<>();
    private static final Set<String> LOCAL_CAPES = new HashSet<>();

    private static String localPathCapeName;
    private static String localPathCapePath;

    private static final List<Cape> TO_REGISTER = new ArrayList<>();
    private static final List<Cape> TO_RETRY = new ArrayList<>();
    private static final List<Cape> TO_REMOVE = new ArrayList<>();

    private Capes() {
    }

    @PreInit(dependencies = MeteorExecutor.class)
    public static void init() {
        OWNERS.clear();
        URLS.clear();
        TEXTURES.clear();
        LOCAL_CAPES.clear();
        TO_REGISTER.clear();
        TO_RETRY.clear();
        TO_REMOVE.clear();

        // Load local capes
        loadLocalCapes();

        MeteorExecutor.execute(() -> {
            // Cape owners
            MeteorClient.LOG.info("Refreshing cape owners and URLs.");
            Stream<String> lines = Http.get(CAPE_OWNERS_URL)
                .exceptionHandler(e -> MeteorClient.LOG.error("Could not load capes: {}", e.getMessage()))
                .sendLines();
            if (lines != null) {
                lines.forEach(s -> {
                    String[] split = s.split(" ");

                    if (split.length >= 2) {
                        OWNERS.put(UUID.fromString(split[0]), split[1]);
                        if (!TEXTURES.containsKey(split[1])) TEXTURES.put(split[1], new Cape(split[1]));
                    }
                });
            } else return;

            // Capes
            lines = Http.get(CAPES_URL).sendLines();
            if (lines != null) lines.forEach(s -> {
                String[] split = s.split(" ");

                if (split.length >= 2) {
                    if (!URLS.containsKey(split[0])) URLS.put(split[0], split[1]);
                }
            });
        });

        if (localPathCapePath != null) {
            addLocalPathCape(localPathCapePath);
        }

        MeteorClient.EVENT_BUS.subscribe(Capes.class);
    }

    private static void loadLocalCapes() {
        File capesDir = LOCAL_CAPES_DIR.toFile();
        if (!capesDir.exists()) {
            capesDir.mkdirs();
            return;
        }

        File[] capeFiles = capesDir.listFiles((dir, name) -> 
            name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));

        if (capeFiles != null) {
            for (File file : capeFiles) {
                String capeName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                LOCAL_CAPES.add(capeName);
                if (!TEXTURES.containsKey(capeName)) {
                    MeteorClient.LOG.info("Loading local cape from folder: {}", file.getAbsolutePath());
                    TEXTURES.put(capeName, new Cape(capeName, file.getAbsolutePath(), true));
                }
            }
        }
    }

    public static Set<String> getAvailableCapes() {
        return TEXTURES.keySet();
    }

    public static boolean isLocalCape(String capeName) {
        return LOCAL_CAPES.contains(capeName) || Objects.equals(capeName, localPathCapeName);
    }

    public static void logDebugStatus() {
        MeteorClient.LOG.info("Capes status: owners={}, urls={}, textures={}, localFolder={}, localPath={}", OWNERS.size(), URLS.size(), TEXTURES.size(), LOCAL_CAPES.size(), localPathCapePath == null ? "none" : localPathCapePath);
        if (localPathCapeName != null) MeteorClient.LOG.info("Current local path cape name: {}", localPathCapeName);
    }

    @EventHandler
    private static void onTick(TickEvent.Post event) {
        synchronized (TO_REGISTER) {
            for (Cape cape : TO_REGISTER) cape.register();
            TO_REGISTER.clear();
        }

        synchronized (TO_RETRY) {
            TO_RETRY.removeIf(Cape::tick);
        }

        synchronized (TO_REMOVE) {
            for (Cape cape : TO_REMOVE) {
                URLS.remove(cape.name);
                TEXTURES.remove(cape.name);
                TO_REGISTER.remove(cape);
                TO_RETRY.remove(cape);
            }

            TO_REMOVE.clear();
        }
    }

    public static Identifier get(Player player) {
        // Respect the Capes module toggle
        if (!Modules.get().isActive(CapesModule.class)) return null;

        if (localPathCapeName != null && player == mc.player) {
            Cape cape = TEXTURES.get(localPathCapeName);
            if (cape == null) return null;
            if (cape.isDownloaded()) return cape.getIdentifier();
            cape.download();
            return null;
        }

        String capeName = OWNERS.get(player.getUUID());
        if (capeName != null) {
            Cape cape = TEXTURES.get(capeName);
            if (cape == null) return null;

            if (cape.isDownloaded()) return cape.getIdentifier();

            cape.download();
            return null;
        }

        return null;
    }

    /**
     * Adds a local cape from an absolute file path. The cape name will be the file name without extension.
     */
    public static void addLocalCape(String filePath) {
        if (filePath == null || filePath.isBlank()) return;

        File f = new File(filePath);
        if (!f.exists() || f.isDirectory()) return;

        String capeName = f.getName();
        int idx = capeName.lastIndexOf('.');
        if (idx > 0) capeName = capeName.substring(0, idx);

        LOCAL_CAPES.add(capeName);
        if (!TEXTURES.containsKey(capeName)) {
            MeteorClient.LOG.info("Adding local cape '{}': {}", capeName, f.getAbsolutePath());
            TEXTURES.put(capeName, new Cape(capeName, f.getAbsolutePath(), true));
        }
    }

    public static void addLocalPathCape(String filePath) {
        if (filePath == null || filePath.isBlank()) return;

        File f = new File(filePath);
        if (!f.exists() || f.isDirectory()) {
            MeteorClient.LOG.error("Local cape path does not exist or is a directory: {}", filePath);
            return;
        }

        String capeName = "local-path-cape";
        localPathCapeName = capeName;
        localPathCapePath = f.getAbsolutePath();

        MeteorClient.LOG.info("Adding custom local path cape: {}", localPathCapePath);
        TEXTURES.put(capeName, new Cape(capeName, f.getAbsolutePath(), true));
    }

    /**
     * Removes a local cape by name (file name without extension).
     */
    public static void removeLocalCape(String capeName) {
        if (capeName == null) return;

        Cape cape = TEXTURES.get(capeName);
        if (cape != null) {
            synchronized (TO_REMOVE) {
                TO_REMOVE.add(cape);
            }
        }

        LOCAL_CAPES.remove(capeName);
        if (Objects.equals(capeName, localPathCapeName)) {
            localPathCapeName = null;
            localPathCapePath = null;
        }
    }

    public static void removeLocalPathCape() {
        if (localPathCapeName == null) return;
        removeLocalCape(localPathCapeName);
    }

    private static class Cape {
        private static int COUNT = 0;

        private final String name;
        private final Identifier identifier;
        private final String localFilePath;
        private final boolean isLocal;

        private boolean downloaded;
        private boolean downloading;

        private NativeImage img;

        private int retryTimer;

        public Cape(String name) {
            this(name, null, false);
        }

        public Cape(String name, String localFilePath, boolean isLocal) {
            this.identifier = MeteorClient.identifier("capes/" + COUNT++);
            this.name = name;
            this.localFilePath = localFilePath;
            this.isLocal = isLocal;
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public void download() {
            if (downloaded || downloading || retryTimer > 0) return;
            downloading = true;

            MeteorExecutor.execute(() -> {
                try {
                    InputStream in;

                    if (isLocal) {
                        // Load from local file
                        if (localFilePath == null) {
                            synchronized (TO_REMOVE) {
                                TO_REMOVE.add(this);
                                downloading = false;
                                return;
                            }
                        }
                        in = Files.newInputStream(Path.of(localFilePath));
                    } else {
                        // Load from online URL
                        String url = URLS.get(name);
                        if (url == null) {
                            synchronized (TO_REMOVE) {
                                TO_REMOVE.add(this);
                                downloading = false;
                                return;
                            }
                        }
                        in = Http.get(url).sendInputStream();
                    }

                    if (in == null) {
                        synchronized (TO_RETRY) {
                            TO_RETRY.add(this);
                            retryTimer = 10 * 20;
                            downloading = false;
                            return;
                        }
                    }

                    img = NativeImage.read(in);

                    synchronized (TO_REGISTER) {
                        TO_REGISTER.add(this);
                    }
                } catch (IOException e) {
                    MeteorClient.LOG.error("Failed to load cape '{}'", name, e);
                }
            });
        }

        public void register() {
            mc.getTextureManager().register(identifier, new DynamicTexture(null, img));
            img = null;

            downloading = false;
            downloaded = true;
        }

        public boolean tick() {
            if (retryTimer > 0) {
                retryTimer--;
            } else {
                download();
                return true;
            }

            return false;
        }

        public boolean isDownloaded() {
            return downloaded;
        }
    }
}
