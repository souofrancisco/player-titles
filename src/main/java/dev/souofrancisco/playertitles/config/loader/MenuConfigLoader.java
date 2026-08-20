package dev.souofrancisco.playertitles.config.loader;

import dev.souofrancisco.playertitles.config.ConfigReader;
import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.TitleStatusLoreConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.NavigationMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.SelectedTitleMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.StaticMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.type.NavigationDirection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Loads and validates the configurable PlayerTitles inventory menu.
 */
@UtilityClass
public final class MenuConfigLoader {

    private static final char EMPTY_SLOT = 'X';
    private static final int INVENTORY_WIDTH = 9;
    private static final int MIN_ROWS = 1;
    private static final int MAX_ROWS = 6;

    public static @NotNull MenuConfig load(@NotNull ConfigReader menuReader) {
        String title = menuReader.requireString("title");
        char titleSlot = loadSymbol(menuReader, "title-slot");
        if (titleSlot == EMPTY_SLOT)
            throw menuReader.invalid("title-slot", "X is reserved for empty inventory slots");

        List<String> layout = menuReader.requireStringList("layout");
        validateLayoutShape(menuReader, layout);

        ConfigReader itemsReader = menuReader.requireSection("items");
        Map<Character, MenuItemConfig> items = loadItems(itemsReader, titleSlot);
        validateLayoutSymbols(menuReader, layout, titleSlot, items);
        TitleStatusLoreConfig titleStatus = loadTitleStatus(menuReader.requireSection("title-status"));

        return new MenuConfig(title, titleSlot, layout, items, titleStatus);
    }

    private static @NotNull Map<Character, MenuItemConfig> loadItems(
            @NotNull ConfigReader itemsReader,
            char titleSlot
    ) {
        Map<Character, MenuItemConfig> items = new LinkedHashMap<>();
        for (String key : itemsReader.keys()) {
            if (key.length() != 1)
                throw itemsReader.invalid(key, "menu item keys must be exactly one character");

            char symbol = key.charAt(0);
            if (symbol == EMPTY_SLOT)
                throw itemsReader.invalid(key, "X is reserved for empty inventory slots");

            if (symbol == titleSlot)
                throw itemsReader.invalid(key, "symbol is already used by menu.title-slot");

            ConfigReader itemReader = itemsReader.requireSection(key);
            items.put(symbol, loadItem(itemReader));
        }

        return items;
    }

    private static @NotNull MenuItemConfig loadItem(@NotNull ConfigReader itemReader) {
        String type = itemReader.requireString("type");
        return switch (type) {
            case "static" -> new StaticMenuItemConfig(loadAppearance(itemReader));
            case "previous-page" -> new NavigationMenuItemConfig(
                    NavigationDirection.PREVIOUS,
                    loadAppearance(itemReader.requireSection("available")),
                    loadAppearance(itemReader.requireSection("unavailable"))
            );
            case "next-page" -> new NavigationMenuItemConfig(
                    NavigationDirection.NEXT,
                    loadAppearance(itemReader.requireSection("available")),
                    loadAppearance(itemReader.requireSection("unavailable"))
            );
            case "selected-title" -> new SelectedTitleMenuItemConfig(
                    loadAppearance(itemReader.requireSection("selected")),
                    loadAppearance(itemReader.requireSection("none"))
            );
            default -> throw itemReader.invalid("type", "unknown menu item type '" + type + "'");
        };
    }

    private static @NotNull ItemAppearanceConfig loadAppearance(@NotNull ConfigReader reader) {
        String materialName = reader.requireString("material");
        Material material = Material.matchMaterial(materialName);
        if (material == null)
            throw reader.invalid("material", "invalid material '" + materialName + "'");

        return new ItemAppearanceConfig(
                material,
                optionalItemModel(reader),
                requireString(reader, "name", true),
                reader.requireStringList("lore")
        );
    }

    private static @NotNull String requireString(
            @NotNull ConfigReader reader,
            @NotNull String key,
            boolean allowBlank
    ) {
        if (!reader.isSet(key))
            throw reader.invalid(key, "missing required text value");

        return reader.optionalString(key, "", allowBlank);
    }

    private static @NotNull TitleStatusLoreConfig loadTitleStatus(@NotNull ConfigReader reader) {
        return new TitleStatusLoreConfig(
                reader.requireStringList("locked"),
                reader.requireStringList("unlocked"),
                reader.requireStringList("selected")
        );
    }

    private static char loadSymbol(@NotNull ConfigReader reader, @NotNull String key) {
        String symbol = reader.requireString(key);
        if (symbol.length() != 1)
            throw reader.invalid(key, "must be exactly one character");

        return symbol.charAt(0);
    }

    private static void validateLayoutShape(
            @NotNull ConfigReader menuReader,
            @NotNull List<String> layout
    ) {
        if (layout.size() < MIN_ROWS || layout.size() > MAX_ROWS)
            throw menuReader.invalid("layout", "must contain between 1 and 6 rows");

        for (int index = 0; index < layout.size(); index++) {
            if (layout.get(index).length() != INVENTORY_WIDTH)
                throw menuReader.invalid(
                        "layout[" + index + "]",
                        "row must contain exactly 9 characters"
                );
        }
    }

    private static void validateLayoutSymbols(
            @NotNull ConfigReader menuReader,
            @NotNull List<String> layout,
            char titleSlot,
            @NotNull Map<Character, MenuItemConfig> items
    ) {
        boolean hasTitleSlot = false;
        for (int row = 0; row < layout.size(); row++) {
            String value = layout.get(row);
            for (int column = 0; column < value.length(); column++) {
                char symbol = value.charAt(column);
                if (symbol == EMPTY_SLOT) continue;
                if (symbol == titleSlot) {
                    hasTitleSlot = true;
                    continue;
                }

                if (!items.containsKey(symbol))
                    throw menuReader.invalid(
                            "layout[" + row + "]",
                            "unknown symbol '" + symbol + "' at column " + column
                    );
            }
        }

        if (!hasTitleSlot)
            throw menuReader.invalid("layout", "must contain at least one title slot '" + titleSlot + "'");
    }

    private static @Nullable NamespacedKey optionalItemModel(@NotNull ConfigReader reader) {
        String raw = reader.optionalString("item-model");
        if (raw == null) return null;

        NamespacedKey itemModel = NamespacedKey.fromString(raw);
        if (itemModel == null)
            throw reader.invalid("item-model", "invalid namespaced key '" + raw + "'");

        return itemModel;
    }
}
