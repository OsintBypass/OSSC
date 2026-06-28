/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
//This file is written 100% without ai
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class EtherwarpHelper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgHUD = settings.createGroup("HUD");

    // Chat message delay in ticks (20 ticks = 1 second)
    private final Setting<Integer> messageDelay = sgGeneral.add(new IntSetting.Builder()
        .name("message-delay")
        .description("Delay between chat messages in ticks (20 ticks = 1 second).")
        .defaultValue(20)
        .min(0)
        .build()
    );

    // HUD settings
    private final Setting<Boolean> showHUD = sgHUD.add(new BoolSetting.Builder()
        .name("show-hud")
        .description("Show HUD indicator for etherwarp status.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showHUDBackground = sgHUD.add(new BoolSetting.Builder()
        .name("hud-background")
        .description("Show background behind HUD text.")
        .defaultValue(true)
        .build()
    );

    private int tickCounter = 0;
    private boolean wasHoldingEtherwarpable = false;
    private boolean lastStatus = false;

    public EtherwarpHelper() {
        super(Categories.Developer, "EtherwarpHelper", "Simple etherwarp helper, just like any other");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        //gets the users hands itemname
        String displayName = mc.player.getMainHandItem().getDisplayName().getString().toLowerCase();
        //checks if it's one of these
        boolean isHoldingEtherwarpable = displayName.contains("aspect of the end") ||
            displayName.contains("aspect of the void") ||
            displayName.contains("etherwarp conduit");

        // Update HUD status
        lastStatus = isHoldingEtherwarpable;

        // Only send chat message if status changed or on interval
        if (isHoldingEtherwarpable != wasHoldingEtherwarpable) {
            wasHoldingEtherwarpable = isHoldingEtherwarpable;
            tickCounter = 0;
        }

        tickCounter++;

        // Send message based on delay setting
        if (tickCounter >= messageDelay.get() && wasHoldingEtherwarpable) {
            tickCounter = 0;
            //sends a message in the debug chat message
            ChatUtils.sendMsg(Component.literal("[DEBUG] You are holding an etherwarpable tool.")
                .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || !showHUD.get() || !isActive()) return;

        String displayName = mc.player.getMainHandItem().getDisplayName().getString().toLowerCase();
        boolean isHoldingEtherwarpable = displayName.contains("aspect of the end") ||
            displayName.contains("aspect of the void") ||
            displayName.contains("etherwarp conduit");

        String text;
        int color;
        if (isHoldingEtherwarpable) {
            text = "Holding etherwarpable";
            color = 0xFF800080; // Purple
        } else {
            text = "Not holding etherwarpable";
            color = 0xFFFF0000; // Red
        }

        // Calculate text width and position
        int textWidth = mc.font.width(text);
        int x = 10;
        int y = 10;

        // Draw background if enabled
        if (showHUDBackground.get()) {
            int padding = 4;
            int bgWidth = textWidth + padding * 2;
            int bgHeight = mc.font.lineHeight + padding * 2;

            // Semi-transparent black background
            event.graphics.fill(x - padding, y - padding/2, x + bgWidth, y + bgHeight, 0x80000000);
        }

        // Draw text
        event.graphics.text(mc.font, text, x, y, color, false);
    }
}
