package dev.souofrancisco.playertitles.config.section;

import dev.souofrancisco.playertitles.repository.database.DatabaseType;
import org.jetbrains.annotations.NotNull;

/**
 * Typed persistence configuration used to create the database connection pool.
 */
public record DatabaseConfig(
        @NotNull DatabaseType type,
        @NotNull String sqliteFile,
        @NotNull String host,
        int port,
        @NotNull String database,
        @NotNull String username,
        @NotNull String password,
        int maximumPoolSize
) {

    public static @NotNull DatabaseConfig sqliteDefaults() {
        return new DatabaseConfig(
                DatabaseType.SQLITE,
                "player-titles.db",
                "localhost",
                3306,
                "player_titles",
                "player_titles",
                "",
                4
        );
    }
}
