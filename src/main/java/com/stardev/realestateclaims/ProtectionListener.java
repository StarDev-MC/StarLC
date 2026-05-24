package com.stardev.realestateclaims;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public final class ProtectionListener implements Listener {
    private final RealEstateClaims plugin;

    public ProtectionListener(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        // Prevent breaking of claim signs unless player has admin permission
        if (block.getState() instanceof Sign) {
            Claim claim = plugin.getClaimManager().getClaimBySignLocation(block.getLocation());
            if (claim != null && !event.getPlayer().hasPermission("realestate.admin")) {
                event.setCancelled(true);
                return;
            }
        }
        if (canUse(event.getPlayer(), block.getLocation())) {
            event.setCancelled(false);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (canUse(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(false);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!canUse(event.getPlayer(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            Location location = event.getInventory().getLocation();
            if (location != null && !canUse(player, location)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!canUse(event.getPlayer(), event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!canUse(event.getPlayer(), event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() == null) {
            return;
        }
        if (!canUse(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player target && event.getDamager() instanceof Player damager) {
            Claim claim = plugin.getClaimManager().getClaimAt(target.getLocation());
            if (claim != null && !claim.isMember(damager.getUniqueId()) && !damager.hasPermission("realestate.bypass")) {
                event.setCancelled(true);
            }
        }
        if (event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.ITEM_FRAME) {
            if (event.getDamager() instanceof Player player && !canUse(player, event.getEntity().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            Claim claim = plugin.getClaimManager().getClaimAt(block.getLocation());
            if (claim != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            Claim claim = plugin.getClaimManager().getClaimAt(block.getLocation());
            if (claim != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Claim claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
        if (claim != null && !(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Claim claim = plugin.getClaimManager().getClaimAt(event.getLocation());
        if (claim != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Claim claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
        if (claim != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Claim claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
        if (claim != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            Claim claim = plugin.getClaimManager().getClaimAt(b.getLocation());
            if (claim != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            Claim claim = plugin.getClaimManager().getClaimAt(b.getLocation());
            if (claim != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!canUse(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player player && !canUse(player, event.getVehicle().getLocation())) {
            event.setCancelled(true);
        }
    }

    private boolean canUse(Player player, Location location) {
        Claim claim = plugin.getClaimManager().getClaimAt(location);
        return claim == null || claim.isMember(player.getUniqueId()) || player.hasPermission("realestate.bypass");
    }
}
