package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.repository.Database;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * Opens and closes the plugin database connection pool.
 */
public final class PersistenceModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        Path dataDirectory = context.plugin().getDataFolder().toPath();
        Database database = new Database();
        database.open(dataDirectory);
        context.database(database);
    }

    @Override
    public void disable(@NotNull BootstrapContext context) {
        Database database = context.database();
        if (database == null) return;

        database.close();
    }
}
