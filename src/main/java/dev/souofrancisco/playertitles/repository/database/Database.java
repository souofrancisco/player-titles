package dev.souofrancisco.playertitles.repository.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

/**
 * Owns the common HikariCP connection-pool lifecycle for a persistence engine.
 */
public abstract class Database implements AutoCloseable {

    private HikariDataSource dataSource;

    public final void open() {
        if (dataSource != null) {
            throw new IllegalStateException("Database is already open.");
        }

        dataSource = new HikariDataSource(hikariConfig());
        verifyConnection();
    }

    public final @NotNull Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("Database is not open.");
        }

        return dataSource.getConnection();
    }

    @Override
    public final void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    protected abstract @NotNull HikariConfig hikariConfig();

    protected @NotNull HikariConfig baseHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("PlayerTitlesPool");
        hikariConfig.setAutoCommit(true);
        hikariConfig.setConnectionTimeout(10_000L);
        hikariConfig.setValidationTimeout(3_000L);
        return hikariConfig;
    }

    protected void configureConnection(@NotNull Connection connection) throws SQLException {}

    private void verifyConnection() {
        try (Connection connection = getConnection()) {
            configureConnection(connection);
            if (!connection.isValid(2)) {
                throw new IllegalStateException("Database connection validation failed.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not verify database connection.", exception);
        }
    }
}
