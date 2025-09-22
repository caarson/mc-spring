# Spring Bounce Pad Plugin for Minecraft

A configurable bounce pad plugin for Minecraft 1.21.8 (Paper/Spigot) that adds progressive bounce chains, WorldGuard integration, safe landing, and PlaceholderAPI leaderboards.

## Features

- **Configurable Bounce Pads**: Define bounce materials and progressive chain levels
- **WorldGuard Integration**: Restrict bouncing to specific regions with custom flags
- **Safe Landing**: Prevent fall damage after bouncing with configurable timeout
- **Progressive Chains**: Bounce strength increases with consecutive bounces on same material
- **PlaceholderAPI Support**: Track bounce statistics and leaderboards
- **Permission Control**: `spring.use` for players, `spring.admin` for commands
- **Multiple Storage Options**: SQLite (default) or JSON storage

## Installation

1. Download the `Spring-1.0.jar` from the releases page
2. Place the JAR file in your server's `plugins/` folder
3. Restart your server
4. Configure the plugin using `/spring reload`

## Configuration

The plugin creates a `config.json` file in `plugins/Spring/` with the following structure:

```json
{
  "enabled": true,
  "locale": "en-US",
  "worlds": {
    "mode": "whitelist",
    "list": ["world", "nether"]
  },
  "region": {
    "requireBounceFlag": true,
    "allowRegionBypassPermission": false
  },
  "safety": {
    "enabled": true,
    "timeoutTicks": 20
  },
  "placeholderapi": {
    "topSize": 10,
    "weekStart": "Monday",
    "timezone": "America/Los_Angeles"
  },
  "levels": [
    {
      "name": "gentle",
      "verticalVelocity": 0.5,
      "horizontalMultiplier": 0.3,
      "anglePreservation": true
    }
  ],
  "materials": ["SLIME_BLOCK"],
  "bounceChains": {
    "SLIME_BLOCK": [0, 1, 2]
  },
  "debug": {
    "loggingEnabled": false
  }
}
```

## Commands

- `/spring reload` - Reload configuration (requires `spring.admin`)
- `/spring list` - Show plugin status (requires `spring.admin`)

## Permissions

- `spring.use` - Allows players to use bounce pads
- `spring.admin` - Administrative commands

## WorldGuard Integration

If WorldGuard is installed, you can restrict bouncing to specific regions:
- Set `requireBounceFlag: true` in config
- Use the `spring-bounce` flag in WorldGuard regions
- Players with `spring.admin` can bypass region restrictions if `allowRegionBypassPermission: true`

## PlaceholderAPI Placeholders

The plugin provides the following placeholders:

### Player Statistics
- `%spring_bounces_alltime%` - Total bounces
- `%spring_bounces_month%` - Bounces this month
- `%spring_bounces_week%` - Bounces this week
- `%spring_bounces_day%` - Bounces today

### Leaderboards
- `%spring_top_name_X_alltime%` - Player name at position X (1-10)
- `%spring_top_count_X_alltime%` - Bounce count at position X
- Replace `alltime` with `month`, `week`, or `day` for different time periods

## Bounce Chain Mechanics

- Each material can have multiple bounce levels
- Chain level increases with consecutive bounces on the same material
- Chain resets when player steps off the material or sneaks
- Different levels can have different physics properties

## Dependencies

### Required
- **Paper/Spigot 1.21.8+** - [Download Paper](https://papermc.io/downloads) | [Download Spigot](https://www.spigotmc.org/wiki/spigot-installation/)

### Optional (for full functionality)
- **PlaceholderAPI** - [Download](https://www.spigotmc.org/resources/placeholderapi.6245/) - Required for bounce statistics and leaderboards
- **WorldGuard 7.x** - [Download](https://enginehub.org/worldguard/) - Required for region-based bounce restrictions
- **LuckPerms** - [Download](https://luckperms.net/download) - Enhanced permission management (falls back to Bukkit permissions if not present)

### Storage Dependencies (automatically included)
- **SQLite JDBC** - Embedded database for statistics (included in JAR)
- **JSON Support** - Built-in for JSON storage option

## Building from Source

1. Clone the repository
2. Navigate to the `spring/` directory
3. Run `./gradlew build` (or `gradlew.bat build` on Windows)
4. Find the JAR in `build/libs/Spring-1.0.jar`

## Support

For issues or feature requests, please create an issue on the GitHub repository.

## License

This plugin is provided as-is for educational and personal use.
