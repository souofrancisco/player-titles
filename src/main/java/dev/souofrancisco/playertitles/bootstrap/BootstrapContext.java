package dev.souofrancisco.playertitles.bootstrap;

import dev.souofrancisco.playertitles.PlayerTitlesPlugin;
import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import dev.souofrancisco.playertitles.PlayerTitlesDebug;
import dev.souofrancisco.playertitles.internal.PlayerTitleCache;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.repository.database.Database;
import dev.souofrancisco.playertitles.repository.executor.DatabaseExecutor;
import dev.souofrancisco.playertitles.repository.PlayerTitleRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the components shared between modules during plugin bootstrap.
 */
@Getter
@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class BootstrapContext {

    private final @NotNull PlayerTitlesPlugin plugin;

    private @Nullable Database database;
    private @Nullable DatabaseExecutor databaseExecutor;
    private @Nullable PlayerTitleRepository playerTitleRepository;
    private @Nullable PlayerTitleCache playerTitleCache;
    private @Nullable PlayerTitlesController playerTitlesController;
    private @Nullable PlayerTitlesApi playerTitlesApi;
    private @Nullable PlayerTitlesDebug playerTitlesDebug;
}
