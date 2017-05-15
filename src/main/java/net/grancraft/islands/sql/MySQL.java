package net.grancraft.islands.sql;

import net.grancraft.islands.GrancraftIslands;
import net.grancraft.islands.islands.Island;
import net.grancraft.islands.players.IslandPlayer;
import net.grancraft.islands.utils.IslandUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQL {
    private final GrancraftIslands plugin;
    private Connection connection;

    public MySQL(GrancraftIslands plugin) {
        this.plugin = plugin;

        createTables();
        loadPlayers();
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection != null) {
            if (connection.isValid(1)) {
                return connection;
            }
            connection.close();
        }
        FileConfiguration config = plugin.getConfig();
        connection = DriverManager.getConnection(
                "jdbc:mysql://"
                        + config.getString("db_hostname")
                        + ":" + config.getInt("db_port")
                        + "/islands",
                config.getString("db_user"),
                config.getString("db_pass"));

        return connection;
    }

    private void createTables() {
        try (PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `users` (`id` MEDIUMINT(8) UNSIGNED NOT NULL AUTO_INCREMENT, `name` VARCHAR(16) NOT NULL, `uuid` VARCHAR(36) NOT NULL, PRIMARY KEY(`id`), UNIQUE(`uuid`))")) {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `islands` (`id` MEDIUMINT(8) UNSIGNED NOT NULL AUTO_INCREMENT, `owner` MEDIUMINT(8) UNSIGNED NOT NULL, `pvp` BOOLEAN DEFAULT 0, `mobs` BOOLEAN DEFAULT 0, `spawn` VARCHAR(50) NOT NULL, `location` VARCHAR(50) NOT NULL, `trusted` VARCHAR(255) DEFAULT '', `type` VARCHAR(15) NOT NULL, `phase` MEDIUMINT(2) UNSIGNED NOT NULL, PRIMARY KEY(`id`), UNIQUE(`owner`))")) {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadPlayers() {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM `users`")) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                GrancraftIslands.addPlayer(new IslandPlayer(
                        results.getInt("id"),
                        UUID.fromString(results.getString("uuid")),
                        results.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadIslands() {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM `islands`")) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                Island island = new Island(results);
                GrancraftIslands.getPlayer(island.getOwnerId()).setIsland(island);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayer(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final String name = event.getName();

        runAsync(() -> {
            IslandPlayer player = GrancraftIslands.getPlayer(uniqueId);
            if (player == null) {
                // Player does not exist in the db, add him
                try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO `users` (`name`, `uuid`) VALUES (?, ?)", new String[] {"id"})) {
                    statement.setString(1, name);
                    statement.setString(2, uniqueId.toString());
                    statement.execute();

                    ResultSet results = statement.getGeneratedKeys();
                    if (results.next()) {
                        GrancraftIslands.addPlayer(new IslandPlayer(
                                results.getInt(1),
                                uniqueId,
                                name
                        ));
                        GrancraftIslands.getPlayer(uniqueId).setOnline(true);
                    } else {
                        throw new SQLException("Error grabbing generated primary key 'id' for player '" + name + "'");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "There was an error adding you to our db, let an admin know so we can resolve the issue");
                }
            } else {
                // Player exists in the db just update his name if it's changed
                if (!player.getName().equals(name)) {
                    player.setName(name);
                    player.setOnline(true);
                    try (PreparedStatement statement = getConnection().prepareStatement("UPDATE `users` SET `name` = ? WHERE `id` = ?")) {
                        statement.setString(1, name);
                        statement.setInt(2, player.getId());
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                player.setOnline(true);
            }
        });
    }

    public void updateIsland(Island island) {
        runAsync(() -> {
            try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO `islands`  (`owner`, `pvp`, `mobs`, `spawn`, `location`, `trusted`, `type`, `phase`) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `pvp` = VALUES(`pvp`), `mobs` = VALUES(`mobs`), `spawn` = VALUES(`spawn`), `trusted` = VALUES(`trusted`), `type` = VALUES(`type`), `phase` = VALUES(`phase`)")) {
                statement.setInt(1, island.getOwnerId());
                statement.setBoolean(2, island.isPvpEnabled());
                statement.setBoolean(3, island.isMobsEnabled());
                statement.setString(4, IslandUtils.locationToString(island.getSpawnLocation()));
                statement.setString(5, IslandUtils.locationToString(island.getIslandLocation()));
                statement.setString(6, StringUtils.join(island.getTrustedPlayers(), ","));
                statement.setString(7, island.getIslandType().getName());
                statement.setInt(8, island.getPhase());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteIsland(Island island) {
        runAsync(() -> {
            try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM `islands` WHERE `owner` = ?")) {
                statement.setInt(1, island.getOwnerId());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
