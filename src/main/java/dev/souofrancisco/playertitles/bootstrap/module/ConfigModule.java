package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.PlayerTitlesDebug;
import org.jetbrains.annotations.NotNull;

/**
 * Loads and validates the Bukkit YAML configuration.
 */
public final class ConfigModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PluginConfig config = ConfigLoader.load(context.plugin());
        PlayerTitlesDebug debug = new PlayerTitlesDebug(context.plugin().getLogger());
        context.playerTitlesDebug(debug);

        debug.log("CONFIG", () -> "Loaded configuration titles=" + config.titles().size());
    }
}
