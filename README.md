# PlayerTitles

PlayerTitles is a minimal Folia-compatible Minecraft plugin foundation for Java 21.

## Stack

- Java 21
- Folia / Minecraft 1.21.11
- CommandAPI 11.1.0
- InvUI 1.49
- SQLite
- HikariCP
- Gradle Kotlin DSL

## Build

```bash
./gradlew build
```

The shaded plugin jar is created at:

```text
build/libs/player-titles-0.1.0.jar
```

If `../test-server/plugins/` exists, the build also copies the jar there. If that directory does not exist, the build still succeeds.

## Runtime

The server must provide CommandAPI as a plugin because PlayerTitles declares it as a required dependency in `plugin.yml`.

## Credits

This project was designed and developed by Francisco Correia.

ChatGPT (GPT-5.6 Sol) was used as a writing assistant to improve grammar, clarity, and wording in documentation and code comments. 
All technical decisions, architecture, and implementation remain the responsibility of the project author.
