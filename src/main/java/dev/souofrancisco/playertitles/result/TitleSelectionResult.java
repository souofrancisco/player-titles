package dev.souofrancisco.playertitles.result;

/**
 * Result of attempting to select or clear a selected title through the public PlayerTitles API.
 */
public enum TitleSelectionResult {
    /**
     * The player is not currently loaded in the in-memory title cache, so no selection was changed.
     */
    PLAYER_NOT_LOADED,

    /**
     * The requested title ID does not exist in the validated plugin configuration.
     */
    TITLE_NOT_FOUND,

    /**
     * The requested title is not unlocked for the loaded player, so it was not selected.
     */
    TITLE_NOT_UNLOCKED,

    /**
     * The requested title is already selected for the loaded player, so the cached selection was not
     * changed.
     */
    ALREADY_SELECTED,

    /**
     * The requested title was selected for the loaded player in the in-memory cache.
     */
    SELECTED,

    /**
     * The loaded player had no selected title, so clearing the cached selection made no change.
     */
    NOTHING_SELECTED,

    /**
     * The loaded player's selected title was cleared from the in-memory cache.
     */
    CLEARED
}
