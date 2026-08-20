package dev.souofrancisco.playertitles.gui.model;

import dev.souofrancisco.playertitles.config.section.menu.type.TitleStatus;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable runtime snapshot for one opened titles menu.
 *
 * <p>This model is intentionally GUI-local and does not read Bukkit, storage, cache, or the public
 * API. The menu refreshes it from the API when player title state changes.
 */
public record MenuState(
        @NotNull Set<@NotNull String> unlockedTitles,
        @Nullable String selectedTitleId
) {

    public MenuState {
        unlockedTitles = Set.copyOf(unlockedTitles);
    }

    public @NotNull TitleStatus statusOf(@NotNull String titleId) {
        if (titleId.equals(selectedTitleId)) return TitleStatus.SELECTED;
        return unlockedTitles.contains(titleId) ? TitleStatus.UNLOCKED : TitleStatus.LOCKED;
    }
}
