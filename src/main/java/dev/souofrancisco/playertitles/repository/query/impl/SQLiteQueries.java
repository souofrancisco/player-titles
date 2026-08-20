package dev.souofrancisco.playertitles.repository.query.impl;

import dev.souofrancisco.playertitles.repository.query.PlayerTitleQueries;
import org.jetbrains.annotations.NotNull;

/**
 * SQLite-specific schema and persistence statements.
 */
public final class SQLiteQueries implements PlayerTitleQueries {

    /** Creates one profile row per player and stores the selected title. */
    private static final String CREATE_PROFILE_TABLE = """
            CREATE TABLE IF NOT EXISTS player_title_profiles (
                player_uuid TEXT PRIMARY KEY,
                selected_title_id TEXT NULL,
                updated_at INTEGER NOT NULL
            )
            """;

    /** Creates the player-to-title relationship table for unlocked titles. */
    private static final String CREATE_UNLOCK_TABLE = """
            CREATE TABLE IF NOT EXISTS player_title_unlocks (
                player_uuid TEXT NOT NULL,
                title_id TEXT NOT NULL,
                unlocked_at INTEGER NOT NULL,
                PRIMARY KEY (player_uuid, title_id),
                FOREIGN KEY (player_uuid)
                    REFERENCES player_title_profiles(player_uuid)
                    ON DELETE CASCADE
            )
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
            ON CONFLICT(player_uuid) DO UPDATE SET
                selected_title_id = excluded.selected_title_id,
                updated_at = excluded.updated_at
            """;

    /** Inserts a player profile row before an unlock row is persisted. */
    private static final String INSERT_PROFILE_IF_ABSENT = """
            INSERT OR IGNORE INTO player_title_profiles (player_uuid, selected_title_id, updated_at)
            VALUES (?, NULL, ?)
            """;

    /** Inserts one unlocked title while preserving idempotency for already-owned titles. */
    private static final String INSERT_UNLOCK = """
            INSERT OR IGNORE INTO player_title_unlocks (player_uuid, title_id, unlocked_at)
            VALUES (?, ?, ?)
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
}
