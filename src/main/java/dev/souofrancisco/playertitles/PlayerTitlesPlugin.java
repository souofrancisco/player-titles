package dev.souofrancisco.playertitles;

import dev.souofrancisco.playertitles.bootstrap.PlayerTitlesBootstrap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class PlayerTitlesPlugin extends JavaPlugin {

    private @Nullable PlayerTitlesBootstrap bootstrap;

    @Override
    public void onEnable() {
        try {
            bootstrap = new PlayerTitlesBootstrap(this);
            bootstrap.enable();
            getLogger().info("PlayerTitles enabled.");
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to initialize PlayerTitles: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            throw exception;
        }
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
            bootstrap = null;
        }
        getLogger().info("PlayerTitles disabled.");
    }
}
