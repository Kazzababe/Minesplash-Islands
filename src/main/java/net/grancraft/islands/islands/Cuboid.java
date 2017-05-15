package net.grancraft.islands.islands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Cuboid {
    private World world;
    private Vector min;
    private Vector max;

    public Set<LivingEntity> entities = new HashSet();

    public Cuboid(World world, Vector v1, Vector v2) {
        this.world = world;

        int x1 = v1.getBlockX();
        int y1 = v1.getBlockY();
        int z1 = v1.getBlockZ();

        int x2 = v2.getBlockX();
        int y2 = v2.getBlockY();
        int z2 = v2.getBlockZ();

        min = new Vector(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
        max = new Vector(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public boolean isInside(Vector location) {
        return location.isInAABB(min, max);
    }

    public boolean isLocationInside(Location location) {
        return location.getWorld().equals(world) && isInside(location.toVector());
    }

    public World getWorld() {
        return world;
    }

    public Vector getMin() {
        return min;
    }

    public Vector getMax() {
        return max;
    }
}