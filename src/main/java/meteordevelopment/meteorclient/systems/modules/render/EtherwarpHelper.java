/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
//This file is written 100% without ai
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class EtherwarpHelper extends Module {
    public EtherwarpHelper() {
        super(Categories.Render, "EtherwarpHelper", "Simple etherwarp helper, just like any other");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        //gets the users hands itemname
        String displayName = mc.player.getMainHandItem().getDisplayName().getString().toLowerCase();
        //checks if it's one of these
        if (displayName.contains("aspect of the end") ||
            displayName.contains("aspect of the void") ||
            displayName.contains("etherwarp conduit")) {
            //sends a message in the debug chat message
            ChatUtils.sendMsg(Component.literal("[DEBUG] You are holding an etherwarpable tool.")
                .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
