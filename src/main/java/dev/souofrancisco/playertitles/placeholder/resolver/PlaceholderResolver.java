package dev.souofrancisco.playertitles.placeholder.resolver;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolver contract for one or more PlayerTitles placeholder keys.
 */
public interface PlaceholderResolver {

    boolean supports(@NotNull String key);

    @NotNull
    String resolve(@Nullable OfflinePlayer player);
}
