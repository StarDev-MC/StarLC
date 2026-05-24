package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class MovementListener implements Listener {
    private final RealEstateClaims plugin;

    public MovementListener(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        var player = event.getPlayer();
        Claim claim = plugin.getClaimManager().getClaimAt(event.getTo());
        if (claim == null) {
            return;
        }
        if (claim.isMember(player.getUniqueId()) || player.hasPermission("realestate.bypass")) {
            return;
        }
        event.setTo(event.getFrom());
        player.sendActionBar(Component.text("You are not trusted in this property.", net.kyori.adventure.text.format.NamedTextColor.RED));
    }
}
