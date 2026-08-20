package dev.souofrancisco.playertitles.config.section.menu.impl;

import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuItemConfig;
import org.jetbrains.annotations.NotNull;

public record StaticMenuItemConfig(
        @NotNull ItemAppearanceConfig appearance
) implements MenuItemConfig {
}
