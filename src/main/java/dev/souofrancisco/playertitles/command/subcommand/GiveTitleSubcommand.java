package dev.souofrancisco.playertitles.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminGiveMessagesConfig;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.render.TextRenderer;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class GiveTitleSubcommand {

    private static final @NotNull String PERMISSION = "playertitles.admin.give";

    private final @NotNull PlayerTitlesController controller;
    private final @NotNull TextRenderer textRenderer;

    public @NotNull CommandAPICommand create() {
        return new CommandAPICommand("give")
                .withPermission(PERMISSION)
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(titleArgument())
                .executes((CommandExecutor) (sender, args) -> execute(sender, args.get("player"), args.get("title")));
    }

    private void execute(
            @NotNull CommandSender sender,
            @NotNull Object target,
            @NotNull Object title
    ) {
        Player player = (Player) target;
        String titleId = (String) title;
        TitleUnlockResult result = controller.unlockTitle(player.getUniqueId(), titleId);
        sender.sendMessage(render(player, titleId, message(result)));
    }

    private @NotNull Argument<String> titleArgument() {
        return new StringArgument("title")
                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                        ConfigLoader.current().titles().keySet().toArray(String[]::new)
                ));
    }

    private @NotNull String message(@NotNull TitleUnlockResult result) {
        AdminGiveMessagesConfig messages = ConfigLoader.current().messages().admin().give();
        return switch (result) {
            case UNLOCKED -> messages.success();
            case ALREADY_UNLOCKED -> messages.alreadyUnlocked();
            case TITLE_NOT_FOUND -> messages.titleNotFound();
            case PLAYER_NOT_LOADED -> messages.playerNotLoaded();
        };
    }

    private @NotNull Component render(
            @NotNull Player player,
            @NotNull String titleId,
            @NotNull String rawMessage
    ) {
        return textRenderer.render(player, rawMessage, tagResolver(player, titleId));
    }

    private @NotNull TagResolver tagResolver(
            @NotNull Player player,
            @NotNull String titleId
    ) {
        return TagResolver.builder()
                .resolver(TagResolver.resolver("player", (arguments, context) ->
                        Tag.selfClosingInserting(Component.text(player.getName()))))
                .resolver(TagResolver.resolver("title", (arguments, context) ->
                        Tag.selfClosingInserting(titleDisplayName(titleId))))
                .build();
    }

    private @NotNull Component titleDisplayName(@NotNull String titleId) {
        TitleConfig title = ConfigLoader.current().titles().get(titleId);
        return title == null ? Component.text(titleId) : textRenderer.render(title.displayName());
    }
}
