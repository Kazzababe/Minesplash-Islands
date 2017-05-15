package net.grancraft.islands.players;

import net.grancraft.islands.islands.Island;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class IslandPlayer {
    private int id;
    private UUID uniqueId;
    private String name;

    private boolean isOnline;
    private Island island;

    public IslandPlayer(int id, UUID uniqueId, String name) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.name = name;
    }

    public Player getPlayer() {
        Player player = Bukkit.getPlayer(uniqueId);
        isOnline = player != null;

        return player;
    }

    public boolean isOnline() {
        getPlayer();
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getId() {
        return id;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Island getIsland() {
        return island;
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    public void sendMessage(Object object) {
        if (isOnline) {
            getPlayer().sendMessage(object.toString());
        }
    }
}
