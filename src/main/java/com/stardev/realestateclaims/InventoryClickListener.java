package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            // GREEN_WOOL = buy, BLUE_WOOL = rent
            if (item.getType() != Material.GREEN_WOOL && item.getType() != Material.BLUE_WOOL) {
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
            if (item.getType() == Material.GREEN_WOOL) {
                // Buy flow
                if (claim.getRenter() != null) {
                    player.sendMessage(Component.text("This land is already rented. You cannot buy it while someone is renting.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                    return;
                }
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
            // Rent flow (BLUE_WOOL)
            // prevent renting if already rented by someone else
            if (claim.getRenter() != null) {
                if (claim.getRenter().equals(player.getUniqueId())) {
                    long nextDue = claim.getNextRentDue();
                    if (nextDue > System.currentTimeMillis()) {
                        player.sendMessage(Component.text("You already rent this land. Next rent due: " + java.time.Instant.ofEpochMilli(nextDue).toString()).color(net.kyori.adventure.text.format.NamedTextColor.RED));
                    } else {
                        player.sendMessage(Component.text("You already rent this land and your payment is overdue. Contact the owner.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                    }
                    return;
                }
                player.sendMessage(Component.text("This land is already rented by someone else.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }

            double rentPrice = claim.getRentPrice() > 0 ? claim.getRentPrice() : plugin.getConfig().getDouble("default-rent", 100.0);
            if (plugin.getEconomy().getBalance(player) < rentPrice) {
                player.sendMessage(Component.text("You do not have enough money to rent this land.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }
            // charge first day immediately
            plugin.getEconomy().withdrawPlayer(player, rentPrice);
            // deposit to recipient (owner or configured recipient)
            String recipientName = plugin.getConfig().getString("rent-recipient", "");
            if (recipientName != null && !recipientName.isBlank()) {
                plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(recipientName), rentPrice);
            } else if (claim.getOwner() != null) {
                plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(claim.getOwner()), rentPrice);
            }
            claim.setRenter(player.getUniqueId());
            claim.setRentPrice(rentPrice);
            claim.setNextRentDue(System.currentTimeMillis() + 24L * 60L * 60L * 1000L);
            plugin.getClaimManager().updateClaim(claim);
            plugin.getClaimManager().updateSign(claim);
            player.closeInventory();
            player.sendMessage(Component.text("You rented land #" + claim.getId() + " for $" + rentPrice + " (daily).").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
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
