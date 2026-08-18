package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.TitleIconConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Loads and validates PlayerTitles YAML, keeping Bukkit configuration APIs out of runtime models.
 */
@UtilityClass
public final class ConfigLoader {

    private static final Pattern TITLE_ID_PATTERN = Pattern.compile("[a-z0-9_-]+");

    /**
     * Reads the plugin's active config file into immutable typed records.
     *
     * <p>Text values are kept as raw templates. Future rendering should first apply PlaceholderAPI
     * where player context is available, then parse the result with MiniMessage. The literal
     * {@code <status>} token in lore is reserved for a future internal MiniMessage tag supplied by
     * a TagResolver.
     *
     * @param plugin plugin whose Bukkit configuration should be loaded
     * @return validated immutable configuration
     * @throws IllegalArgumentException when the YAML shape or values are invalid
     */
    public static @NotNull PluginConfig load(@NotNull JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        return load(plugin.getConfig());
    }

    /**
     * Converts a Bukkit configuration section into the typed root config.
     *
     * <p>This overload keeps direct YAML API usage inside this loader class.
     *
     * @param root root Bukkit configuration section
     * @return validated immutable configuration
     */
    public static @NotNull PluginConfig load(@NotNull ConfigurationSection root) {
        ConfigurationSection titlesSection = requireSection(root, "titles");
        Map<String, TitleConfig> titles = new LinkedHashMap<>();

        for (String titleId : titlesSection.getKeys(false)) {
            String path = "titles." + titleId;
            validateTitleId(titleId, path);

            ConfigurationSection titleSection = requireSection(titlesSection, titleId);
            TitleConfig title = loadTitle(titleId, titleSection, path);
            TitleConfig previous = titles.put(titleId, title);
            if (previous != null)
                throw invalid(path, "duplicate title id '" + titleId + "'");
        }

        return new PluginConfig(titles);
    }

    private static @NotNull TitleConfig loadTitle(
            @NotNull String titleId,
            @NotNull ConfigurationSection section,
            @NotNull String path
    ) {
        String displayName = requireString(section, "display-name", path);
        String prefix = requireString(section, "prefix", path);
        ConfigurationSection iconSection = requireSection(section, "icon");
        TitleIconConfig icon = loadIcon(iconSection, path + ".icon");

        return new TitleConfig(titleId, displayName, prefix, icon);
    }

    private static @NotNull TitleIconConfig loadIcon(
            @NotNull ConfigurationSection section,
            @NotNull String path
    ) {
        String materialName = requireString(section, "material", path);
        Material material = Material.matchMaterial(materialName);
        if (material == null)
            throw invalid(path + ".material", "invalid material '" + materialName + "'");

        NamespacedKey itemModel = optionalItemModel(section, path);
        String name = requireString(section, "name", path);
        List<String> lore = requireStringList(section, "lore", path);

        return new TitleIconConfig(material, itemModel, name, lore);
    }

    private static void validateTitleId(@NotNull String titleId, @NotNull String path) {
        if (titleId.isBlank()) throw invalid(path, "title id must not be blank");

        if (!TITLE_ID_PATTERN.matcher(titleId).matches())
            throw invalid(path, "title id must use only lowercase letters, numbers, underscores, or hyphens");
    }

    private static @NotNull ConfigurationSection requireSection(
            @NotNull ConfigurationSection section,
            @NotNull String key
    ) {
        String path = childPath(section, key);
        if (!section.isConfigurationSection(key))
            throw invalid(path, "missing or malformed section");


        ConfigurationSection child = section.getConfigurationSection(key);
        if (child == null)
            throw invalid(path, "missing or malformed section");

        return child;
    }

    private static @NotNull String requireString(
            @NotNull ConfigurationSection section,
            @NotNull String key,
            @NotNull String parentPath
    ) {
        String path = parentPath + "." + key;
        if (!section.isString(key))
            throw invalid(path, "missing required text value");


        String value = section.getString(key);
        if (value == null || value.isBlank())
            throw invalid(path, "text value must not be blank");

        return value;
    }

    private static @Nullable NamespacedKey optionalItemModel(
            @NotNull ConfigurationSection section,
            @NotNull String path
    ) {
        if (!section.isSet("item-model"))
            return null;

        if (!section.isString("item-model"))
            throw invalid(path + ".item-model", "item-model must be a namespaced key string");

        String raw = section.getString("item-model");
        if (raw == null || raw.isBlank())
            throw invalid(path + ".item-model", "item-model must not be blank");

        NamespacedKey itemModel = NamespacedKey.fromString(raw);
        if (itemModel == null)
            throw invalid(path + ".item-model", "invalid namespaced key '" + raw + "'");

        return itemModel;
    }

    private static @NotNull List<@NotNull String> requireStringList(
            @NotNull ConfigurationSection section,
            @NotNull String key,
            @NotNull String parentPath
    ) {
        String path = parentPath + "." + key;
        if (!section.isList(key))
            throw invalid(path, "missing required string list");

        List<?> rawLore = section.getList(key);
        if (rawLore == null)
            throw invalid(path, "missing required string list");

        for (int index = 0; index < rawLore.size(); index++) {
            Object line = rawLore.get(index);
            if (!(line instanceof String))
                throw invalid(path + "[" + index + "]", "lore entries must be strings");
        }

        return section.getStringList(key);
    }

    private static @NotNull String childPath(
            @NotNull ConfigurationSection section,
            @NotNull String key
    ) {
        String currentPath = section.getCurrentPath();
        return currentPath == null || currentPath.isBlank() ? key : currentPath + "." + key;
    }

    private static @NotNull IllegalArgumentException invalid(
            @NotNull String path,
            @Nullable String reason
    ) {
        String message = reason == null || reason.isBlank()
                ? "Invalid configuration at '" + path + "'."
                : "Invalid configuration at '" + path + "': " + reason + ".";
        return new IllegalArgumentException(message);
    }
}
