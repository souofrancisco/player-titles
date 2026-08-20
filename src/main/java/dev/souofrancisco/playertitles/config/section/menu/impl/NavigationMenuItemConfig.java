package dev.souofrancisco.playertitles.config.section.menu.impl;

import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.type.NavigationDirection;
import org.jetbrains.annotations.NotNull;

public record NavigationMenuItemConfig(
        @NotNull NavigationDirection direction,
        @NotNull ItemAppearanceConfig available,
        @NotNull ItemAppearanceConfig unavailable
) implements MenuItemConfig {
}
