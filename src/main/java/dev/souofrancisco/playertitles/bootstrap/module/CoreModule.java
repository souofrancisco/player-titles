package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.internal.PlayerTitleCache;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.repository.PlayerTitleRepository;
import dev.souofrancisco.playertitles.service.PlayerTitlesService;
import org.jetbrains.annotations.NotNull;

/**
 * Wires the core in-memory title state components after configuration is available.
 */
public final class CoreModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PlayerTitleRepository repository = context.playerTitleRepository();
        if (repository == null)
            throw new IllegalStateException("PlayerTitleRepository must be initialized before CoreModule.");

        PlayerTitleCache cache = new PlayerTitleCache();
        PlayerTitlesController controller = new PlayerTitlesController(
                context.plugin(),
                cache,
                repository,
                context.plugin().getLogger()
        );

        context.playerTitleCache(cache);
        context.playerTitlesController(controller);
        context.playerTitlesApi(new PlayerTitlesService(controller));
    }
}
