package dev.souofrancisco.playertitles.result;

/**
 * Result of attempting to unlock a title through the public PlayerTitles API.
 */
public enum TitleUnlockResult {
    /**
     * The player is not currently loaded in the in-memory title cache, so no title was unlocked.
     */
    PLAYER_NOT_LOADED,

    /**
     * The requested title ID does not exist in the validated plugin configuration.
     */
    TITLE_NOT_FOUND,

    /**
     * The player is loaded and already owns the requested title, so the cached unlock set was not
     * changed.
     */
    ALREADY_UNLOCKED,

    /**
     * The player is loaded and the requested title was newly added to the cached unlock set.
     */
    UNLOCKED
}
