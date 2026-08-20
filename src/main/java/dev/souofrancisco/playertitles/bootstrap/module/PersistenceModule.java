package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.internal.PlayerTitleCache;
import dev.souofrancisco.playertitles.internal.PlayerTitleState;
import dev.souofrancisco.playertitles.repository.database.Database;
import dev.souofrancisco.playertitles.repository.executor.DatabaseExecutor;
import dev.souofrancisco.playertitles.repository.database.DatabaseFactory;
import dev.souofrancisco.playertitles.repository.query.PlayerTitleQueries;
import dev.souofrancisco.playertitles.repository.PlayerTitleJdbcStore;
import dev.souofrancisco.playertitles.repository.PlayerTitleRepository;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.jetbrains.annotations.NotNull;

/**
 * Opens persistence components and shuts them down after queued database work drains.
 */
public final class PersistenceModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PluginConfig pluginConfig = context.pluginConfig();
        if (pluginConfig == null)
            throw new IllegalStateException("PluginConfig must be initialized before PersistenceModule.");

        Path dataDirectory = context.plugin().getDataFolder().toPath();

        Database database = DatabaseFactory.createDatabase(pluginConfig.database(), dataDirectory);

        PlayerTitleQueries queries = DatabaseFactory.createQueries(pluginConfig.database().type());
        DatabaseExecutor executor = DatabaseExecutor.create();

        PlayerTitleJdbcStore jdbcStore = new PlayerTitleJdbcStore(database, queries);
        PlayerTitleRepository repository = new PlayerTitleRepository(executor, jdbcStore);

        database.open();
        repository.initializeSchema().join();

        context.database(database);
        context.databaseExecutor(executor);
        context.playerTitleRepository(repository);
    }

    @Override
    public void disable(@NotNull BootstrapContext context) {
        RuntimeException failure = null;

        PlayerTitleRepository repository = context.playerTitleRepository();
        PlayerTitleCache cache = context.playerTitleCache();

        if (repository != null && cache != null) {
            Collection<PlayerTitleState> remainingStates = cache.snapshot();
            if (!remainingStates.isEmpty()) {
                try {
                    CompletableFuture.allOf(remainingStates.stream()
                            .map(state -> repository.persistSelectedTitle(
                                    state.playerId(),
                                    state.selectedTitleId()
                            ))
                            .toArray(CompletableFuture[]::new))
                            .join();
                } catch (CompletionException exception) {
                    failure = new IllegalStateException("Could not persist remaining selected player titles.", exception);
                }
            }
        }

        DatabaseExecutor executor = context.databaseExecutor();
        if (executor != null) {
            try {
                executor.close();
            } catch (RuntimeException exception) {
                failure = combine(failure, exception);
            }
        }

        Database database = context.database();
        if (database != null) {
            database.close();
        }

        if (failure != null) throw failure;
    }

    private @NotNull RuntimeException combine(
            RuntimeException failure,
            @NotNull RuntimeException exception
    ) {
        if (failure == null) return exception;

        failure.addSuppressed(exception);
        return failure;
    }
}
