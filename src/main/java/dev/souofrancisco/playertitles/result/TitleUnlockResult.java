package dev.souofrancisco.playertitles.result;

/**
 * Result of attempting to unlock a title through PlayerTitles.
 */
public enum TitleUnlockResult {
    /**
     * The player is not currently loaded in the in-memory title cache.
     *
     * <p>Returned only by loaded-player cache operations. Persistent unlock APIs do not return this
     * value for offline targets.
     */
    PLAYER_NOT_LOADED,

    /**
     * The requested title ID does not exist in the validated plugin configuration.
     */
    TITLE_NOT_FOUND,

    /**
     * The player already owns the requested title, so ownership was not changed.
     */
    ALREADY_UNLOCKED,

    /**
     * The requested title was newly unlocked for the player.
     */
    UNLOCKED
}
