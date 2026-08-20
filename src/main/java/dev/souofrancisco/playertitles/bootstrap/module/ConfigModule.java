package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import org.jetbrains.annotations.NotNull;

/**
 * Loads and validates the Bukkit YAML configuration.
 */
public final class ConfigModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        ConfigLoader.load(context.plugin());
    }
}
