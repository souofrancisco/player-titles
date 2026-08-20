package dev.souofrancisco.playertitles.repository.query;

import org.jetbrains.annotations.NotNull;

/**
 * SQL statements required by the player-title JDBC repository.
 */
public interface PlayerTitleQueries {

    /** Creates one profile row per player and stores the selected title. */
    @NotNull String createProfileTable();

    /** Creates the player-to-title relationship table for unlocked titles. */
    @NotNull String createUnlockTable();

    /** Loads the selected title for one player profile. */
    @NotNull String selectProfile();

    /** Loads all unlocked title IDs for one player using the composite primary key prefix. */
    @NotNull String selectUnlocks();

    /** Inserts or updates the selected title for a player profile. */
    @NotNull String upsertProfile();

    /** Inserts a player profile row before an unlock row is persisted. */
    @NotNull String insertProfileIfAbsent();

    /** Inserts one unlocked title while preserving idempotency for already-owned titles. */
    @NotNull String insertUnlock();
}
