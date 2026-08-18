package dev.souofrancisco.playertitles;

import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.repository.Database;
import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerTitlesPlugin extends JavaPlugin {

    private Database database;
    private PluginConfig pluginConfig;

    @Override
    public void onEnable() {
        try {
            pluginConfig = ConfigLoader.load(this);
        } catch (IllegalArgumentException exception) {
            getLogger().severe("Failed to load configuration: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            throw exception;
        }

        try {
            Path dataDirectory = getDataFolder().toPath();
            database = new Database();
            database.open(dataDirectory);
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to initialize the database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            throw exception;
        }

        getLogger().info("PlayerTitles enabled.");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
            database = null;
        }

        pluginConfig = null;
        getLogger().info("PlayerTitles disabled.");
    }
}
