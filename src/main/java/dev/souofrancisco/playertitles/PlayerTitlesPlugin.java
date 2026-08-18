package dev.souofrancisco.playertitles;

import dev.souofrancisco.playertitles.database.Database;
import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerTitlesPlugin extends JavaPlugin {
    private Database database;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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

        getLogger().info("PlayerTitles disabled.");
    }
}
