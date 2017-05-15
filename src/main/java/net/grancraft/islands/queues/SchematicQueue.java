package net.grancraft.islands.queues;

import net.grancraft.islands.GrancraftIslands;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SchematicQueue {
    public static int INTERVAL;
    public static int INTERVAL_BLOCKS;

    private GrancraftIslands plugin;
    private List<BukkitRunnable> operations = new ArrayList();

    public SchematicQueue(GrancraftIslands plugin) {
        this.plugin = plugin;

        INTERVAL = plugin.getConfig().getInt("queue.interval");
        INTERVAL_BLOCKS = plugin.getConfig().getInt("queue.blocks");
    }

    public void update() {
        operations.get(0).cancel();
        operations.remove(0);

        if (!operations.isEmpty()) {
            operations.get(0).runTaskTimer(plugin, INTERVAL, INTERVAL);
        }
    }

    public void add(BukkitRunnable operation) {
        if (operations.isEmpty()) {
            operation.runTaskTimer(plugin, INTERVAL, INTERVAL);
        }
        operations.add(operation);
    }
}
