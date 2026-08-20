package dev.souofrancisco.playertitles.config.loader;

import dev.souofrancisco.playertitles.config.ConfigReader;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.TitleIconConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Loads and validates the configured title entries and their icon definitions.
 */
@UtilityClass
public final class TitleConfigLoader {

    private static final Pattern TITLE_ID_PATTERN = Pattern.compile("[a-z0-9_-]+");

    public static @NotNull Map<@NotNull String, @NotNull TitleConfig> load(@NotNull ConfigReader root) {
        ConfigReader titlesReader = root.requireSection("titles");
        Map<String, TitleConfig> titles = new LinkedHashMap<>();

        for (String titleId : titlesReader.keys()) {
            validateTitleId(titleId, titlesReader);

            ConfigReader titleReader = titlesReader.requireSection(titleId);
            TitleConfig title = loadTitle(titleId, titleReader);
            titles.put(titleId, title);
        }

        return titles;
    }

    private static @NotNull TitleConfig loadTitle(
            @NotNull String titleId,
            @NotNull ConfigReader titleReader
    ) {
        String displayName = titleReader.requireString("display-name");
        String prefix = titleReader.requireString("prefix");
        TitleIconConfig icon = loadIcon(titleReader.requireSection("icon"));

        return new TitleConfig(titleId, displayName, prefix, icon);
    }

    private static @NotNull TitleIconConfig loadIcon(@NotNull ConfigReader iconReader) {
        String materialName = iconReader.requireString("material");
        Material material = Material.matchMaterial(materialName);
        if (material == null)
            throw iconReader.invalid("material", "invalid material '" + materialName + "'");

        NamespacedKey itemModel = optionalItemModel(iconReader);
        String name = iconReader.requireString("name");
        List<String> lore = iconReader.requireStringList("lore");

        return new TitleIconConfig(material, itemModel, name, lore);
    }

    private static void validateTitleId(
            @NotNull String titleId,
            @NotNull ConfigReader titlesReader
    ) {
        if (titleId.isBlank()) throw titlesReader.invalid(titleId, "title id must not be blank");

        if (!TITLE_ID_PATTERN.matcher(titleId).matches())
            throw titlesReader.invalid(titleId, "title id must use only lowercase letters, numbers, underscores, or hyphens");
    }

    private static @Nullable NamespacedKey optionalItemModel(@NotNull ConfigReader iconReader) {
        String raw = iconReader.optionalString("item-model");
        if (raw == null) return null;

        NamespacedKey itemModel = NamespacedKey.fromString(raw);
        if (itemModel == null)
            throw iconReader.invalid("item-model", "invalid namespaced key '" + raw + "'");

        return itemModel;
    }
}
