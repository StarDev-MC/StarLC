package com.stardev.realestateclaims;

import net.milkbowl.vault.economy.Economy;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RealEstateClaims extends JavaPlugin {
    private ClaimManager claimManager;
    private Economy economy;
    private final Map<UUID, LandSelection> selections = new HashMap<>();
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private NamespacedKey claimIdKey;
    private NamespacedKey wandKey;
    private AdminClaimListGui adminClaimListGui;

    private static final Component PURCHASE_TITLE = Component.text("Purchase Land");
    private static final Component CLAIM_INFO_TITLE = Component.text("Claim Info");
    private static final Component MYCLAIMS_TITLE = Component.text("My Claims");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("claims.yml", false);
        claimIdKey = new NamespacedKey(this, "claim-id");
        wandKey = new NamespacedKey(this, "realestate-wand");
        adminClaimListGui = new AdminClaimListGui(this);

        claimManager = new ClaimManager(this);
        claimManager.loadClaims();
        if (!setupVault()) {
            getLogger().severe("Vault not found or economy provider missing. RealEstateClaims will disable.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerListeners();
        registerCommands();
        // Schedule rent collection check every hour (real-life hour)
        long ticksPerHour = 20L * 60L * 60L;
        long overdueGraceMillis = 60L * 60L * 1000L;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long now = System.currentTimeMillis();
            for (Claim claim : claimManager.getAllClaims()) {
                UUID renter = claim.getRenter();
                if (renter == null) continue;
                long nextDue = claim.getNextRentDue();
                if (nextDue <= 0 || nextDue > now) continue;
                double rent = claim.getRentPrice() > 0 ? claim.getRentPrice() : getConfig().getDouble("default-rent", 100.0);
                OfflinePlayer off = Bukkit.getOfflinePlayer(renter);
                if (now >= nextDue + overdueGraceMillis) {
                    // Evict if rent has been overdue for more than one hour
                    claim.getTrusted().remove(renter);
                    claim.setRenter(null);
                    claim.setNextRentDue(0L);
                    claim.setOwner(claim.getOwner());
                    claimManager.updateClaim(claim);
                    claimManager.updateSign(claim);
                    if (off.isOnline() && off.getPlayer() != null) {
                        off.getPlayer().sendMessage(Component.text("You were evicted from rented land #" + claim.getId() + " after missing rent for over one hour.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                    }
                    continue;
                }
                if (economy.getBalance(off) >= rent) {
                    economy.withdrawPlayer(off, rent);
                    String recipientName = getConfig().getString("rent-recipient", "");
                    if (recipientName != null && !recipientName.isBlank()) {
                        economy.depositPlayer(Bukkit.getOfflinePlayer(recipientName), rent);
                    } else if (claim.getOwner() != null) {
                        economy.depositPlayer(Bukkit.getOfflinePlayer(claim.getOwner()), rent);
                    }
                    claim.setNextRentDue(nextDue + 24L * 60L * 60L * 1000L);
                    claimManager.updateClaim(claim);
                }
            }
        }, ticksPerHour, ticksPerHour);
    }

    @Override
    public void onDisable() {
        if (claimManager != null) {
            claimManager.saveClaims();
        }
    }

    private boolean setupVault() {
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            return false;
        }
        economy = provider.getProvider();
        return economy != null;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new SelectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SignInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MovementListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }

    private void registerCommands() {
        getCommand("lcwand").setExecutor(new LcWandCommand(this));
        getCommand("setnewlc").setExecutor(new SetNewLcCommand(this));
        getCommand("lcprice").setExecutor(new LcPriceCommand(this));
        getCommand("lcrent").setExecutor(new LcRentCommand(this));
        getCommand("lcdelete").setExecutor(new LcDeleteCommand(this));
        getCommand("lcinfo").setExecutor(new LcInfoCommand(this));
        getCommand("lcreload").setExecutor(new LcReloadCommand(this));
        getCommand("lcremove").setExecutor(new LcRemoveCommand(this));
        getCommand("lcreset").setExecutor(new LcResetCommand(this));
        getCommand("lcadminlist").setExecutor(new LcAdminListCommand(this, adminClaimListGui));
        getCommand("lctrust").setExecutor(new LcTrustCommand(this));
        getCommand("lcuntrust").setExecutor(new LcUntrustCommand(this));
        getCommand("lclist").setExecutor(new LcListCommand(this));
        getCommand("claiminfo").setExecutor(new ClaimInfoCommand(this));
        getCommand("myclaims").setExecutor(new MyClaimsCommand(this));
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public NamespacedKey getClaimIdKey() {
        return claimIdKey;
    }

    public NamespacedKey getWandKey() {
        return wandKey;
    }

    public LandSelection getSelection(UUID playerId) {
        return selections.get(playerId);
    }

    public void setSelection(UUID playerId, LandSelection selection) {
        selections.put(playerId, selection);
    }

    public Component getPurchaseTitle() {
        return PURCHASE_TITLE;
    }

    public Component getClaimInfoTitle() {
        return CLAIM_INFO_TITLE;
    }

    public Component getMyClaimsTitle() {
        return MYCLAIMS_TITLE;
    }

    public boolean hasTeleportCooldown(UUID playerId) {
        long expiration = teleportCooldowns.getOrDefault(playerId, 0L);
        return System.currentTimeMillis() < expiration;
    }

    public long getTeleportCooldownRemaining(UUID playerId) {
        long expiration = teleportCooldowns.getOrDefault(playerId, 0L);
        return Math.max(0, (expiration - System.currentTimeMillis()) / 1000);
    }

    public void setTeleportCooldown(UUID playerId) {
        int seconds = getConfig().getInt("teleport-cooldown", 30);
        teleportCooldowns.put(playerId, System.currentTimeMillis() + seconds * 1000L);
    }

    public void openPurchaseGui(Player player, Claim claim) {
        Inventory inventory = Bukkit.createInventory(null, 27, PURCHASE_TITLE);
        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("Confirm Purchase", net.kyori.adventure.text.format.NamedTextColor.GREEN));
        confirmMeta.lore(List.of(
                Component.text("Price: $" + claim.getPrice()),
                Component.text("Click to buy this land.")));
        confirmMeta.getPersistentDataContainer().set(claimIdKey, org.bukkit.persistence.PersistentDataType.INTEGER, claim.getId());
        confirmMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel", net.kyori.adventure.text.format.NamedTextColor.RED));
        cancelMeta.lore(List.of(Component.text("Close without purchasing.")));
        cancelMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        cancel.setItemMeta(cancelMeta);

        inventory.setItem(11, confirm);
        // Rent option
        ItemStack rent = new ItemStack(Material.BLUE_WOOL);
        ItemMeta rentMeta = rent.getItemMeta();
        double rentPrice = claim.getRentPrice() > 0 ? claim.getRentPrice() : getConfig().getDouble("default-rent", 100.0);
        rentMeta.displayName(Component.text("Rent (daily)", net.kyori.adventure.text.format.NamedTextColor.AQUA));
        rentMeta.lore(List.of(
            Component.text("First day: $" + rentPrice),
            Component.text("Click to rent this land for one day.")));
        rentMeta.getPersistentDataContainer().set(claimIdKey, org.bukkit.persistence.PersistentDataType.INTEGER, claim.getId());
        rentMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        rent.setItemMeta(rentMeta);
        inventory.setItem(13, rent);
        inventory.setItem(15, cancel);
        player.openInventory(inventory);
    }

    public void openClaimInfoGui(Player player, Claim claim) {
        Inventory inventory = Bukkit.createInventory(null, 27, CLAIM_INFO_TITLE);
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("Claim #" + claim.getId(), net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Owner: " + (claim.getOwnerName() == null ? "None" : claim.getOwnerName())));
        lore.add(Component.text("Price: $" + claim.getPrice()));
        lore.add(Component.text("Status: " + (claim.isPurchased() ? "Owned" : "For Sale")));
        lore.add(Component.text("World: " + claim.getWorldName()));
        lore.add(Component.text("X: " + claim.getMinX() + " to " + claim.getMaxX()));
        lore.add(Component.text("Z: " + claim.getMinZ() + " to " + claim.getMaxZ()));
        lore.add(Component.text("Trusted: " + claim.getTrusted().size()));
        lore.add(Component.text("Renter: " + (claim.getRenter() == null ? "None" : Bukkit.getOfflinePlayer(claim.getRenter()).getName())));
        long nextDue = claim.getNextRentDue();
        lore.add(Component.text("Next rent due: " + (nextDue <= 0 ? "None" : java.time.Instant.ofEpochMilli(nextDue).toString())));
        infoMeta.lore(lore);
        infoMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        info.setItemMeta(infoMeta);
        inventory.setItem(13, info);
        player.openInventory(inventory);
    }

    public void openMyClaimsGui(Player player) {
        List<Claim> ownedClaims = claimManager.getOwnedClaims(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(null, 54, MYCLAIMS_TITLE);
        for (int index = 0; index < ownedClaims.size() && index < 54; index++) {
            Claim claim = ownedClaims.get(index);
            ItemStack claimItem = new ItemStack(Material.PAPER);
            ItemMeta meta = claimItem.getItemMeta();
            meta.displayName(Component.text("Claim #" + claim.getId(), net.kyori.adventure.text.format.NamedTextColor.GOLD));
            List<Component> lore = List.of(
                    Component.text("Price: $" + claim.getPrice()),
                    Component.text("World: " + claim.getWorldName()),
                    Component.text("X: " + claim.getMinX() + " to " + claim.getMaxX()),
                    Component.text("Z: " + claim.getMinZ() + " to " + claim.getMaxZ()),
                    Component.text("Click to teleport to your sign."));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(claimIdKey, org.bukkit.persistence.PersistentDataType.INTEGER, claim.getId());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            claimItem.setItemMeta(meta);
            inventory.setItem(index, claimItem);
        }
        if (ownedClaims.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta emptyMeta = empty.getItemMeta();
            emptyMeta.displayName(Component.text("No claims", net.kyori.adventure.text.format.NamedTextColor.RED));
            emptyMeta.lore(List.of(Component.text("You do not own any land yet.")));
            emptyMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            empty.setItemMeta(emptyMeta);
            inventory.setItem(22, empty);
        }
        player.openInventory(inventory);
    }

    public FileConfiguration getClaimsConfig() {
        return claimManager.getClaimsConfig();
    }
}
