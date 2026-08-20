package dev.souofrancisco.playertitles.listener;

import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Thin Bukkit listener that only forwards player lifecycle events to {@link PlayerTitlesController}.
 *
 * <p>This class must not access persistence, cache state, database executors, JDBC, or Folia
 * entity schedulers. The controller owns all lifecycle orchestration after an event is received.
 */
@RequiredArgsConstructor
public final class PlayerConnectionListener implements Listener {

    private final @NotNull PlayerTitlesController controller;

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        controller.loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        controller.unloadPlayer(event.getPlayer().getUniqueId());
    }
}
