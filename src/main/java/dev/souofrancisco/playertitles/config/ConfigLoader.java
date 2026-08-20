package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.loader.DatabaseConfigLoader;
import dev.souofrancisco.playertitles.config.loader.DebugConfigLoader;
import dev.souofrancisco.playertitles.config.loader.MenuConfigLoader;
import dev.souofrancisco.playertitles.config.loader.MessageConfigLoader;
import dev.souofrancisco.playertitles.config.loader.TitleConfigLoader;
import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.config.section.DebugConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuConfig;
import dev.souofrancisco.playertitles.config.section.message.PluginMessagesConfig;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Orchestrates all plugin YAML files into one validated {@link PluginConfig}.
 *
 * <p>This class stores the currently active configuration. {@link #reload} only replaces that
 * reference after the next config has been fully read and validated. A failed reload leaves the
 * previous configuration unchanged.
 *
 * <p>Text values are kept as raw templates. Player-aware GUI rendering applies PlaceholderAPI where
 * player context is available, then parses MiniMessage and internal PlayerTitles tag resolvers.
 * The {@code %playertitles_title%} expansion renders the selected prefix with MiniMessage only.
 */
@UtilityClass
public final class ConfigLoader {

    private static final String CONFIG_FILE = "config.yml";
    private static final String TITLES_FILE = "titles.yml";
    private static final String MENU_FILE = "menu.yml";

    private static volatile PluginConfig current;

    /**
     * Returns the currently active validated configuration.
     *
     * @return active plugin configuration
     * @throws IllegalStateException when configuration has not been loaded
     */
    public static @NotNull PluginConfig current() {
        PluginConfig config = current;
        if (config == null)
            throw new IllegalStateException("Plugin configuration has not been loaded.");

        return config;
    }

    /**
     * Ensures default YAML files exist, then reads and activates a validated configuration.
     *
     * @param plugin plugin whose data folder contains the YAML files
     * @return validated immutable configuration
     * @throws IllegalArgumentException when the YAML shape or values are invalid
     */
    public static @NotNull PluginConfig load(@NotNull JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        saveDefaultResource(plugin, TITLES_FILE);
        saveDefaultResource(plugin, MENU_FILE);

        PluginConfig next = read(plugin);
        current = next;
        return next;
    }

    /**
     * Rereads the existing YAML files and replaces the active configuration only after complete
     * validation.
     *
     * @param plugin plugin whose data folder contains the YAML files
     * @return the newly activated configuration
     * @throws IllegalArgumentException when the YAML shape or values are invalid
     * @throws IllegalStateException when configuration has not been loaded
     */
    public static @NotNull PluginConfig reload(@NotNull JavaPlugin plugin) {
        if (current == null)
            throw new IllegalStateException("Plugin configuration has not been loaded.");

        PluginConfig next = read(plugin);
        current = next;
        return next;
    }

    private static @NotNull PluginConfig read(@NotNull JavaPlugin plugin) {
        return parse(
                loadYaml(plugin, CONFIG_FILE),
                loadYaml(plugin, TITLES_FILE),
                loadYaml(plugin, MENU_FILE)
        );
    }

    private static @NotNull PluginConfig parse(
            @NotNull ConfigurationSection configRoot,
            @NotNull ConfigurationSection titlesRoot,
            @NotNull ConfigurationSection menuRoot
    ) {
        DatabaseConfig database = DatabaseConfigLoader.load(new ConfigReader(configRoot));
        Map<String, TitleConfig> titles = TitleConfigLoader.load(new ConfigReader(titlesRoot));
        MenuConfig menu = MenuConfigLoader.load(new ConfigReader(menuRoot));
        PluginMessagesConfig messages = MessageConfigLoader.load(new ConfigReader(configRoot));
        DebugConfig debug = DebugConfigLoader.load(new ConfigReader(configRoot));

        return new PluginConfig(database, titles, menu, messages, debug);
    }

    private static void saveDefaultResource(
            @NotNull JavaPlugin plugin,
            @NotNull String resourcePath
    ) {
        File target = new File(plugin.getDataFolder(), resourcePath);
        if (target.exists()) return;

        plugin.saveResource(resourcePath, false);
    }

    private static @NotNull YamlConfiguration loadYaml(
            @NotNull JavaPlugin plugin,
            @NotNull String fileName
    ) {
        File file = new File(plugin.getDataFolder(), fileName);
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
            return configuration;
        } catch (IOException | InvalidConfigurationException exception) {
            throw new IllegalArgumentException("Could not load " + fileName + ".", exception);
        }
    }
}
