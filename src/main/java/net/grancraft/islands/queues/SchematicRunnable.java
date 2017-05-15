package net.grancraft.islands.queues;

import net.grancraft.islands.islands.Island;
import net.grancraft.islands.schematics.Schematic;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class SchematicRunnable extends BukkitRunnable {
    protected Island island;
    protected int currentIndex;

    public SchematicRunnable(Island island) {
        this.island = island;
    }

    abstract void onEnd();
}
