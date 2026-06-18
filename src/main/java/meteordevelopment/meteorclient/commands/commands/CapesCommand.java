/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.network.Capes;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class CapesCommand extends Command {
    public CapesCommand() {
        super("capes", "Manage Meteor capes.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder
            .executes(context -> {
                info("Use (highlight)%s(default) or (highlight)%s(default).", "/capes reload", "/capes status");
                return SINGLE_SUCCESS;
            })
            .then(literal("reload").executes(context -> {
                info("Reloading capes...");
                Capes.init();
                info("Reloaded capes.");
                return SINGLE_SUCCESS;
            }))
            .then(literal("status").executes(context -> {
                Capes.logDebugStatus();
                return SINGLE_SUCCESS;
            }));
    }
}
