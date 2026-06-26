/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.network.Capes;

import java.io.File;

public class CapesModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private String currentCapeName;

    public final Setting<String> localPath = sgGeneral.add(new StringSetting.Builder()
        .name("local-path")
        .description("Absolute path to a local cape PNG/JPG to use.")
        .defaultValue("")
        .onChanged(path -> {
            if (path == null || path.isBlank()) {
                if (currentCapeName != null) {
                    Capes.removeLocalCape(currentCapeName);
                    currentCapeName = null;
                }
            } else {
                File f = new File(path);
                if (f.exists() && !f.isDirectory()) {
                    String name = f.getName();
                    int idx = name.lastIndexOf('.');
                    if (idx > 0) name = name.substring(0, idx);

                    Capes.addLocalCape(f.getAbsolutePath());
                    if (currentCapeName != null && !currentCapeName.equals(name)) {
                        Capes.removeLocalCape(currentCapeName);
                    }
                    currentCapeName = name;
                }
            }
        })
        .wide()
        .build()
    );

    public CapesModule() {
        super(Categories.Developer, "capes", "Shows Meteor capes.");
    }
}
