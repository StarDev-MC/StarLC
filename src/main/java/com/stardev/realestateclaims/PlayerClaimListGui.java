package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerClaimListGui implements Listener {
    private final RealEstateClaims plugin;
    private static final int ITEMS_PER_PAGE = 45;

    public PlayerClaimListGui(RealEstateClaims plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openPlayerClaimList(Player admin, UUID ownerId, int page) {
        List<Claim> owned = plugin.getClaimManager().getOwnedClaims(ownerId);
        int totalPages = (owned.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        if (page < 1) page = 1;
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Claims of " + (ownerName == null ? ownerId.toString() : ownerName) + " (Page " + page + "/" + totalPages + ")"));

        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, owned.size());
        for (int i = start; i < end; i++) {
            Claim claim = owned.get(i);
            ItemStack claimItem = new ItemStack(Material.PAPER);
            ItemMeta meta = claimItem.getItemMeta();
            meta.displayName(Component.text("Claim #" + claim.getId(), net.kyori.adventure.text.format.NamedTextColor.GOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("World: " + claim.getWorldName()));
            lore.add(Component.text("X: " + claim.getMinX() + " to " + claim.getMaxX()));
            lore.add(Component.text("Z: " + claim.getMinZ() + " to " + claim.getMaxZ()));
            lore.add(Component.text(""));
            lore.add(Component.text("Left click to teleport"));
            lore.add(Component.text("Right click to view info"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(plugin.getClaimIdKey(), PersistentDataType.INTEGER, claim.getId());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            claimItem.setItemMeta(meta);
            inventory.addItem(claimItem);
        }

        // navigation
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            prevMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "player-list-nav"), PersistentDataType.STRING, "prev:" + (page - 1) + ":" + ownerId.toString());
            prevMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            prev.setItemMeta(prevMeta);
            inventory.setItem(48, prev);
        }
        if (page < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            nextMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "player-list-nav"), PersistentDataType.STRING, "next:" + (page + 1) + ":" + ownerId.toString());
            nextMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            next.setItemMeta(nextMeta);
            inventory.setItem(50, next);
        }

        admin.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerListClick(InventoryClickEvent event) {
        var title = event.getView().getTitle();
        if (!title.startsWith("Claims of ")) return;
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String nav = meta.getPersistentDataContainer().get(new org.bukkit.NamespacedKey(plugin, "player-list-nav"), PersistentDataType.STRING);
        if (nav != null) {
            String[] parts = nav.split(":" );
            if (parts.length >= 3) {
                int nextPage = Integer.parseInt(parts[1]);
                UUID ownerId = UUID.fromString(parts[2]);
                openPlayerClaimList((Player) event.getWhoClicked(), ownerId, nextPage);
            }
            return;
        }

        Integer claimId = meta.getPersistentDataContainer().get(plugin.getClaimIdKey(), PersistentDataType.INTEGER);
        if (claimId == null) return;
        Claim claim = plugin.getClaimManager().getClaim(claimId);
        if (claim == null) return;
        Player admin = (Player) event.getWhoClicked();
        ClickType clickType = event.getClick();
        if (clickType.isRightClick()) {
            plugin.openClaimInfoGui(admin, claim);
            return;
        }
        // left click -> open confirmation for admins
        if (claim.getSignLocation() == null) {
            admin.sendMessage(Component.text("Claim sign is not available.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }
        plugin.openTeleportConfirmGui(admin, claim);
    }
}
