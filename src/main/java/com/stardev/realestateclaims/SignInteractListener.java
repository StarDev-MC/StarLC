package com.stardev.realestateclaims;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class SignInteractListener implements Listener {
    private final RealEstateClaims plugin;

    public SignInteractListener(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        Block block = event.getClickedBlock();
        if (!(block.getBlockData() instanceof org.bukkit.block.data.type.Sign)) {
            return;
        }
        Claim claim = plugin.getClaimManager().getClaimBySignLocation(block.getLocation());
        if (claim == null) {
            return;
        }
        event.setCancelled(true);
        if (!claim.isPurchased()) {
            plugin.openPurchaseGui(event.getPlayer(), claim);
            return;
        }
        plugin.openClaimInfoGui(event.getPlayer(), claim);
    }
}
