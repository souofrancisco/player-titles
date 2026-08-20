package dev.souofrancisco.playertitles.result;

/**
 * Result of attempting to revoke a title through PlayerTitles.
 */
public enum TitleRevokeResult {
    /**
     * The player is not currently loaded in the in-memory title cache.
     *
     * <p>Returned only by loaded-player cache operations. Persistent revoke APIs do not return this
     * value for offline targets.
     */
    PLAYER_NOT_LOADED,

    /**
     * The requested title ID does not exist in the validated plugin configuration.
     */
    TITLE_NOT_FOUND,

    /**
     * The requested title is not unlocked for the player, so it was not revoked.
     */
    NOT_UNLOCKED,

    /**
     * The requested title was removed from the player's unlock set.
     */
    REVOKED
}
