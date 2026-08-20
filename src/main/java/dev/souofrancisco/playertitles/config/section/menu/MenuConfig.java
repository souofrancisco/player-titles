package dev.souofrancisco.playertitles.config.section.menu;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable configuration for the titles inventory menu.
 */
public record MenuConfig(
        @NotNull String title,
        char titleSlot,
        @NotNull List<@NotNull String> layout,
        @NotNull Map<@NotNull Character, @NotNull MenuItemConfig> items,
        @NotNull TitleStatusLoreConfig titleStatus
) {

    public MenuConfig {
        layout = List.copyOf(layout);
        items = Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }

    public int rows() {
        return layout.size();
    }
}
