package dev.souofrancisco.playertitles.config.section.menu;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable visual item appearance used by configurable menu controls.
 */
public record ItemAppearanceConfig(
        @NotNull Material material,
        @Nullable NamespacedKey itemModel,
        @NotNull String name,
        @NotNull List<@NotNull String> lore
) {

    public ItemAppearanceConfig {
        lore = List.copyOf(lore);
    }
}
