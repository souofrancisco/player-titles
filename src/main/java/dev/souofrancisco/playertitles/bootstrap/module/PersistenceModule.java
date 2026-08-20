package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.PlayerTitlesDebug;
import dev.souofrancisco.playertitles.internal.PlayerTitleCache;
import dev.souofrancisco.playertitles.repository.database.Database;
import dev.souofrancisco.playertitles.repository.executor.DatabaseExecutor;
import dev.souofrancisco.playertitles.repository.database.DatabaseFactory;
import dev.souofrancisco.playertitles.repository.query.PlayerTitleQueries;
import dev.souofrancisco.playertitles.repository.PlayerTitleJdbcStore;
import dev.souofrancisco.playertitles.repository.PlayerTitleRepository;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Opens persistence components and shuts them down after queued database work drains.
 */
public final class PersistenceModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PlayerTitlesDebug debug = requireDebug(context);
        PluginConfig pluginConfig = ConfigLoader.current();

        Path dataDirectory = context.plugin().getDataFolder().toPath();

        Database database = DatabaseFactory.createDatabase(pluginConfig.database(), dataDirectory);

        PlayerTitleQueries queries = DatabaseFactory.createQueries(pluginConfig.database().type());
        DatabaseExecutor executor = DatabaseExecutor.create();

        PlayerTitleJdbcStore jdbcStore = new PlayerTitleJdbcStore(database, queries);
        PlayerTitleRepository repository = new PlayerTitleRepository(executor, jdbcStore, debug);

        database.open();
        repository.initializeSchema().join();

        debug.log("PERSIST", () -> "Schema initialization completed type=" + pluginConfig.database().type());

        context.database(database);
        context.databaseExecutor(executor);
        context.playerTitleRepository(repository);
    }

    @Override
    public void disable(@NotNull BootstrapContext context) {
        PlayerTitlesDebug debug = context.playerTitlesDebug();

        RuntimeException failure = flushSelectedTitles(context, debug);
        failure = closeExecutor(context, failure);
        failure = closeDatabase(context, failure);

        if (debug != null) {
            debug.log("PERSIST", () -> "Shutdown persistence completed");
        }

        if (failure != null) throw failure;
    }

    private @Nullable RuntimeException flushSelectedTitles(
            @NotNull BootstrapContext context,
            @Nullable PlayerTitlesDebug debug
    ) {
        PlayerTitleRepository repository = context.playerTitleRepository();
        PlayerTitleCache cache = context.playerTitleCache();
        if (repository == null || cache == null) return null;

        var snapshot = cache.snapshot();

        try {
            repository.persistSelectedTitles(snapshot).join();
            if (debug != null) {
                debug.log("PERSIST", () -> "Persisted shutdown batch players=" + snapshot.size());
            }
            return null;
        } catch (CompletionException exception) {
            return new IllegalStateException("Could not persist remaining selected player titles.", exception);
        }
    }

    private @Nullable RuntimeException closeExecutor(
            @NotNull BootstrapContext context,
            @Nullable RuntimeException failure
    ) {
        DatabaseExecutor executor = context.databaseExecutor();
        if (executor == null) return failure;

        try {
            executor.close();
            return failure;
        } catch (RuntimeException exception) {
            return combine(failure, exception);
        }
    }

    private @Nullable RuntimeException closeDatabase(
            @NotNull BootstrapContext context,
            @Nullable RuntimeException failure
    ) {
        Database database = context.database();
        if (database == null) return failure;

        try {
            database.close();
            return failure;
        } catch (RuntimeException exception) {
            return combine(failure, exception);
        }
    }

    private @NotNull RuntimeException combine(
            @Nullable RuntimeException failure,
            @NotNull RuntimeException exception
    ) {
        if (failure == null) return exception;

        failure.addSuppressed(exception);
        return failure;
    }

    private @NotNull PlayerTitlesDebug requireDebug(@NotNull BootstrapContext context) {
        PlayerTitlesDebug debug = context.playerTitlesDebug();
        if (debug == null) {
            throw new IllegalStateException("PlayerTitlesDebug must be initialized before PersistenceModule.");
        }
        return debug;
    }
}
