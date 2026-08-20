package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.loader.DatabaseConfigLoader;
import dev.souofrancisco.playertitles.config.loader.MenuConfigLoader;
import dev.souofrancisco.playertitles.config.loader.MessageConfigLoader;
import dev.souofrancisco.playertitles.config.loader.TitleConfigLoader;
import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuConfig;
import dev.souofrancisco.playertitles.config.section.message.PluginMessagesConfig;
import java.io.File;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Root orchestrator for loading the plugin YAML into immutable typed configuration records.
 */
@UtilityClass
public final class ConfigLoader {

    private static final String TITLES_FILE = "titles.yml";
    private static final String MENU_FILE = "menu.yml";

    /**
     * Reads the plugin's active YAML files into immutable typed records.
     *
     * <p>Text values are kept as raw templates. Player-aware rendering applies PlaceholderAPI
     * where player context is available, then parses the result with MiniMessage and internal
     * PlayerTitles tag resolvers.
     *
     * @param plugin plugin whose Bukkit configuration should be loaded
     * @return validated immutable configuration
     * @throws IllegalArgumentException when the YAML shape or values are invalid
     */
    public static @NotNull PluginConfig load(@NotNull JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        saveDefaultResource(plugin, TITLES_FILE);
        saveDefaultResource(plugin, MENU_FILE);
        plugin.reloadConfig();

        return load(
                plugin.getConfig(),
                loadYaml(plugin, TITLES_FILE),
                loadYaml(plugin, MENU_FILE)
        );
    }

    /**
     * Converts Bukkit configuration sections into the typed root config.
     *
     * @param configRoot global/plugin infrastructure configuration
     * @param titlesRoot title definition configuration
     * @param menuRoot menu configuration
     * @return validated immutable configuration
     */
    public static @NotNull PluginConfig load(
            @NotNull ConfigurationSection configRoot,
            @NotNull ConfigurationSection titlesRoot,
            @NotNull ConfigurationSection menuRoot
    ) {
        DatabaseConfig database = DatabaseConfigLoader.load(new ConfigReader(configRoot));
        Map<String, TitleConfig> titles = TitleConfigLoader.load(new ConfigReader(titlesRoot));
        MenuConfig menu = MenuConfigLoader.load(new ConfigReader(menuRoot));
        PluginMessagesConfig messages = MessageConfigLoader.load(new ConfigReader(configRoot));

        return new PluginConfig(database, titles, menu, messages);
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
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), fileName));
    }
}
