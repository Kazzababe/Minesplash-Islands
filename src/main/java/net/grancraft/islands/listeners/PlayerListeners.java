package net.grancraft.islands.listeners;

import net.grancraft.islands.GrancraftIslands;
import net.grancraft.islands.islands.Island;
import net.grancraft.islands.players.IslandPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;

public class PlayerListeners implements Listener {
    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        GrancraftIslands.getSql().updatePlayer(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GrancraftIslands.getPlayer(event.getPlayer().getUniqueId()).setOnline(false);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getView().getTopInventory().getName().equals("Trusted players")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory() == null) {
            return;
        }

        if (event.getView().getTopInventory().getName().equals("Trusted players")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getLocation().getWorld().equals(GrancraftIslands.voidWorld)) {
            for (Island island : GrancraftIslands.getIslands()) {
                if (island.getRegion().isLocationInside(event.getLocation())) {
                    if (!island.isMobsEnabled()) {
                        event.setCancelled(true);
                        break;
                    }
                    island.getRegion().entities.add(event.getEntity());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            for (Island island : GrancraftIslands.getIslands()) {
                if (island.getRegion().isLocationInside(event.getEntity().getLocation())) {
                    if (!island.isPvpEnabled()) {
                        event.setCancelled(true);
                    }
                    break;
                }

            }

        } else if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player &&
                ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            for (Island island : GrancraftIslands.getIslands()) {
                if (island.getRegion().isLocationInside(event.getEntity().getLocation())) {
                    if (island.isPvpEnabled()) {
                        event.setCancelled(true);
                    }
                    break;
                }
            }
        }
    }

    private void checkIsland(Cancellable event, Location location, IslandPlayer player) {
        if (location.getWorld().equals(GrancraftIslands.voidWorld)) {
            if (player.getPlayer().hasPermission("island.admin")) {
                return;
            }
            for (Island island : GrancraftIslands.getIslands()) {
                if (island.getRegion().isLocationInside(location)) {
                    if (island.equals(player.getIsland())) {
                        event.setCancelled(island.getPhase() == 2);
                        return;
                    }
                    if (!island.getTrustedPlayers().contains(player.getId())) {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        IslandPlayer player = GrancraftIslands.getPlayer(event.getPlayer().getUniqueId());
        checkIsland(event, event.getBlock().getLocation(), player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        IslandPlayer player = GrancraftIslands.getPlayer(event.getPlayer().getUniqueId());
        checkIsland(event, event.getBlock().getLocation(), player);
    }

    @EventHandler
    public void onBucketFillEvent(PlayerBucketFillEvent event) {
        IslandPlayer player = GrancraftIslands.getPlayer(event.getPlayer().getUniqueId());
        if (event.getBlockClicked() != null) {
            Block block = event.getBlockClicked();
            checkIsland(event, block.getLocation(), player);
        }
    }

    @EventHandler
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        IslandPlayer player = GrancraftIslands.getPlayer(event.getPlayer().getUniqueId());
        if (event.getBlockClicked() != null) {
            Block block = event.getBlockClicked();
            checkIsland(event, block.getLocation(), player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        IslandPlayer player = GrancraftIslands.getPlayer(event.getPlayer().getUniqueId());
        if (event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            checkIsland(event, block.getLocation(), player);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        IslandPlayer player = null;
        if (event.getRemover() instanceof Player) {
            player = GrancraftIslands.getPlayer(event.getRemover().getUniqueId());
        } else if (event.getRemover() instanceof Projectile &&
                ((Projectile) event.getRemover()).getShooter() instanceof Player) {
            player = GrancraftIslands.getPlayer(((Player) ((Projectile) event.getRemover()).getShooter()).getUniqueId());
        }

        if (player != null && player.getPlayer() != null) {
            checkIsland(event, event.getEntity().getLocation(), player);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Island island : GrancraftIslands.getIslands()) {
            if (island.getPhase() != 0 &&
                    island.getRegion().isLocationInside(event.getChunk().getBlock(0, 0, 0).getLocation())) {
                event.setCancelled(true);
                break;
            }
        }
    }

}
