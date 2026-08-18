package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.internal.PlayerTitleCache;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import org.jetbrains.annotations.NotNull;

/**
 * Wires the core in-memory title state components after configuration is available.
 */
public final class CoreModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PluginConfig pluginConfig = context.pluginConfig();
        if (pluginConfig == null)
            throw new IllegalStateException("PluginConfig must be initialized before CoreModule.");

        PlayerTitleCache cache = new PlayerTitleCache();
        PlayerTitlesController controller = new PlayerTitlesController(cache, pluginConfig);

        context.playerTitleCache(cache);
        context.playerTitlesController(controller);
    }
}
