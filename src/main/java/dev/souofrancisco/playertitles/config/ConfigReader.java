package dev.souofrancisco.playertitles.config;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Path-aware wrapper around Bukkit YAML sections for reusable typed access and validation errors.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigReader {

    private final @NotNull ConfigurationSection section;
    private final @NotNull String path;

    public ConfigReader(@NotNull ConfigurationSection section) {
        this(section, rootPath(section));
    }

    public boolean isSet(@NotNull String key) {
        return section.isSet(key);
    }

    public @NotNull Iterable<@NotNull String> keys() {
        return section.getKeys(false);
    }

    public @NotNull ConfigReader requireSection(@NotNull String key) {
        if (!section.isConfigurationSection(key))
            throw invalid(key, "missing or malformed section");

        ConfigurationSection child = section.getConfigurationSection(key);
        if (child == null)
            throw invalid(key, "missing or malformed section");

        return new ConfigReader(child, childPath(key));
    }

    public @Nullable ConfigReader optionalSection(@NotNull String key) {
        if (!section.isSet(key)) return null;
        return requireSection(key);
    }

    public @NotNull String requireString(@NotNull String key) {
        if (!section.isString(key))
            throw invalid(key, "missing required text value");

        String value = section.getString(key);
        if (value == null || value.isBlank())
            throw invalid(key, "text value must not be blank");

        return value;
    }

    public @Nullable String optionalString(@NotNull String key) {
        if (!section.isSet(key)) return null;
        if (!section.isString(key))
            throw invalid(key, "must be a text value");

        String value = section.getString(key);
        if (value == null || value.isBlank())
            throw invalid(key, "text value must not be blank");

        return value;
    }

    public @NotNull String optionalString(@NotNull String key, @NotNull String fallback) {
        return optionalString(key, fallback, false);
    }

    public @NotNull String optionalStringIn(
            @NotNull String sectionKey,
            @NotNull String key,
            @NotNull String fallback
    ) {
        return optionalStringIn(sectionKey, key, fallback, false);
    }

    public @NotNull String optionalStringIn(
            @NotNull String sectionKey,
            @NotNull String key,
            @NotNull String fallback,
            boolean allowBlank
    ) {
        ConfigReader child = optionalSection(sectionKey);
        return child == null ? fallback : child.optionalString(key, fallback, allowBlank);
    }

    public @NotNull String optionalString(
            @NotNull String key,
            @NotNull String fallback,
            boolean allowBlank
    ) {
        if (!section.isSet(key)) return fallback;
        if (!section.isString(key))
            throw invalid(key, "must be a text value");

        String value = section.getString(key);
        if (value == null || (!allowBlank && value.isBlank()))
            throw invalid(key, "text value must not be blank");

        return value;
    }

    public int optionalInt(@NotNull String key, int fallback) {
        if (!section.isSet(key)) return fallback;
        if (!section.isInt(key))
            throw invalid(key, "must be an integer");

        return section.getInt(key);
    }

    public int optionalInt(@NotNull String key, int fallback, int minimum, int maximum) {
        int value = optionalInt(key, fallback);
        if (value < minimum || value > maximum)
            throw invalid(key, "must be between " + minimum + " and " + maximum);

        return value;
    }

    public int optionalIntIn(
            @NotNull String sectionKey,
            @NotNull String key,
            int fallback,
            int minimum,
            int maximum
    ) {
        ConfigReader child = optionalSection(sectionKey);
        return child == null ? fallback : child.optionalInt(key, fallback, minimum, maximum);
    }

    public @NotNull List<@NotNull String> requireStringList(@NotNull String key) {
        if (!section.isList(key))
            throw invalid(key, "missing required string list");

        List<?> rawValues = section.getList(key);
        if (rawValues == null)
            throw invalid(key, "missing required string list");

        for (int index = 0; index < rawValues.size(); index++) {
            Object value = rawValues.get(index);
            if (!(value instanceof String))
                throw invalidAt(childPath(key) + "[" + index + "]", "entries must be strings");
        }

        return section.getStringList(key);
    }

    public @NotNull IllegalArgumentException invalid(@NotNull String key, @NotNull String reason) {
        return invalidAt(childPath(key), reason);
    }

    private @NotNull String childPath(@NotNull String key) {
        return path.isBlank() ? key : path + "." + key;
    }

    private static @NotNull String rootPath(@NotNull ConfigurationSection section) {
        String currentPath = section.getCurrentPath();
        return currentPath == null ? "" : currentPath;
    }

    private static @NotNull IllegalArgumentException invalidAt(
            @NotNull String path,
            @Nullable String reason
    ) {
        String message = reason == null || reason.isBlank()
                ? "Invalid configuration at '" + path + "'."
                : "Invalid configuration at '" + path + "': " + reason + ".";
        return new IllegalArgumentException(message);
    }
}
