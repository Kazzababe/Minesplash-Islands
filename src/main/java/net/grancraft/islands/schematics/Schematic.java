package net.grancraft.islands.schematics;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.DataException;
import net.grancraft.islands.GrancraftIslands;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Schematic {
    private List<BlockData> blocks = new ArrayList<BlockData>();
    private Vector offset;
    private Vector origin;

    public Schematic(File file) {
        try {
            loadSchematic(file);
        } catch (IOException | DataException e) {
            e.printStackTrace();
            System.out.print("There was an error loading your schematic: " + file.getName());
        }
    }

    private void loadSchematic(File file) throws IOException, DataException {
        EditSession es = new EditSession(new BukkitWorld(GrancraftIslands.mainWorld), 999999999);
        CuboidClipboard cc = CuboidClipboard.loadSchematic(file);

        int width = cc.getWidth();
        int height = cc.getHeight();
        int length = cc.getLength();

        com.sk89q.worldedit.Vector vec = cc.getOffset();
        com.sk89q.worldedit.Vector vec2 = cc.getOrigin();
        offset = new Vector(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
        origin = new Vector(vec2.getBlockX(), vec2.getBlockY(), vec2.getBlockZ());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    com.sk89q.worldedit.Vector location = new com.sk89q.worldedit.Vector(x, y, z);
                    try {
                        if (cc.getBlock(location).getType() != 0) {
                            blocks.add(new BlockData(new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ()), cc.getBlock(location).getType(), (byte) cc.getBlock(location).getData()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public List<BlockData> getBlocks() {
        return blocks;
    }

    public Vector getOffset() {
        return offset;
    }

    public Vector getOrigin() {
        return origin;
    }
}