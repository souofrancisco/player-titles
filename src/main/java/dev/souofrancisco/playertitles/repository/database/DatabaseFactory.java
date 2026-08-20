package dev.souofrancisco.playertitles.repository.database;

import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.repository.database.impl.MySqlDatabase;
import dev.souofrancisco.playertitles.repository.database.impl.SQLiteDatabase;
import dev.souofrancisco.playertitles.repository.query.PlayerTitleQueries;
import dev.souofrancisco.playertitles.repository.query.impl.MySqlQueries;
import dev.souofrancisco.playertitles.repository.query.impl.SQLiteQueries;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Creates engine-specific persistence components from the configured database type.
 */
@UtilityClass
public final class DatabaseFactory {

    public static @NotNull Database createDatabase(
            @NotNull DatabaseConfig config,
            @NotNull Path dataDirectory
    ) {
        return switch (config.type()) {
            case SQLITE -> new SQLiteDatabase(config, dataDirectory);
            case MYSQL -> new MySqlDatabase(config);
        };
    }

    public static @NotNull PlayerTitleQueries createQueries(@NotNull DatabaseType type) {
        return switch (type) {
            case SQLITE -> new SQLiteQueries();
            case MYSQL -> new MySqlQueries();
        };
    }
}
