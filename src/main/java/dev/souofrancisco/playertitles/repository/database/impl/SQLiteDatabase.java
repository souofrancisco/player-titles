package dev.souofrancisco.playertitles.repository.database.impl;

import com.zaxxer.hikari.HikariConfig;
import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.repository.database.Database;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * SQLite Hikari configuration and connection setup.
 */
@RequiredArgsConstructor
public final class SQLiteDatabase extends Database {

    private static final String DRIVER_CLASS = "org.sqlite.JDBC";
    private static final String DATABASE_DIRECTORY = "db";

    /** Enables write-ahead logging for safer concurrent reads while writes are serialized. */
    private static final String ENABLE_WAL = "PRAGMA journal_mode = WAL";

    /** Enforces foreign key constraints for the active SQLite connection. */
    private static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON";

    /** Allows SQLite to wait briefly when the database file is locked. */
    private static final String SET_BUSY_TIMEOUT = "PRAGMA busy_timeout = 5000";

    private final @NotNull DatabaseConfig config;
    private final @NotNull Path dataDirectory;

    @Override
    protected @NotNull HikariConfig hikariConfig() {
        Path databaseFile = databaseFile();
        Path parent = databaseFile.getParent();
        if (parent != null) {
            createDirectories(parent);
        }

        HikariConfig hikariConfig = baseHikariConfig();
        hikariConfig.setDriverClassName(DRIVER_CLASS);
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaxLifetime(0L);
        hikariConfig.setConnectionInitSql(ENABLE_FOREIGN_KEYS);
        return hikariConfig;
    }

    @Override
    protected void configureConnection(@NotNull Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(ENABLE_FOREIGN_KEYS);
            statement.execute(SET_BUSY_TIMEOUT);
            statement.execute(ENABLE_WAL);
        }
    }

    private @NotNull Path databaseFile() {
        Path normalizedDataDirectory = dataDirectory.toAbsolutePath().normalize();
        Path databaseDirectory = normalizedDataDirectory.resolve(DATABASE_DIRECTORY).normalize();
        Path databaseFile = databaseDirectory.resolve(config.sqliteFile()).normalize();
        if (!databaseFile.startsWith(databaseDirectory)) {
            throw new IllegalArgumentException("SQLite database file must stay inside the plugin database directory.");
        }
        return databaseFile;
    }

    private void createDirectories(@NotNull Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create database directory.", exception);
        }
    }
}
