package dev.souofrancisco.playertitles.api;

import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Public API for cache-backed player title operations.
 *
 * <p>This contract is intentionally independent from Bukkit, Paper, and Folia entity types. Callers
 * identify players by {@link UUID} so API usage does not imply ownership of a live Minecraft entity or
 * a particular scheduler context.
 *
 */
public interface PlayerTitlesApi {

    /**
     * Checks whether title data for a player is currently loaded in memory.
     *
     * <p>This method only reads the in-memory cache and performs no database access. Implementations
     * must be safe to call from different Paper/Folia server threads, provided callers pass
     * a non-null player UUID.
     *
     * @param playerId UUID of the player to check
     * @return {@code true} when the player's title data is loaded in the cache, or {@code false} when
     *     the player is not loaded
     */
    boolean isLoaded(@NotNull UUID playerId);

    /**
     * Gets the title IDs currently unlocked by a loaded player.
     *
     * <p>This method only reads the in-memory cache and performs no database access. Implementations
     * must be safe to call from different Paper/Folia server threads, provided callers pass
     * a non-null player UUID.
     *
     * @param playerId UUID of the player whose unlocked titles should be returned
     * @return a non-null set of unlocked title IDs. When the player is not loaded, this returns an
     *     empty set. The returned set must not expose mutable internal cache state.
     */
    @NotNull
    Set<@NotNull String> getUnlockedTitles(@NotNull UUID playerId);

    /**
     * Gets the title ID currently selected by a loaded player.
     *
     * <p>This method only reads the in-memory cache and performs no database access. Implementations
     * must be safe to call from different Paper/Folia server threads, provided callers pass
     * a non-null player UUID.
     *
     * @param playerId UUID of the player whose selected title should be returned
     * @return a non-null {@link Optional} containing the selected title ID when the player is loaded
     *     and has a selected title. Returns {@link Optional#empty()} when the player is not loaded or
     *     no title is selected. The {@link NotNull} annotation applies to the {@link Optional}
     *     container and to any contained title ID.
     */
    @NotNull
    Optional<String> getSelectedTitle(@NotNull UUID playerId);

    /**
     * Checks whether a loaded player owns an unlocked title.
     *
     * <p>This method only reads the in-memory cache and performs no database access. Implementations
     * must be safe to call from different Paper/Folia server threads, provided callers pass
     * non-null arguments.
     *
     * @param playerId UUID of the player to check
     * @param titleId title ID to check
     * @return {@code true} when the player is loaded and owns the title, or {@code false} when the
     *     player is not loaded or does not own the title
     */
    boolean hasTitle(@NotNull UUID playerId, @NotNull String titleId);

    /**
     * Unlocks a title for a loaded player.
     *
     * <p>This method only updates the in-memory cache and performs no database access. Implementations
     * must be safe to call from different Paper/Folia server threads, provided callers pass
     * non-null arguments.
     *
     * @param playerId UUID of the player receiving the title
     * @param titleId title ID to unlock
     * @return a non-null result describing the cache outcome: {@link TitleUnlockResult#UNLOCKED}
     *     when the title was newly unlocked, {@link TitleUnlockResult#ALREADY_UNLOCKED} when the
     *     player already owned it, {@link TitleUnlockResult#TITLE_NOT_FOUND} when the title is not
     *     configured, or {@link TitleUnlockResult#PLAYER_NOT_LOADED} when the player is not loaded
     */
    @NotNull
    TitleUnlockResult unlockTitle(@NotNull UUID playerId, @NotNull String titleId);

    /**
     * Selects one of a loaded player's unlocked titles.
     *
     * <p>This method only updates the in-memory cache and performs no database access. Implementations
     * must be safe to call from different Paper/Folia server threads, provided callers pass
     * non-null arguments.
     *
     * @param playerId UUID of the player selecting the title
     * @param titleId unlocked title ID to select
     * @return a non-null result describing the cache outcome: {@link TitleSelectionResult#SELECTED}
     *     when the selected title changed, {@link TitleSelectionResult#ALREADY_SELECTED} when that
     *     title was already selected, {@link TitleSelectionResult#TITLE_NOT_FOUND} when the title is
     *     not configured, {@link TitleSelectionResult#TITLE_NOT_UNLOCKED} when the player does not own
     *     the title, or {@link TitleSelectionResult#PLAYER_NOT_LOADED} when the player is not loaded
     */
    @NotNull
    TitleSelectionResult selectTitle(@NotNull UUID playerId, @NotNull String titleId);

    /**
     * Clears the currently selected title for a loaded player.
     *
     * <p>This method only updates the in-memory cache and performs no database access. Implementations
     * must be safe to call from different Paper/Folia server threads, provided callers pass
     * a non-null player UUID.
     *
     * @param playerId UUID of the player whose selected title should be cleared
     * @return a non-null result describing the cache outcome: {@link TitleSelectionResult#CLEARED}
     *     when a selected title was removed, {@link TitleSelectionResult#NOTHING_SELECTED} when the
     *     player was loaded but had no selected title, or {@link TitleSelectionResult#PLAYER_NOT_LOADED}
     *     when the player is not loaded
     */
    @NotNull
    TitleSelectionResult clearSelectedTitle(@NotNull UUID playerId);
}
