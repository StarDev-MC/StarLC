package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AdminClaimListGui implements Listener {
    private final RealEstateClaims plugin;
    private static final int ITEMS_PER_PAGE = 45;
    
    public AdminClaimListGui(RealEstateClaims plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openAdminClaimList(Player player, int page) {
        List<Claim> allClaims = new ArrayList<>(plugin.getClaimManager().getAllClaims());
        int totalPages = (allClaims.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("All Claims (Page " + page + "/" + totalPages + ")"));
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allClaims.size());

        for (int i = start; i < end; i++) {
            Claim claim = allClaims.get(i);
            ItemStack claimItem = new ItemStack(Material.PAPER);
            ItemMeta meta = claimItem.getItemMeta();

            String ownerName = claim.getOwnerName() != null ? claim.getOwnerName() : "None";
            String renterName = claim.getRenter() != null ? Bukkit.getOfflinePlayer(claim.getRenter()).getName() : "None";
            int trustedCount = claim.getTrusted().size();

            meta.displayName(Component.text("Claim #" + claim.getId(), net.kyori.adventure.text.format.NamedTextColor.GOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("World: " + claim.getWorldName()));
            lore.add(Component.text("X: " + claim.getMinX() + " to " + claim.getMaxX()));
            lore.add(Component.text("Z: " + claim.getMinZ() + " to " + claim.getMaxZ()));
            lore.add(Component.text(""));
            lore.add(Component.text("Owner: " + ownerName, net.kyori.adventure.text.format.NamedTextColor.GREEN));
            lore.add(Component.text("Renter: " + renterName, net.kyori.adventure.text.format.NamedTextColor.AQUA));
            lore.add(Component.text("Trusted: " + trustedCount, net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            lore.add(Component.text(""));
            lore.add(Component.text("Buy Price: $" + claim.getPrice()));
            lore.add(Component.text("Rent Price: $" + claim.getRentPrice()));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(plugin.getClaimIdKey(), PersistentDataType.INTEGER, claim.getId());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            claimItem.setItemMeta(meta);
            inventory.addItem(claimItem);
        }

        // Add navigation buttons
        if (page > 1) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            prevMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "admin-list-nav"), PersistentDataType.STRING, "prev:" + (page - 1));
            prevMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(48, prevButton);
        }

        if (page < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            nextMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "admin-list-nav"), PersistentDataType.STRING, "next:" + (page + 1));
            nextMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(50, nextButton);
        }

        player.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminListClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("All Claims (Page")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }

            // Check for navigation
            String nav = meta.getPersistentDataContainer().get(new org.bukkit.NamespacedKey(plugin, "admin-list-nav"), PersistentDataType.STRING);
            if (nav != null) {
                String[] parts = nav.split(":");
                if (parts.length == 2) {
                    int nextPage = Integer.parseInt(parts[1]);
                    openAdminClaimList((Player) event.getWhoClicked(), nextPage);
                }
                return;
            }

            // Show claim info
            Integer claimId = meta.getPersistentDataContainer().get(plugin.getClaimIdKey(), PersistentDataType.INTEGER);
            if (claimId != null) {
                Claim claim = plugin.getClaimManager().getClaim(claimId);
                if (claim != null) {
                    plugin.openClaimInfoGui((Player) event.getWhoClicked(), claim);
                }
            }
        }
    }
}
