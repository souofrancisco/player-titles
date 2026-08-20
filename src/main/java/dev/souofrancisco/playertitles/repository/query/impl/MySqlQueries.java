package dev.souofrancisco.playertitles.repository.query.impl;

import dev.souofrancisco.playertitles.repository.query.PlayerTitleQueries;
import org.jetbrains.annotations.NotNull;

/**
 * MySQL-specific schema and persistence statements.
 */
public final class MySqlQueries implements PlayerTitleQueries {

    /** Creates one profile row per player and stores the selected title. */
    private static final String CREATE_PROFILE_TABLE = """
            CREATE TABLE IF NOT EXISTS player_title_profiles (
                player_uuid VARCHAR(36) NOT NULL,
                selected_title_id VARCHAR(128) NULL,
                updated_at BIGINT NOT NULL,
                PRIMARY KEY (player_uuid)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    /** Creates the player-to-title relationship table for unlocked titles. */
    private static final String CREATE_UNLOCK_TABLE = """
            CREATE TABLE IF NOT EXISTS player_title_unlocks (
                player_uuid VARCHAR(36) NOT NULL,
                title_id VARCHAR(128) NOT NULL,
                unlocked_at BIGINT NOT NULL,
                PRIMARY KEY (player_uuid, title_id),
                CONSTRAINT fk_player_title_unlocks_profile
                    FOREIGN KEY (player_uuid)
                    REFERENCES player_title_profiles(player_uuid)
                    ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

    /** Loads the selected title for one player profile. */
    private static final String SELECT_PROFILE = """
            SELECT selected_title_id
            FROM player_title_profiles
            WHERE player_uuid = ?
            """;

    /** Loads all unlocked title IDs for one player using the composite primary key prefix. */
    private static final String SELECT_UNLOCKS = """
            SELECT title_id
            FROM player_title_unlocks
            WHERE player_uuid = ?
            """;

    /** Inserts or updates the selected title for a player profile. */
    private static final String UPSERT_PROFILE = """
            INSERT INTO player_title_profiles (player_uuid, selected_title_id, updated_at)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                selected_title_id = VALUES(selected_title_id),
                updated_at = VALUES(updated_at)
            """;

    /** Inserts a player profile row before an unlock row is persisted. */
    private static final String INSERT_PROFILE_IF_ABSENT = """
            INSERT IGNORE INTO player_title_profiles (player_uuid, selected_title_id, updated_at)
            VALUES (?, NULL, ?)
            """;

    /** Inserts one unlocked title while preserving idempotency for already-owned titles. */
    private static final String INSERT_UNLOCK = """
            INSERT IGNORE INTO player_title_unlocks (player_uuid, title_id, unlocked_at)
            VALUES (?, ?, ?)
            """;

    /** Deletes one unlocked title relationship for a player. */
    private static final String DELETE_UNLOCK = """
            DELETE FROM player_title_unlocks
            WHERE player_uuid = ? AND title_id = ?
            """;

    /** Clears the selected title only when it matches the revoked title. */
    private static final String CLEAR_SELECTED_TITLE_IF_MATCHES = """
            UPDATE player_title_profiles
            SET selected_title_id = NULL, updated_at = ?
            WHERE player_uuid = ? AND selected_title_id = ?
            """;

    @Override
    public @NotNull String createProfileTable() {
        return CREATE_PROFILE_TABLE;
    }

    @Override
    public @NotNull String createUnlockTable() {
        return CREATE_UNLOCK_TABLE;
    }

    @Override
    public @NotNull String selectProfile() {
        return SELECT_PROFILE;
    }

    @Override
    public @NotNull String selectUnlocks() {
        return SELECT_UNLOCKS;
    }

    @Override
    public @NotNull String upsertProfile() {
        return UPSERT_PROFILE;
    }

    @Override
    public @NotNull String insertProfileIfAbsent() {
        return INSERT_PROFILE_IF_ABSENT;
    }

    @Override
    public @NotNull String insertUnlock() {
        return INSERT_UNLOCK;
    }

    @Override
    public @NotNull String deleteUnlock() {
        return DELETE_UNLOCK;
    }

    @Override
    public @NotNull String clearSelectedTitleIfMatches() {
        return CLEAR_SELECTED_TITLE_IF_MATCHES;
    }
}
