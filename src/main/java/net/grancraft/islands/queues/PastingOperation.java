package net.grancraft.islands.queues;

import com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import net.grancraft.islands.GrancraftIslands;
import net.grancraft.islands.islands.Island;
import net.grancraft.islands.players.IslandPlayer;
import net.grancraft.islands.schematics.BlockData;
import net.grancraft.islands.schematics.Schematic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class PastingOperation extends SchematicRunnable {
    private Location location;
    private Schematic schematic;
    public PastingOperation(Island island, Location location, Schematic schematic) {
        super(island);

        this.location = location;
        this.schematic = schematic;
    }

    @Override
    void onEnd() {
        IslandPlayer player = GrancraftIslands.getPlayer(island.getOwnerId());
        if (player.isOnline()) {
            player.sendMessage(GrancraftIslands.getMessage("finished_generating"));
        }
        island.setPhase(0);
        island.save();
        GrancraftIslands.getSchematicQueue().update();
    }

    @Override
    public void run() {
        if (currentIndex == 0) {
            IslandPlayer player = GrancraftIslands.getPlayer(island.getOwnerId());
            if (player.isOnline()) {
                player.sendMessage(GrancraftIslands.getMessage("created_queue"));
            }
        }

        Vector offset = schematic.getOffset();
        for (BlockData data : schematic.getBlocks()) {
            Vector loc = data.getLocation();
            Location blockLoc = new Location(GrancraftIslands.voidWorld, location.getBlockX() + loc.getBlockX() + offset.getBlockX(),
                    location.getBlockY() + loc.getBlockY() + offset.getBlockY(),
                    location.getBlockZ() + loc.getBlockZ() + offset.getBlockZ());
            blockLoc.getChunk().load(false);
        }

        for (int i = 0; i < SchematicQueue.INTERVAL_BLOCKS && currentIndex < schematic.getBlocks().size(); i++, currentIndex++) {
            BlockData data = schematic.getBlocks().get(currentIndex);
            Vector loc = data.getLocation();
            Block block = new Location(GrancraftIslands.voidWorld, location.getBlockX() + loc.getBlockX() + offset.getBlockX(),
                    location.getBlockY() + loc.getBlockY() + offset.getBlockY(),
                    location.getBlockZ() + loc.getBlockZ() + offset.getBlockZ())
                    .getBlock();
            block.setTypeIdAndData(data.getBlockId(), data.getData(), false);
        }

        if (currentIndex >= schematic.getBlocks().size() - 1) {
            onEnd();
        }
    }
}
