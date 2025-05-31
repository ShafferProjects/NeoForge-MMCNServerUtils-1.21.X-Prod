# MMCN Server Utils

A custom server-side utility mod for [The ModdedMC Network](https://moddedmc.net), built using NeoForge 21.1.172 for Minecraft 1.21.1.

This mod provides essential infrastructure tools and staff utilities designed to improve network-wide server moderation, player management, and cross-server synchronization.

## Key Features

- ✅ **Rule Enforcement System**  
  Freeze players on first join and require rule agreement before allowing gameplay.

- ✅ **Player Session Logging**  
  Logs first join, last seen, and online/offline status per server in a centralized MySQL database.

- ✅ **Crash Recovery Support**  
  Automatically resets all lingering player sessions to `OFFLINE` for the current server on restart.

- ✅ **Admin Command Utilities**
    - `/showrules <player>` — force a player to re-read and re-agree to the rules  
      This also logs a warning entry in the database with the admin who issued the command.

- ✅ **Player Commands**
    - `/agree` and `/decline` — clickable command integration with in-game prompts

## Database Setup

This mod requires a MySQL database with the following schema:

```sql
CREATE TABLE IF NOT EXISTS rules_agreed (
    uuid CHAR(36) PRIMARY KEY,
    agreed_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS player_sessions (
    uuid CHAR(36) NOT NULL,
    server VARCHAR(64) NOT NULL,
    first_joined TIMESTAMP NULL,
    last_seen TIMESTAMP NULL,
    status VARCHAR(16) NOT NULL,
    PRIMARY KEY (uuid, server)
);

CREATE TABLE IF NOT EXISTS warnings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user CHAR(36) NOT NULL,
    warnedBy CHAR(36) NOT NULL,
    issuedAt DATETIME NOT NULL,
    reason TEXT NOT NULL
);
```

> ⚠️ **Important:** You must edit the `DB.java` file to set your database connection details:
- Host
- Port
- Database name
- Username & password
- Server name (`SERVER_NAME` constant)

This allows you to run multiple servers using the same database without conflicts.

## Built With

- Minecraft 1.21.1
- NeoForge 21.1.172
- Java 21
