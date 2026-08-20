package dev.souofrancisco.playertitles.config.section.menu.impl;

import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuItemConfig;
import org.jetbrains.annotations.NotNull;

public record SelectedTitleMenuItemConfig(
        @NotNull ItemAppearanceConfig selected,
        @NotNull ItemAppearanceConfig none
) implements MenuItemConfig {
}
