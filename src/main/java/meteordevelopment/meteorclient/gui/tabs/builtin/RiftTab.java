/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.gui.screens.Screen;

public class RiftTab extends Tab {
    public RiftTab() {
        super("Rift");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new RiftScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof RiftScreen;
    }

    private static class RiftScreen extends WindowTabScreen {
        private final Module placeholder = new RiftPlaceholderModule();

        public RiftScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            add(theme.label("Rift Placeholder Module")).expandX();
            add(theme.module(placeholder)).expandX();

            WVerticalList info = add(theme.verticalList()).expandX().widget();
            info.add(theme.label("This tab is a placeholder for Rift content.")).expandX();
            info.add(theme.label("Use this area for example module layout or future Rift UI."));
        }
    }

    private static class RiftPlaceholderModule extends Module {
        public RiftPlaceholderModule() {
            super(Categories.Misc, "rift-placeholder", "Example placeholder module for the Rift tab.");
            serialize = false;
            autoSubscribe = false;
            chatFeedback = false;
            favorite = false;
        }
    }
}
