package dev.souofrancisco.playertitles.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable cached title state for one player.
 *
 * <p>All collection input is defensively copied and state-transition methods return new record
 * instances, so callers can safely share snapshots without exposing mutable cache internals.
 */
public record PlayerTitleState(
        @NotNull UUID playerId,
        @NotNull Set<@NotNull String> unlockedTitles,
        @Nullable String selectedTitleId
) {

    public PlayerTitleState {
        unlockedTitles = Set.copyOf(unlockedTitles);
    }

    public boolean hasTitle(@NotNull String titleId) {
        return unlockedTitles.contains(titleId);
    }

    public boolean isSelected(@NotNull String titleId) {
        return titleId.equals(selectedTitleId);
    }

    public @NotNull PlayerTitleState unlock(@NotNull String titleId) {
        Set<String> nextUnlockedTitles = new HashSet<>(unlockedTitles);
        nextUnlockedTitles.add(titleId);
        return new PlayerTitleState(playerId, nextUnlockedTitles, selectedTitleId);
    }

    /**
     * Removes an unlocked title and clears the active selection when that title was selected.
     */
    public @NotNull PlayerTitleState revoke(@NotNull String titleId) {
        Set<String> nextUnlockedTitles = new HashSet<>(unlockedTitles);
        nextUnlockedTitles.remove(titleId);
        return new PlayerTitleState(
                playerId,
                nextUnlockedTitles,
                isSelected(titleId) ? null : selectedTitleId
        );
    }

    public @NotNull PlayerTitleState select(@Nullable String titleId) {
        return new PlayerTitleState(playerId, unlockedTitles, titleId);
    }
}
