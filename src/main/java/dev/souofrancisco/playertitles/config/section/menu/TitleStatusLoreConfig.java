package dev.souofrancisco.playertitles.config.section.menu;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public record TitleStatusLoreConfig(
        @NotNull List<@NotNull String> locked,
        @NotNull List<@NotNull String> unlocked,
        @NotNull List<@NotNull String> selected
) {

    public TitleStatusLoreConfig {
        locked = List.copyOf(locked);
        unlocked = List.copyOf(unlocked);
        selected = List.copyOf(selected);
    }
}
