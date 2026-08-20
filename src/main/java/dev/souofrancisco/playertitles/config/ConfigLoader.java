package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.loader.DatabaseConfigLoader;
import dev.souofrancisco.playertitles.config.loader.TitleConfigLoader;
import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Root orchestrator for loading the plugin YAML into immutable typed configuration records.
 */
@UtilityClass
public final class ConfigLoader {

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
     * @param root root Bukkit configuration section
     * @return validated immutable configuration
     */
    public static @NotNull PluginConfig load(@NotNull ConfigurationSection root) {
        ConfigReader reader = new ConfigReader(root);
        DatabaseConfig database = DatabaseConfigLoader.load(reader);
        Map<String, TitleConfig> titles = TitleConfigLoader.load(reader);

        return new PluginConfig(database, titles);
    }
}
