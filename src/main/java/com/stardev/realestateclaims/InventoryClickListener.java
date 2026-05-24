package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class InventoryClickListener implements Listener {
    private final RealEstateClaims plugin;

    public InventoryClickListener(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        var title = event.getView().title();
        if (title.equals(plugin.getPurchaseTitle())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            if (item.getType() == Material.RED_WOOL) {
                event.getWhoClicked().closeInventory();
                return;
            }
            if (item.getType() != Material.GREEN_WOOL) {
                return;
            }
            Integer claimId = meta.getPersistentDataContainer().get(plugin.getClaimIdKey(), PersistentDataType.INTEGER);
            if (claimId == null) {
                return;
            }
            Claim claim = plugin.getClaimManager().getClaim(claimId);
            if (claim == null) {
                event.getWhoClicked().closeInventory();
                return;
            }
            var player = (org.bukkit.entity.Player) event.getWhoClicked();
            if (plugin.getEconomy().getBalance(player) < claim.getPrice()) {
                player.sendMessage(Component.text("You do not have enough money to purchase this land.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }
            plugin.getEconomy().withdrawPlayer(player, claim.getPrice());
            claim.setOwner(player.getUniqueId());
            claim.getTrusted().add(player.getUniqueId());
            plugin.getClaimManager().updateClaim(claim);
            plugin.getClaimManager().updateSign(claim);
            player.closeInventory();
            player.sendMessage(Component.text("You purchased land #" + claim.getId() + ".").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
            return;
        }
        if (title.equals(plugin.getMyClaimsTitle())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }
            Integer claimId = meta.getPersistentDataContainer().get(plugin.getClaimIdKey(), PersistentDataType.INTEGER);
            if (claimId == null) {
                return;
            }
            var player = (org.bukkit.entity.Player) event.getWhoClicked();
            if (plugin.hasTeleportCooldown(player.getUniqueId())) {
                long remaining = plugin.getTeleportCooldownRemaining(player.getUniqueId());
                player.sendMessage(Component.text("Teleport cooldown active. Wait " + remaining + " seconds.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }
            Claim claim = plugin.getClaimManager().getClaim(claimId);
            if (claim == null || claim.getSignLocation() == null) {
                player.sendMessage(Component.text("Claim sign is no longer available.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }
            player.teleport(claim.getSignLocation().add(0.5, 1, 0.5));
            plugin.setTeleportCooldown(player.getUniqueId());
            player.closeInventory();
            player.sendMessage(Component.text("Teleported to claim sign for claim #" + claim.getId() + ".").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        }
    }
}
