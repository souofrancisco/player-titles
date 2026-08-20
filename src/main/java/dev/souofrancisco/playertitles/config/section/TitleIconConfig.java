package dev.souofrancisco.playertitles.config.section;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable inventory icon configuration for presenting a title in future UI flows.
 *
 * @param material validated Bukkit material used for the icon item
 * @param itemModel optional vanilla {@code minecraft:item_model} component; Nexo and ItemsAdder
 *     custom items are referenced by this namespaced key, not by plugin-specific item IDs
 * @param name raw MiniMessage and PlaceholderAPI template for the icon display name
 * @param lore immutable raw MiniMessage and PlaceholderAPI templates for icon lore lines
 */
public record TitleIconConfig(
        @NotNull Material material,
        @Nullable NamespacedKey itemModel,
        @NotNull String name,
        @NotNull List<@NotNull String> lore
) {

    public TitleIconConfig {
        lore = List.copyOf(lore);
    }
}
