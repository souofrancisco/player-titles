package dev.souofrancisco.playertitles.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminReloadMessagesConfig;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public final class ReloadTitlesSubcommand {

    private static final @NotNull String PERMISSION = "playertitles.admin.reload";

    private final @NotNull JavaPlugin plugin;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    public @NotNull CommandAPICommand create() {
        return new CommandAPICommand("reload")
                .withPermission(PERMISSION)
                .executes((CommandExecutor) (sender, args) -> execute(sender));
    }

    private void execute(@NotNull CommandSender sender) {
        try {
            PluginConfig previous = ConfigLoader.current();
            PluginConfig next = ConfigLoader.reload(plugin);

            AdminReloadMessagesConfig messages = next.messages().admin().reload();
            String rawMessage = previous.database().equals(next.database())
                    ? messages.success()
                    : messages.successDatabaseRestartRequired();
            sender.sendMessage(miniMessage.deserialize(rawMessage));
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Could not reload PlayerTitles configuration from config.yml, titles.yml, and menu.yml."
                            + " Keeping the previous working configuration active.",
                    exception
            );
            sender.sendMessage(miniMessage.deserialize(
                    ConfigLoader.current().messages().admin().reload().failure(),
                    TagResolver.resolver("error", (arguments, context) ->
                            Tag.selfClosingInserting(Component.text(failureMessage(exception))))
            ));
        }
    }

    private @NotNull String failureMessage(@Nullable Throwable failure) {
        if (failure == null) return "unknown error";
        String message = failure.getMessage();
        return message == null || message.isBlank() ? failure.getClass().getSimpleName() : message;
    }
}
