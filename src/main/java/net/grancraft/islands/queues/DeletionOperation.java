package net.grancraft.islands.queues;

import net.grancraft.islands.GrancraftIslands;
import net.grancraft.islands.islands.Island;
import net.grancraft.islands.players.IslandPlayer;
import net.grancraft.islands.schematics.BlockData;
import net.grancraft.islands.schematics.Schematic;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class DeletionOperation extends SchematicRunnable {
    private List<Location> blocks = new ArrayList();
    private int currentIndex;

    public DeletionOperation(Island island) {
        super(island);

        Vector min = island.getRegion().getMin();
        Vector max = island.getRegion().getMax();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Location loc = new Location(island.getIslandLocation().getWorld(), x, y, z);
                    if (loc.getBlock().getType() != Material.AIR) {
                        blocks.add(loc);
                    }
                }
            }
        }
    }

    @Override
    public void onEnd() {
        GrancraftIslands.getSchematicQueue().update();
        GrancraftIslands.getSql().deleteIsland(island);

        IslandPlayer player = GrancraftIslands.getPlayer(island.getOwnerId());
        player.setIsland(null);
        if (player.isOnline()) {
            player.sendMessage(GrancraftIslands.getMessage("finished_deleting"));
        }
    }

    @Override
    public void run() {
        if (currentIndex == 0) {
            IslandPlayer player = GrancraftIslands.getPlayer(island.getOwnerId());
            if (player.isOnline()) {
                player.sendMessage(GrancraftIslands.getMessage("deleted_queue"));

            }
        }
        for (int i = 0; i < SchematicQueue.INTERVAL_BLOCKS && currentIndex < blocks.size(); i++, currentIndex++) {
            blocks.get(currentIndex).getBlock().setTypeIdAndData(0, (byte) 0, false);
        }

        if (currentIndex >= blocks.size() - 1) {
            onEnd();
        }
    }
}
