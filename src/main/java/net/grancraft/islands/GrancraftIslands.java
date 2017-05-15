package net.grancraft.islands;

import com.greatmancode.craftconomy3.Common;
import com.greatmancode.craftconomy3.tools.interfaces.Loader;
import net.grancraft.islands.commands.Command;
import net.grancraft.islands.commands.IslandCommand;
import net.grancraft.islands.islands.Island;
import net.grancraft.islands.islands.IslandType;
import net.grancraft.islands.listeners.PlayerListeners;
import net.grancraft.islands.players.IslandPlayer;
import net.grancraft.islands.queues.SchematicQueue;
import net.grancraft.islands.sql.MySQL;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class GrancraftIslands extends JavaPlugin {
    private static MySQL sql;

    private static Map<UUID, IslandPlayer> players = new HashMap();
    private static Map<String, IslandType> islandTypes = new HashMap();
    private static Map<String, String> messages = new HashMap();
    private static SchematicQueue schematicQueue;
    private static ZPermissionsService zPerms;
    private static Common craftconomy;

    public static World mainWorld;
    public static World voidWorld;

    public static int ISLAND_RADIUS;
    public static int ISLAND_SEPARATION;

    @Override
    public void onLoad() {
        setupConfig();
        schematicQueue = new SchematicQueue(this);

        sql = new MySQL(this);
    }

    @Override
    public void onEnable() {
        zPerms = (ZPermissionsService) Bukkit.getServicesManager().load(ZPermissionsService.class);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Craftconomy3");
        if (plugin != null) {
            craftconomy = (Common) ((Loader) plugin).getCommon();
        }

        registerListener(new PlayerListeners());
        registerCommand(new IslandCommand(this));

        voidWorld = getServer().createWorld(WorldCreator.name(getConfig().getString("island_world")));
        mainWorld = getServer().createWorld(WorldCreator.name(getConfig().getString("return_world").equals("DEFAULT")?
                getServer().getWorlds().get(0).getName() :
                getConfig().getString("return_world")));

        FileConfiguration config = getConfig();
        for (String name : config.getConfigurationSection("types").getKeys(false)) {
            String schematicName = config.getString("types." + name + ".name");
            Integer cost = config.getInt("types." + name + ".cost");
            if (cost == null) {
                System.out.println("[GrancraftIslands] Island type '" + name + "' does not have a cost defined, setting to 0 for now...");
            }
            boolean isDefault = config.isSet("types." + name + ".default")? config.getBoolean("types." + name + ".default") : false;
            islandTypes.put(name.toLowerCase(), new IslandType(schematicName, name, cost == null? 0 : cost, isDefault));
        }
        sql.loadIslands();
    }

    @Override
    public void onDisable() {
        try {
            sql.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register all events in all the specified listener classes
     *
     * @param listeners Listeners to register
     */
    public void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Dynamically register a command
     *
     * @param command The command to register
     */
    public static void registerCommand(Command command) {
        CommandMap commandMap = null;
        try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getPlugin(GrancraftIslands.class).getName(), command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("db_hostname", "localhost");
        config.addDefault("db_port", 3306);
        config.addDefault("db_user", "root");
        config.addDefault("db_pass", "password");
        config.addDefault("island_world", "void");
        config.addDefault("return_world", "DEFAULT");
        config.addDefault("queue.interval", 10);
        config.addDefault("queue.blocks", 200);
        config.addDefault("properties.restart", 0);
        config.addDefault("properties.radius", 50);
        config.addDefault("properties.separation", 500);
        config.addDefault("properties.currency_name", "dollars");
        config.addDefault("messages.no_island", "You don't currently have an island, use '/island create' to make one now");
        config.addDefault("messages.no_trusted", "You haven't given anyone permissions to access your island");
        config.addDefault("messages.no_perms", "You don't have the permissions required to do this");
        config.addDefault("messages.just_trusted", "&p has just trusted you access to their island");
        config.addDefault("messages.just_untrusted", "&p has just revoked your access to their island");
        config.addDefault("messages.restarted", "Your island has been added to the queue and will restart soon");
        config.addDefault("messages.created", "Your island as been added to the queue and you will be notified when it starts generating");
        config.addDefault("messages.created_queue", "Your island is first in queue and is currently being generated");
        config.addDefault("messages.deleted", "Your island has been added to the queue and you will be notified when it starts deleting");
        config.addDefault("messages.deleted_queue", "Your island is first in queue is currently being deleted");
        config.addDefault("messages.already_exists", "You already have an island, use '/island tp' to go to it");
        config.addDefault("messages.already_trusted", "&p is already on your list of trusted people");
        config.addDefault("messages.not_trusted", "&p is not on your list of trusted people");
        config.addDefault("messages.not_trusted_by", "Your are not on &p's list of trusted people");
        config.addDefault("messages.never_joined", "This player has not been on the server before");
        config.addDefault("messages.target_no_island", "&p does not currently have an island");
        config.addDefault("messages.teleported_to", "&p has just teleported to your island");
        config.addDefault("messages.still_generating", "Your island is still generating, wait a while and try again");
        config.addDefault("messages.finished_generating", "Your island has finished generating, use '/island tp' to go to it");
        config.addDefault("messages.still_deleting", "Your island is currently being deleted");
        config.addDefault("messages.finished_deleting", "Your island has been finished deleting");
        config.addDefault("messages.pvp_enabled", "Pvp on your island is now enabled");
        config.addDefault("messages.pvp_disabled", "Pvp on your island is now disabled");
        config.addDefault("messages.mobs_enabled", "Mob spawning on your island is now enabled");
        config.addDefault("messages.mobs_disabled", "Mob spawning on your island is now disabled");
        config.addDefault("messages.too_many_trusted", "You've hit the limit for how many people can access your island");
        config.addDefault("messages.not_enough_money", "You don't have enough money to purchase an island");
        config.addDefault("messages.set_spawn", "Your islands spawn location is now set to your current location");
        config.addDefault("messages.not_islandtype", "The island '&p' does not exist");
        config.addDefault("messages.outside_island", "You are not currently on your island");
        config.options().copyDefaults(true);
        saveConfig();

        ISLAND_RADIUS = config.getInt("properties.radius");
        ISLAND_SEPARATION = config.getInt("properties.separation");

        for (String string : config.getConfigurationSection("messages").getKeys(false)) {
            messages.put(string, ChatColor.translateAlternateColorCodes('&', config.getString("messages." + string)));
        }
    }

    public static String getMessage(String node) {
        return messages.get(node);
    }

    public static ZPermissionsService getZPerms() {
        return zPerms;
    }

    public static Common getCraftconomy() {
        return craftconomy;
    }

    public static SchematicQueue getSchematicQueue() {
        return schematicQueue;
    }

    public static MySQL getSql() {
        return sql;
    }

    public static IslandType getIslandType(String name) {
        return islandTypes.get(name.toLowerCase());
    }

    public static IslandType getDefaultIslandType() {
        for (IslandType type : islandTypes.values()) {
            if (type.isDefaultType()) {
                return type;
            }
        }
        return islandTypes.values().iterator().next();
    }

    public static List<Island> getIslands() {
        List<Island> islands = players.values()
                .stream()
                .filter(player -> player.getIsland() != null)
                .map(IslandPlayer::getIsland)
                .collect(Collectors.toList());
        return islands;
    }

    public static void addPlayer(IslandPlayer player) {
        players.put(player.getUniqueId(), player);
    }

    public static IslandPlayer getPlayer(UUID uniqueId) {
        return players.get(uniqueId);
    }

    public static IslandPlayer getPlayer(Integer id) {
        return players.entrySet().stream()
                .filter(e -> e.getValue().getId() == id)
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static IslandPlayer getPlayer(String name, boolean ignore) {
        return players.entrySet().stream()
                .filter(e -> ignore?
                        e.getValue().getName().equalsIgnoreCase(name) :
                        e.getValue().getName().equals(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
