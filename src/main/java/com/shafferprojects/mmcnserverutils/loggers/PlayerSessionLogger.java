package com.shafferprojects.mmcnserverutils.loggers;

import com.mojang.authlib.GameProfile;
import com.shafferprojects.mmcnserverutils.database.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerSessionLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSessionLogger.class);

    public static void logPlayer(GameProfile profile, boolean online) {
        try (Connection conn = DB.get()) {
            if (conn == null) {
                LOGGER.error("[MySQL] No active database connection.");
                return;
            }

            String uuid = profile.getId().toString();
            String server = DB.SERVER_NAME;
            Timestamp now = Timestamp.from(Instant.now());

            String sql = """
            INSERT INTO player_sessions (uuid, server, first_joined, last_seen, status)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                last_seen = VALUES(last_seen),
                status = VALUES(status)
        """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid);
                stmt.setString(2, server);
                stmt.setTimestamp(3, now);
                stmt.setTimestamp(4, now);
                stmt.setString(5, online ? "ONLINE" : "OFFLINE");
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("[MySQL] Failed to log player session", e);
        }
    }

    public static void markAllOfflineForServer(String serverName) {
        String sql = "UPDATE player_sessions SET status = 'OFFLINE' WHERE server = ? AND status = 'ONLINE'";

        try (Connection conn = DB.get(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serverName);
            int updated = stmt.executeUpdate();
            LOGGER.info("[MMCN UTILS] Reset {} lingering ONLINE sessions to OFFLINE for server '{}'", updated, serverName);
        } catch (SQLException e) {
            LOGGER.error("[MySQL] Failed to mark previous sessions as OFFLINE", e);
        }
    }

}
