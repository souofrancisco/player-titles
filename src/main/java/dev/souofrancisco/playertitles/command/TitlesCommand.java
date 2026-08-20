package dev.souofrancisco.playertitles.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import dev.souofrancisco.playertitles.gui.PlayerTitlesMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Player entry point for the titles GUI.
 */
@RequiredArgsConstructor
public final class TitlesCommand {

    public static final @NotNull String NAME = "titles";

    private final @NotNull PlayerTitlesMenu menu;

    public void register(@NotNull JavaPlugin plugin) {
        new CommandAPICommand(NAME)
                .withShortDescription("Opens the PlayerTitles menu.")
                .withUsage("/titles")
                .executesPlayer((PlayerCommandExecutor) (player, args) -> menu.open(player))
                .register(plugin);
    }
}
