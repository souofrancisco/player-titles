package dev.souofrancisco.playertitles.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public final class Database implements AutoCloseable {

    private HikariDataSource dataSource;

    public void open(Path dataDirectory) {
        if (dataSource != null) {
            throw new IllegalStateException("Database is already open.");
        }

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create plugin data directory.", exception);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dataDirectory.resolve("player-titles.db").toAbsolutePath());
        config.setPoolName("PlayerTitlesPool");
        config.setMaximumPoolSize(1);
        config.setConnectionInitSql("PRAGMA foreign_keys = ON");

        dataSource = new HikariDataSource(config);
        verifyConnection();
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("Database is not open.");
        }

        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private void verifyConnection() {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(2)) {
                throw new IllegalStateException("Database connection validation failed.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not verify database connection.", exception);
        }
    }
}
