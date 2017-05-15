package net.grancraft.islands.schematics;

import org.bukkit.util.Vector;

public class BlockData {
    private Vector location;
    private int blockId;
    private byte data;

    public BlockData(Vector location, int blockId, byte data) {
        this.location = location;
        this.blockId = blockId;
        this.data = data;
    }

    public Vector getLocation() {
        return location;
    }

    public int getBlockId() {
        return blockId;
    }

    public byte getData() {
        return data;
    }
}
