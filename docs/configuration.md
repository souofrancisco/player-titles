# Configuration

## Files

| File | Purpose |
| --- | --- |
| `config.yml` | Database connection, messages, debug toggle |
| `titles.yml` | Title definitions (display name, prefix, icon) |
| `menu.yml` | GUI layout, items, and status lore |

All text values are raw MiniMessage templates. Player-aware rendering applies PlaceholderAPI first, then MiniMessage. The `%playertitles_title%` expansion renders the selected prefix with MiniMessage only (no PlaceholderAPI pass).

## config.yml

### debug

```yaml
debug: false
```

Enables internal debug logging.

### database

```yaml
database:
  type: sqlite
  sqlite:
    file: player-titles.db
  mysql:
    host: localhost
    port: 3306
    database: player_titles
    username: player_titles
    password: ""
    maximum-pool-size: 4
```

| Key | Description |
| --- | --- |
| `type` | `sqlite` or `mysql` |
| `sqlite.file` | Database filename inside the plugin's `db/` directory |
| `mysql.host` | MySQL server hostname |
| `mysql.port` | MySQL server port |
| `mysql.database` | Schema name |
| `mysql.username` | Connection username |
| `mysql.password` | Connection password |
| `mysql.maximum-pool-size` | HikariCP maximum pool size |

### messages

All message values are MiniMessage templates. Available placeholders depend on the command context:

```yaml
messages:
  admin:
    give:
      success: "<green>Title <white><title></white> granted to <white><player></white>."
      already-unlocked: "<yellow><player> already owns <title>."
      title-not-found: "<red>Title <title> does not exist."
      player-not-loaded: "<red><player> is not currently loaded."
      player-not-found: "<red>Could not resolve exactly one player for that name."
      failure: "<red>The title change failed: <white><error></white>"
    revoke:
      success: "<green>Title <white><title></white> revoked from <white><player></white>."
      not-unlocked: "<yellow><player> does not own <title>."
      # ... same keys as give
    reload:
      success: "<green>PlayerTitles configuration reloaded."
      success-database-restart-required: "<green>PlayerTitles configuration reloaded. <yellow>Database changes require a server restart."
      failure: "<red>PlayerTitles configuration reload failed: <white><error></white>"
```

Message tag resolvers:

| Tag | Context |
| --- | --- |
| `<title>` | Title display name |
| `<player>` | Target player name |
| `<error>` | Error message |

## titles.yml

Each top-level key is the title ID used in the API and commands.

```yaml
explorer:
  display-name: "<aqua>Explorer</aqua>"
  prefix: "<dark_gray>[<aqua>Explorer</aqua><dark_gray>]"
  icon:
    material: SPYGLASS
    # item-model: nexo:explorer_icon
    name: "<aqua>Explorer</aqua>"
    lore:
      - "<gray>Another generic example title."
      - "<gray>Hello, <white>%player_name%</white>."
      - ""
```

| Property | Description |
| --- | --- |
| `display-name` | MiniMessage template used in menus and messages |
| `prefix` | MiniMessage template rendered as the player's title prefix |
| `icon.material` | Bukkit `Material` for the GUI item |
| `icon.item-model` | Optional vanilla `item_model` namespaced key (1.21.4+). Nexo: `nexo:<item_id>`. ItemsAdder: `<namespace>:<item_id>` |
| `icon.name` | MiniMessage + PlaceholderAPI template for the item display name |
| `icon.lore` | List of MiniMessage + PlaceholderAPI templates for item lore lines |

Title IDs must be lowercase. An empty trailing lore line (`""`) adds visual spacing in the GUI before the status lore.

## menu.yml

Controls the title selection GUI layout and item configuration.

### Layout

```yaml
title: "<gradient:#fc782c:#FF9356><bold>YOUR TITLES</bold></gradient>"
title-slot: "A"
layout:
  - "BBBBBBBBB"
  - "BAAAAAAAB"
  - "BAAAAAAAB"
  - "BAAAAAAAB"
  - "BBBBBBBBB"
  - "BPBBSBBBN"
```

`title-slot` identifies which character in the layout corresponds to the paginated title slot (InvUI content list).

### Items

Each item key corresponds to a character in the layout.

**Static item:**

```yaml
B:
  type: static
  material: BLACK_STAINED_GLASS_PANE
  name: " "
  lore: []
```

**Navigation items:**

```yaml
P:
  type: previous-page
  available:
    material: ARROW
    name: "<#fc782c>Previous Page"
    lore:
      - "<gray>Go to the previous page."
  unavailable:
    material: GRAY_DYE
    name: "<gray>No Previous Page"
    lore:
      - "<dark_gray>You are already on the first page."
```

Navigation items have `available` and `unavailable` appearance variants.

**Selected title item:**

```yaml
S:
  type: selected-title
  selected:
    material: NAME_TAG
    name: "<#fc782c>Selected Title"
    lore:
      - ""
      - "<gray>Current: <white><selected_title>"
      - ""
      - "<yellow>Click to remove it."
  none:
    material: BARRIER
    name: "<red>No Title Selected"
    lore:
      - ""
      - "<gray>You currently have no title selected."
```

| Tag | Context |
| --- | --- |
| `<selected_title>` | Display name of the player's currently selected title |

### Title status lore

Appended to title icons in the GUI based on the player's relationship with each title:

```yaml
title-status:
  unlocked:
    - ""
    - "<green>Unlocked"
    - "<yellow>Click to select."
  selected:
    - ""
    - "<green>Selected"
  locked:
    - ""
    - "<red>Locked"
```

## MiniMessage and placeholders

All text fields support [MiniMessage](https://docs.advntr.dev/minimessage/) formatting. PlaceholderAPI placeholders (e.g. `%player_name%`) are resolved in player-aware contexts such as GUI rendering and lore.

The `%playertitles_title%` placeholder renders the raw MiniMessage prefix of the selected title. It does not apply PlaceholderAPI a second time.

## Reload behavior

Most configuration is hot-reloadable via the admin reload command. A failed reload leaves the previous configuration unchanged.

Database configuration is re-read during reload, but changing it requires a server restart because the active connection pool is not rebuilt.
