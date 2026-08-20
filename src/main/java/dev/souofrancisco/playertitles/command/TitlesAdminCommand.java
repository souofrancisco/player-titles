package dev.souofrancisco.playertitles.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.souofrancisco.playertitles.command.subcommand.GiveTitleSubcommand;
import dev.souofrancisco.playertitles.command.subcommand.ReloadTitlesSubcommand;
import dev.souofrancisco.playertitles.command.subcommand.RevokeTitleSubcommand;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.render.TextRenderer;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Assembles and registers the {@code /titlesadmin} root command.
 */
@RequiredArgsConstructor
public final class TitlesAdminCommand {

    public static final @NotNull String NAME = "titlesadmin";

    private final @NotNull JavaPlugin plugin;
    private final @NotNull PlayerTitlesController controller;
    private final @NotNull TextRenderer textRenderer;

    public void register(@NotNull JavaPlugin plugin) {
        new CommandAPICommand(NAME)
                .withShortDescription("Manages player title ownership.")
                .withSubcommand(new GiveTitleSubcommand(controller, textRenderer).create())
                .withSubcommand(new RevokeTitleSubcommand(controller, textRenderer).create())
                .withSubcommand(new ReloadTitlesSubcommand(this.plugin).create())
                .register(plugin);
    }
}
