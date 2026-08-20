package dev.souofrancisco.playertitles.result;

/**
 * Result of attempting to revoke a title through the public PlayerTitles API.
 */
public enum TitleRevokeResult {
    /**
     * The player is not currently loaded in the in-memory title cache, so no title was revoked.
     */
    PLAYER_NOT_LOADED,

    /**
     * The requested title ID does not exist in the validated plugin configuration.
     */
    TITLE_NOT_FOUND,

    /**
     * The requested title is not unlocked for the loaded player, so it was not revoked.
     */
    NOT_UNLOCKED,

    /**
     * The player is loaded and the requested title was removed from the cached unlock set.
     */
    REVOKED
}
