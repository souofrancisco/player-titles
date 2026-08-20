package dev.souofrancisco.playertitles.repository.database;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;

/**
 * Supported database engines for PlayerTitles persistence.
 */
public enum DatabaseType {
    SQLITE,
    MYSQL;

    public static @NotNull DatabaseType parse(@NotNull String rawType) {
        return switch (rawType.trim().toUpperCase(Locale.ROOT)) {
            case "SQLITE" -> SQLITE;
            case "MYSQL", "MARIADB" -> MYSQL;
            default -> throw new IllegalArgumentException("Unsupported database type '" + rawType + "'.");
        };
    }
}
