package dev.souofrancisco.playertitles.repository;

import dev.souofrancisco.playertitles.internal.PlayerTitleState;
import dev.souofrancisco.playertitles.repository.database.Database;
import dev.souofrancisco.playertitles.repository.query.PlayerTitleQueries;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Blocking JDBC implementation for player title persistence.
 *
 * <p>This store owns connections, prepared statements, transactions, schema operations, and
 * result-set mapping. It is engine-agnostic JDBC code and must never use Bukkit/Folia schedulers or
 * be called directly by runtime layers; {@link PlayerTitleRepository} invokes it through
 * {@code DatabaseExecutor}.
 */
@RequiredArgsConstructor
public final class PlayerTitleJdbcStore {

    /** Maximum rows sent per JDBC batch so one flush stays within driver packet limits. */
    private static final int BATCH_SIZE = 500;

    private final @NotNull Database database;
    private final @NotNull PlayerTitleQueries queries;

    public void initializeSchema() throws SQLException {
        try (Connection connection = database.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(queries.createProfileTable());
            statement.execute(queries.createUnlockTable());
        }
    }

    public @NotNull PlayerTitleState load(@NotNull UUID playerId) throws SQLException {
        String playerUuid = playerId.toString();
        String selectedTitleId = null;
        Set<String> unlockedTitles = new HashSet<>();

        try (Connection connection = database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(queries.selectProfile())) {
                statement.setString(1, playerUuid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        selectedTitleId = resultSet.getString("selected_title_id");
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(queries.selectUnlocks())) {
                statement.setString(1, playerUuid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        unlockedTitles.add(resultSet.getString("title_id"));
                    }
                }
            }
        }

        return new PlayerTitleState(playerId, unlockedTitles, selectedTitleId);
    }

    public void persistUnlock(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) throws SQLException {
        withTransaction(connection -> {
            String playerUuid = playerId.toString();
            long timestamp = now();
            insertProfileIfAbsent(connection, playerUuid, timestamp);
            insertUnlock(connection, playerUuid, titleId, timestamp);
        });
    }

    public void persistSelectedTitle(
            @NotNull UUID playerId,
            @Nullable String titleId
    ) throws SQLException {
        withTransaction(connection -> upsertProfile(connection, playerId.toString(), titleId, now()));
    }

    public void persistSelectedTitles(
            @NotNull Collection<@NotNull PlayerTitleState> states
    ) throws SQLException {
        if (states.isEmpty()) return;

        withTransaction(connection -> {
            long timestamp = now();
            try (PreparedStatement statement = connection.prepareStatement(queries.upsertProfile())) {
                int pending = 0;
                for (PlayerTitleState state : states) {
                    bindProfileUpsert(statement, state.playerId().toString(), state.selectedTitleId(), timestamp);
                    statement.addBatch();

                    if (++pending == BATCH_SIZE) {
                        statement.executeBatch();
                        pending = 0;
                    }
                }

                if (pending > 0) statement.executeBatch();
            }
        });
    }

    private void upsertProfile(
            @NotNull Connection connection,
            @NotNull String playerUuid,
            @Nullable String selectedTitleId,
            long timestamp
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queries.upsertProfile())) {
            bindProfileUpsert(statement, playerUuid, selectedTitleId, timestamp);
            statement.executeUpdate();
        }
    }

    private void bindProfileUpsert(
            @NotNull PreparedStatement statement,
            @NotNull String playerUuid,
            @Nullable String selectedTitleId,
            long timestamp
    ) throws SQLException {
        statement.setString(1, playerUuid);
        statement.setString(2, selectedTitleId);
        statement.setLong(3, timestamp);
    }

    private void insertProfileIfAbsent(
            @NotNull Connection connection,
            @NotNull String playerUuid,
            long timestamp
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queries.insertProfileIfAbsent())) {
            statement.setString(1, playerUuid);
            statement.setLong(2, timestamp);
            statement.executeUpdate();
        }
    }

    private void insertUnlock(
            @NotNull Connection connection,
            @NotNull String playerUuid,
            @NotNull String titleId,
            long timestamp
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(queries.insertUnlock())) {
            statement.setString(1, playerUuid);
            statement.setString(2, titleId);
            statement.setLong(3, timestamp);
            statement.executeUpdate();
        }
    }

    private void withTransaction(@NotNull TransactionBody body) throws SQLException {
        try (Connection connection = database.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                body.run(connection);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private long now() {
        return Instant.now().toEpochMilli();
    }
}
