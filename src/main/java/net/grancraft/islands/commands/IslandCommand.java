package net.grancraft.islands.commands;

import com.greatmancode.craftconomy3.Cause;
import com.greatmancode.craftconomy3.account.Account;
import com.sun.org.apache.xpath.internal.Arg;
import net.grancraft.islands.GrancraftIslands;
import net.grancraft.islands.islands.Island;
import net.grancraft.islands.islands.IslandType;
import net.grancraft.islands.players.IslandPlayer;
import net.grancraft.islands.queues.DeletionOperation;
import net.grancraft.islands.queues.PastingOperation;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class IslandCommand extends Command {
    private String currencyName;
    private int restartCost;

    public IslandCommand(GrancraftIslands plugin) {
        super("island");
        currencyName = plugin.getConfig().getString("properties.currency_name");
        restartCost = plugin.getConfig().getInt("properties.restart");

        registerArgument(new HelpCommand(), true, true, "help");
        registerArgument(new HelpCommand(), false, true, "help");
        registerArgument(new CreateCommand(), false, true, "create");
        registerArgument(new CreateCommand(), true, true, "create");
        registerArgument(new DeleteCommand(), false, true, "delete");
        registerArgument(new TeleportCommand(), true, true, "tp");
        registerArgument(new TeleportCommand(), false, true, "tp");
        registerArgument(new ExitCommand(), false, true, "exit");
        registerArgument(new TrustCommand(), true, true, "trust");
        registerArgument(new UntrustCommand(), true, true, "untrust");
        registerArgument(new SetSpawnCommand(), false, true, "setspawn");
        registerArgument(new SetSpawnCommand(), false, true, "settp");
        registerArgument(new TogglePvpCommand(), true, true, "pvp");
        registerArgument(new ToggleMobsCommand(), true, true, "mobs");
        registerArgument(new UpdateIslandCommand(), false, true, "update");
        registerArgument(new ViewTrustedCommand(), false, true, "view");
        registerArgument(new RestartCommand(), false, true, "restart");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        // IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
        return true;
    }

    private boolean hasPermissions(IslandPlayer islandPlayer, String node) {
        if (islandPlayer.isOnline()) {
            Player player = islandPlayer.getPlayer();
            if (!player.hasPermission(node)) {
                islandPlayer.sendMessage(GrancraftIslands.getMessage("no_perms"));
            }
            return player.hasPermission(node);
        }
        return false;
    }

    class RestartCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() == 3 || island.getPhase() == 2) {
                    player.sendMessage(GrancraftIslands.getMessage("still_deleting"));
                } else if (island.getPhase() == 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else if (island.getPhase() == 0) {
                    Account account = GrancraftIslands.getCraftconomy().getAccountManager().getAccount(player.getName(), false);
                    double balance = account.getBalance(Account.getWorldGroupOfPlayerCurrentlyIn(player.getName()), currencyName);
                    if (balance >= restartCost) {
                        island.setPhase(3);
                        GrancraftIslands.getSchematicQueue().add(new DeletionOperation(island) {
                            @Override
                            public void onEnd() {
                                GrancraftIslands.getSchematicQueue().update();
                            }
                        });
                        GrancraftIslands.getSchematicQueue().add(new PastingOperation(island, island.getIslandLocation(), island.getIslandType().getSchematic()));
                    }
                    account.set(balance - restartCost, GrancraftIslands.mainWorld.getName(), currencyName, Cause.PAYMENT, "Restarted an island");
                }
            }
            return true;
        }
    }

    class ViewTrustedCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() == 3 || island.getPhase() == 2) {
                    player.sendMessage(GrancraftIslands.getMessage("still_deleting"));
                } else if (island.getPhase() == 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else if (island.getPhase() == 0) {
                    player.getPlayer().openInventory(island.getInventory());
                }
            }
            return true;
        }
    }

    class UpdateIslandCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() == 3 || island.getPhase() == 2) {
                    player.sendMessage(GrancraftIslands.getMessage("still_deleting"));
                } else if (island.getPhase() == 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else if (island.getPhase() == 0) {
                    island.updateRegion();
                }
            }
            return true;
        }
    }

    class TogglePvpCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (player.getIsland() == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() >= 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else if (arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("off")) {
                    island.setPvpEnabled(arg.equalsIgnoreCase("on"));
                    island.save();
                    player.sendMessage(arg.equalsIgnoreCase("on") ? GrancraftIslands.getMessage("pvp_enabled") : GrancraftIslands.getMessage("pvp_disabled"));
                } else {
                    player.sendMessage(arg + " is not an accepted argument of '/island pvp [on|off]'");
                }
            }
            return true;
        }
    }

    class ToggleMobsCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (player.getIsland() == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() >= 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else if (arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("off")) {
                    island.setMobsEnabled(arg.equalsIgnoreCase("on"));
                    island.save();
                    player.sendMessage(arg.equalsIgnoreCase("on") ? GrancraftIslands.getMessage("mobs_enabled") : GrancraftIslands.getMessage("mobs_disabled"));

                    if (!island.isMobsEnabled()) {
                        Iterator<LivingEntity> iterator = island.getRegion().entities.iterator();
                        while (iterator.hasNext()) {
                            LivingEntity entity = iterator.next();
                            if (entity == null) {
                                iterator.remove();
                                continue;
                            }
                            if (!(entity instanceof Monster)) {
                                continue;
                            }
                            entity.remove();
                            iterator.remove();
                        }
                    }
                } else {
                    player.sendMessage(arg + " is not an accepted argument of '/island mobs [on|off]'");
                }
            }
            return true;
        }
    }

    class SetSpawnCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (player.getIsland() == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() >= 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else {
                    if (island.getRegion().isLocationInside(player.getPlayer().getLocation())) {
                        island.setSpawnLocation(player.getPlayer().getLocation());
                        island.save();
                        player.sendMessage(GrancraftIslands.getMessage("set_spawn"));
                    } else {
                        player.sendMessage(GrancraftIslands.getMessage("outside_island"));
                    }
                }
            }
            return true;
        }
    }

    class TrustCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() >= 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else {
                    IslandPlayer targetPlayer = GrancraftIslands.getPlayer(arg, true);
                    if (targetPlayer != null) {
                        Integer id = targetPlayer.getId();
                        if (island.getTrustedPlayers().contains(id)) {
                            player.sendMessage(GrancraftIslands.getMessage("already_trusted").replaceAll("&p", targetPlayer.getName()));
                        } else if (island.getTrustedPlayers().size() >= 54) {
                            player.sendMessage(GrancraftIslands.getMessage("too_many_trusted"));
                        } else {
                            island.addTrusted(id);
                            island.save();
                            if (targetPlayer.isOnline()) {
                                targetPlayer.sendMessage(GrancraftIslands.getMessage("just_trusted").replaceAll("&p", player.getName()));
                            }
                        }
                    } else {
                        player.sendMessage(GrancraftIslands.getMessage("never_joined"));
                    }
                }
            }
            return true;
        }
    }

    class UntrustCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() >= 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else {
                    IslandPlayer targetPlayer = GrancraftIslands.getPlayer(arg, true);
                    if (targetPlayer != null) {
                        Integer id = targetPlayer.getId();
                        if (island.getTrustedPlayers().contains(id)) {
                            island.removeTrusted(id);
                            island.save();
                            if (targetPlayer.isOnline()) {
                                targetPlayer.sendMessage(GrancraftIslands.getMessage("just_untrusted").replaceAll("&p", player.getName()));
                                if (island.getRegion().isLocationInside(targetPlayer.getPlayer().getLocation())) {
                                    if (targetPlayer.getIsland() != null) {
                                        targetPlayer.getPlayer().teleport(targetPlayer.getIsland().getSpawnLocation());
                                    } else {
                                        targetPlayer.getPlayer().teleport(GrancraftIslands.mainWorld.getSpawnLocation());
                                    }
                                }
                            }
                        } else {
                            player.sendMessage(GrancraftIslands.getMessage("not_trusted_by").replaceAll("&p", targetPlayer.getName()));
                        }
                    } else {
                        player.sendMessage(GrancraftIslands.getMessage("never_joined"));
                    }
                }
            }
            return true;
        }
    }

    class ExitCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island == null) {
                    if (player.getPlayer().getWorld().equals(GrancraftIslands.voidWorld)) {
                        player.getPlayer().teleport(GrancraftIslands.mainWorld.getSpawnLocation());
                    } else {
                        player.sendMessage(GrancraftIslands.getMessage("no_island"));
                    }
                } else if (island.getRegion().isLocationInside(player.getPlayer().getLocation())) {
                    player.getPlayer().teleport(GrancraftIslands.mainWorld.getSpawnLocation());
                }
            }
            return true;
        }
    }

    class TeleportCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = arg == null?
                        player.getIsland() :
                        GrancraftIslands.getPlayer(arg, true) == null?
                            null :
                            GrancraftIslands.getPlayer(arg, true).getIsland();
                if (island == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() == 0) {
                    if (!island.getRegion().isLocationInside(island.getSpawnLocation())) {
                        island.setSpawnLocation(island.getIslandLocation());
                    }
                    if ((arg != null && player.getPlayer().hasPermission("island.admin"))) {
                        player.getPlayer().teleport(island.getSpawnLocation());
                    } else {
                        if (arg != null) {
                            if (island.getTrustedPlayers().contains(new Integer(player.getId())) || island.getOwnerId() == player.getId()) {
                                player.getPlayer().teleport(island.getSpawnLocation());
                            } else {
                                player.sendMessage(GrancraftIslands.getMessage("not_trusted_by").replaceAll("&p", arg.toLowerCase()));
                            }
                        } else {
                            player.getPlayer().teleport(island.getSpawnLocation());
                        }
                    }
                } else {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                }
            }
            return true;
        }
    }

    class HelpCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                if (arg != null && arg.equalsIgnoreCase("2")) {
                    player.sendMessage(ChatColor.GREEN + "---------------- " + ChatColor.WHITE + "Island Help [2/2]" + ChatColor.GREEN + " ----------------");
                    player.sendMessage(ChatColor.GOLD + "/island untrust <name> " + ChatColor.GRAY + "Take someone's access to your island");
                    player.sendMessage(ChatColor.GOLD + "/island tp <name> " + ChatColor.GRAY + "Teleport to somebody else's island");
                    player.sendMessage(ChatColor.GOLD + "/island pvp <on|off> " + ChatColor.GRAY + "Enable or disable pvp on your island");
                    player.sendMessage(ChatColor.GOLD + "/island mobs <on|off> " + ChatColor.GRAY + "Enable or disable mob spawning on your island");

                    if (player.getPlayer().hasPermission("island.admin")) {
                        player.sendMessage(ChatColor.GOLD + "/island delete <name> " + ChatColor.GRAY + "Delete a players island");
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "---------------- " + ChatColor.WHITE + "Island Help [1/2]" + ChatColor.GREEN + " ----------------");
                    player.sendMessage(ChatColor.GOLD + "/island create [type] " + ChatColor.GRAY + "Create an island");
                    player.sendMessage(ChatColor.GOLD + "/island tp " + ChatColor.GRAY + "Teleport to your island");
                    player.sendMessage(ChatColor.GOLD + "/island exit " + ChatColor.GRAY + "Teleport to the main world or your island if you're on someone elses");
                    player.sendMessage(ChatColor.GOLD + "/island view " + ChatColor.GRAY + "View people you've trusted access to your island");
                    player.sendMessage(ChatColor.GOLD + "/island update " + ChatColor.GRAY + "Update the building radius of your island");
                    player.sendMessage(ChatColor.GOLD + "/island delete " + ChatColor.GRAY + "Delete your island, no refunds");
                    player.sendMessage(ChatColor.GOLD + "/island restart " + ChatColor.GRAY + "Delete and recreate your island for a fee");
                    player.sendMessage(ChatColor.GOLD + "/island trust <name> " + ChatColor.GRAY + "Give someone access to your island");
                }
            }
            return true;
        }
    }

    class CreateCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island != null) {
                    player.sendMessage(GrancraftIslands.getMessage("already_exists"));
                } else {
                    IslandType type = arg == null?
                            GrancraftIslands.getDefaultIslandType() :
                            GrancraftIslands.getIslandType(arg.toLowerCase());
                    if (type != null) {
                        double islandCost = type.getCost();
                        Account account = GrancraftIslands.getCraftconomy().getAccountManager().getAccount(player.getName(), false);
                        double balance = account.getBalance(Account.getWorldGroupOfPlayerCurrentlyIn(player.getName()), currencyName);
                        if (balance >= islandCost) {
                            Island.createIsland(player, type);
                            player.sendMessage(GrancraftIslands.getMessage("created"));
                            account.set(balance - islandCost, GrancraftIslands.mainWorld.getName(), currencyName, Cause.PAYMENT, "Purchased an island");
                        } else {
                            player.sendMessage(GrancraftIslands.getMessage("not_enough_money").replaceAll("&s", "500"));
                        }
                    } else {
                        player.sendMessage(GrancraftIslands.getMessage("not_islandtype").replaceAll("&p", arg));
                    }
                }
            }
            return true;
        }
    }

    class DeleteCommand implements ArgumentRunnable {
        @Override
        public boolean executeArgument(CommandSender sender, String arg) {
            IslandPlayer player = GrancraftIslands.getPlayer(((Player) sender).getUniqueId());
            if (hasPermissions(player, "island.player")) {
                Island island = player.getIsland();
                if (island == null) {
                    player.sendMessage(GrancraftIslands.getMessage("no_island"));
                } else if (island.getPhase() == 3 || island.getPhase() == 2) {
                    player.sendMessage(GrancraftIslands.getMessage("still_deleting"));
                } else if (island.getPhase() == 1) {
                    player.sendMessage(GrancraftIslands.getMessage("still_generating"));
                } else if (island.getPhase() == 0) {
                    if (island.getRegion().isLocationInside(player.getPlayer().getLocation())) {
                        player.getPlayer().teleport(GrancraftIslands.mainWorld.getSpawnLocation());
                    }
                    player.sendMessage(GrancraftIslands.getMessage("deleted"));
                    island.setPhase(2);
                    island.save();
                    GrancraftIslands.getSchematicQueue().add(new DeletionOperation(island));
                }
            }
            return true;
        }
    }
}
