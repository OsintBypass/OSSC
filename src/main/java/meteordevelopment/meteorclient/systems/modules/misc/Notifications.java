/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCustom = settings.createGroup("Custom Toast");

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Where module notifications are displayed.")
        .defaultValue(Mode.CHAT)
        .build()
    );

    public final Setting<Boolean> moduleUpdates = sgGeneral.add(new BoolSetting.Builder()
        .name("module-updates")
        .description("Show notifications for module toggle updates.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> toastDuration = sgGeneral.add(new IntSetting.Builder()
        .name("toast-duration")
        .description("How long toast notifications are shown for, in milliseconds.")
        .defaultValue(4000)
        .min(0)
        .build()
    );

    public final Setting<Position> toastPosition = sgCustom.add(new EnumSetting.Builder<Position>()
        .name("toast-position")
        .description("Where custom toasts are displayed.")
        .defaultValue(Position.TOP_RIGHT)
        .build()
    );

    private final List<CustomToast> customToasts = new ArrayList<>();

    public Notifications() {
        super(Categories.Misc, "notifications", "Route module notifications to chat or toast.");
    }

    public static Notifications get() {
        return Modules.get().get(Notifications.class);
    }

    public boolean showChat() {
        return mode.get() == Mode.CHAT || mode.get() == Mode.BOTH;
    }

    public boolean showToasts() {
        return mode.get() == Mode.TOAST || mode.get() == Mode.BOTH;
    }

    public void displayToast(String title, String text) {
        if (mc == null || mc.getToastManager() == null) return;
        
        if (toastPosition.get() == Position.TOP_RIGHT) {
            mc.getToastManager().addToast(new MeteorToast.Builder(title).text(text).duration(toastDuration.get()).build());
        } else {
            synchronized (customToasts) {
                customToasts.add(new CustomToast(title, text, System.currentTimeMillis() + toastDuration.get()));
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!isActive()) return;

        synchronized (customToasts) {
            long now = System.currentTimeMillis();
            customToasts.removeIf(toast -> toast.expiresAt <= now);

            for (int i = 0; i < customToasts.size(); i++) {
                renderCustomToast(event.graphics, i);
            }
        }
    }

    private void renderCustomToast(GuiGraphicsExtractor graphics, int index) {
        CustomToast toast = customToasts.get(index);
        
        // Toast dimensions
        int width = 160;
        int height = 32;
        int x, y;
        
        // Calculate position based on setting
        switch (toastPosition.get()) {
            case TOP_RIGHT -> {
                x = mc.getWindow().getGuiScaledWidth() - width - 10;
                y = 10 + (index * (height + 5));
            }
            case BOTTOM_RIGHT -> {
                x = mc.getWindow().getGuiScaledWidth() - width - 10;
                y = mc.getWindow().getGuiScaledHeight() - 10 - (index + 1) * (height + 5);
            }
            case TOP_LEFT -> {
                x = 10;
                y = 10 + (index * (height + 5));
            }
            case BOTTOM_LEFT -> {
                x = 10;
                y = mc.getWindow().getGuiScaledHeight() - 10 - (index + 1) * (height + 5);
            }
            default -> { return; }
        }

        // Draw background
        graphics.fill(x, y, x + width, y + height, 0xFF1a1a1a);
        
        // Draw border
        graphics.fill(x, y, x + width, y + 1, 0xFF9145E2);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF9145E2);
        graphics.fill(x, y, x + 1, y + height, 0xFF9145E2);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF9145E2);

        // Draw title
        graphics.text(mc.font, toast.title, x + 8, y + 8, 0xFF9145E2, false);
        
        // Draw text if present
        if (toast.text != null && !toast.text.isEmpty()) {
            graphics.text(mc.font, toast.text, x + 8, y + 19, 0xFFDCDCDC, false);
        }
    }

    private static class CustomToast {
        final String title;
        final String text;
        final long expiresAt;

        CustomToast(String title, String text, long expiresAt) {
            this.title = title;
            this.text = text;
            this.expiresAt = expiresAt;
        }
    }

    public enum Mode {
        CHAT,
        TOAST,
        BOTH,
        NONE
    }

    public enum Position {
        TOP_RIGHT,
        BOTTOM_RIGHT,
        TOP_LEFT,
        BOTTOM_LEFT
    }
}
