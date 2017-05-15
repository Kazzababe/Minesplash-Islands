package net.grancraft.islands.islands;

import net.grancraft.islands.GrancraftIslands;
import net.grancraft.islands.schematics.Schematic;

import java.io.File;

public class IslandType {
    private String name;
    private String schematicName;
    private int cost;
    private boolean defaultType = false;
    private Schematic schematic;

    public IslandType(String schematicName, String name, int cost) {
        this.schematicName = schematicName;
        this.name = name;
        this.cost = cost;
        schematic = new Schematic(new File(GrancraftIslands.getPlugin(GrancraftIslands.class).getDataFolder(), schematicName + ".schematic"));
    }

    public IslandType(String schematicName, String name, int cost, boolean defaultType) {
        this(schematicName, name, cost);
        this.defaultType = defaultType;
    }

    public String getName() {
        return name;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public int getCost() {
        return cost;
    }

    public boolean isDefaultType() {
        return defaultType;
    }

    public Schematic getSchematic() {
        return schematic;
    }
}