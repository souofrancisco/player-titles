# PlayerTitles

A Folia-compatible Minecraft plugin for managing cosmetic player title prefixes with a paginated GUI, PlaceholderAPI integration and async persistence.

## Features

- Configurable titles with MiniMessage formatting and per-title GUI icons.
- Paginated InvUI title selection menu with status lore (unlocked / selected / locked).
- SQLite and MySQL persistence through HikariCP.
- Async database operations that never block Paper or Folia server threads.
- PlaceholderAPI expansion (`%playertitles_title%`).
- Vanilla `item_model` support for Nexo and ItemsAdder custom items.
- Full offline player support for unlock and revoke operations.
- Hot-reloadable configuration.

## Tech Stack

- Java 21
- Folia / Minecraft 1.21.11
- CommandAPI 11.1.0
- InvUI 1.49
- SQLite / MySQL
- HikariCP
- Gradle Kotlin DSL

## Requirements

- Paper 1.21+ or Folia 1.21+.
- Java 21.
- [CommandAPI](https://commandapi.jorel.dev/) as a server plugin.
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional).

## Building

```bash
./gradlew build
```

The shaded plugin jar is created at:

```text
build/libs/player-titles-0.1.0.jar
```

If `../test-server/plugins/` exists, the build also copies the jar there. If that directory does not exist, the build still succeeds.

## Documentation

| Document | Description |
| --- | --- |
| [`docs/api.md`](docs/api.md) | Public API usage, lifecycle expectations, result semantics and offline mutations |
| [`docs/configuration.md`](docs/configuration.md) | Configuration reference for `config.yml`, `titles.yml` and `menu.yml` |
| [`docs/database-threading.md`](docs/database-threading.md) | Database execution model, JDBC isolation and shutdown behavior |
| [`docs/runtime-cache.md`](docs/runtime-cache.md) | Runtime player state, cache lifecycle and listener/controller flow |

For API usage and threading guarantees, see `docs/api.md`.

For the full configuration reference, see `docs/configuration.md`.

## Credits

This project was designed and developed by Francisco Correia.

ChatGPT (GPT-5.6 Sol) was used as a writing assistant to improve grammar, clarity, and wording in documentation and code comments.
All technical decisions, architecture, and implementation remain the responsibility of the project author.
