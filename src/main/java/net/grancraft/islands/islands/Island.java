package net.grancraft.islands.islands;

import net.grancraft.islands.GrancraftIslands;
import net.grancraft.islands.players.IslandPlayer;
import net.grancraft.islands.queues.DeletionOperation;
import net.grancraft.islands.queues.PastingOperation;
import net.grancraft.islands.utils.IslandUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Island {
    private Location spawnLocation;
    private Location islandLocation;

    private int ownerId;
    private Cuboid region;
    private IslandType islandType;

    private int phase;
    private boolean mobsEnabled;
    private boolean pvpEnabled;

    private List<Integer> trustedPlayers = new ArrayList<Integer>();
    private Inventory inventory;

    public Island(ResultSet results) throws SQLException {
        this(results.getInt("owner"));

        spawnLocation = IslandUtils.stringToLocation(results.getString("spawn"));
        islandLocation = IslandUtils.stringToLocation(results.getString("location"));
        phase = results.getInt("phase");
        mobsEnabled = results.getBoolean("mobs");
        pvpEnabled = results.getBoolean("pvp");
        islandType = GrancraftIslands.getIslandType(results.getString("type"));

        String trustedString = results.getString("trusted");
        if (!trustedString.isEmpty()) {
            String[] trustedArray = trustedString.split(",");
            for (String trustedId : trustedArray) {
                trustedPlayers.add(Integer.valueOf(trustedId));
            }
        }
        updateInventory();
        updateRegion();

        if (phase == 1) {
            GrancraftIslands.getSchematicQueue().add(new PastingOperation(this, islandLocation, islandType.getSchematic()));
        } else if (phase == 2) {
            GrancraftIslands.getSchematicQueue().add(new DeletionOperation(this));
        } else if (phase == 3) {
            GrancraftIslands.getSchematicQueue().add(new DeletionOperation(this) {
                @Override
                public void onEnd() {
                    GrancraftIslands.getSchematicQueue().update();
                }
            });
            GrancraftIslands.getSchematicQueue().add(new PastingOperation(this, islandLocation, islandType.getSchematic()));
        }
    }

    public Island(int ownerId) {
        this.ownerId = ownerId;
        inventory = Bukkit.createInventory(null, 45, "Trusted Players");
    }

    public void setSpawnLocation(Location location) {
        spawnLocation = location;
    }

    public void setIslandLocation(Location location) {
        islandLocation = location;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public Location getIslandLocation() {
        return islandLocation;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getPhase() {
        return phase;
    }

    public Cuboid getRegion() {
        return region;
    }

    public void addTrusted(Integer id) {
        if (!trustedPlayers.contains(id)) {
            trustedPlayers.add(id);
        }
        updateInventory();
    }

    public void removeTrusted(Integer id) {
        if (trustedPlayers.contains(id)) {
            trustedPlayers.remove(id);
        }
        updateInventory();
    }

    public int getOwnerId() {
        return ownerId;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public boolean isMobsEnabled() {
        return mobsEnabled;
    }

    public List<Integer> getTrustedPlayers() {
        return trustedPlayers;
    }

    public IslandType getIslandType() {
        return islandType;
    }

    public void setPvpEnabled(boolean enabled) {
        pvpEnabled = enabled;
    }

    public void setMobsEnabled(boolean enabled) {
        mobsEnabled = enabled;
    }

    public void updateInventory() {
        inventory.clear();
        for (Integer id : trustedPlayers) {
            ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
            itemMeta.setOwner(GrancraftIslands.getPlayer(id).getName());
            itemMeta.setDisplayName(ChatColor.GREEN + GrancraftIslands.getPlayer(id).getName());
            item.setItemMeta(itemMeta);
            inventory.addItem(item);
        }
    }

    public void updateRegion() {
        Location min = islandLocation.clone();
        Location max = islandLocation.clone();
        int radius = GrancraftIslands.ISLAND_RADIUS;
        UUID uuid = GrancraftIslands.getPlayer(getOwnerId()).getUniqueId();
        if (uuid != null) {
            for (String perm : GrancraftIslands.getZPerms().getPlayerPermissions(null, null, uuid).keySet()) {
                try {
                    if (!perm.startsWith("island.radius")) continue;
                    int r = Integer.parseInt(perm.substring(perm.lastIndexOf(".") + 1, perm.length()));
                    radius = Math.max(radius, r);
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        min.subtract(radius, 0.0, radius);
        max.add(radius, 0.0, radius);
        min.setY(0.0);
        max.setY(256.0);

        region = new Cuboid(islandLocation.getWorld(), min.toVector(), max.toVector());
    }

    public void save() {
        GrancraftIslands.getSql().updateIsland(this);
    }

    public static Island createIsland(IslandPlayer player, IslandType type) {
        Island island = new Island(player.getId());

        int id = player.getId();
        Location location = new Location(GrancraftIslands.voidWorld, id % 30.0 * GrancraftIslands.ISLAND_SEPARATION, 160, id / 30.0 * GrancraftIslands.ISLAND_SEPARATION);
        island.setSpawnLocation(location);
        island.setIslandLocation(location);
        island.updateInventory();
        island.updateRegion();
        island.islandType = type;
        island.setPhase(1);

        GrancraftIslands.getSchematicQueue().add(new PastingOperation(island, location, type.getSchematic()));
        player.setIsland(island);

        island.save();

        return island;
    }
}
