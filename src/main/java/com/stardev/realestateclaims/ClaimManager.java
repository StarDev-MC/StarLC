package com.stardev.realestateclaims;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ClaimManager {
    private final RealEstateClaims plugin;
    private final Map<Integer, Claim> claims = new HashMap<>();
    private final Map<ChunkKey, Set<Integer>> chunkIndex = new HashMap<>();
    private final Map<String, Integer> signIndex = new HashMap<>();
    private File claimsFile;
    private FileConfiguration claimsConfig;
    private int nextId = 1;

    public ClaimManager(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    public void loadClaims() {
        claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        if (!claimsFile.exists()) {
            plugin.saveResource("claims.yml", false);
        }
        claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);
        nextId = claimsConfig.getInt("next-claim-id", 1);
        ConfigurationSection root = claimsConfig.getConfigurationSection("claims");
        if (root == null) {
            root = claimsConfig.createSection("claims");
        }
        claims.clear();
        chunkIndex.clear();
        signIndex.clear();

        for (String key : root.getKeys(false)) {
            int id;
            try {
                id = Integer.parseInt(key.replace("claim-", ""));
            } catch (NumberFormatException ignore) {
                continue;
            }
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            Claim claim = loadClaim(section, id);
            if (claim != null) {
                claims.put(id, claim);
                indexClaim(claim);
            }
        }
    }

    public void reloadClaims() {
        loadClaims();
    }

    private Claim loadClaim(ConfigurationSection section, int id) {
        String worldName = section.getString("world");
        if (worldName == null) {
            return null;
        }
        int x1 = section.getInt("x1");
        int x2 = section.getInt("x2");
        int z1 = section.getInt("z1");
        int z2 = section.getInt("z2");
        double price = section.getDouble("price", plugin.getConfig().getDouble("default-price", 5000));
        UUID owner = null;
        if (section.isString("owner")) {
            try {
                owner = UUID.fromString(section.getString("owner"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        boolean purchased = section.getBoolean("purchased", owner != null);
        Set<UUID> trusted = new HashSet<>();
        if (section.isList("trusted")) {
            for (String raw : section.getStringList("trusted")) {
                try {
                    trusted.add(UUID.fromString(raw));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        Location signLocation = null;
        if (section.isConfigurationSection("sign-location")) {
            ConfigurationSection signSection = section.getConfigurationSection("sign-location");
            if (signSection != null) {
                String signWorld = signSection.getString("world");
                int signX = signSection.getInt("x");
                int signY = signSection.getInt("y");
                int signZ = signSection.getInt("z");
                World world = Bukkit.getWorld(signWorld);
                if (world != null) {
                    signLocation = new Location(world, signX, signY, signZ);
                }
            }
        }
        Claim claim = new Claim(id, worldName, Math.min(x1, x2), Math.max(x1, x2), Math.min(z1, z2), Math.max(z1, z2), price, owner, trusted, purchased, signLocation);
        return claim;
    }

    public Claim createClaim(LandSelection selection, Location signLocation) {
        int id = nextId++;
        Claim claim = new Claim(id, selection.worldName(), selection.minX(), selection.maxX(), selection.minZ(), selection.maxZ(), plugin.getConfig().getDouble("default-price", 5000), null, new HashSet<>(), false, signLocation);
        claims.put(id, claim);
        indexClaim(claim);
        saveClaims();
        return claim;
    }

    public Claim getClaim(int id) {
        return claims.get(id);
    }

    public Claim getClaimAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        ChunkKey key = ChunkKey.of(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        Set<Integer> ids = chunkIndex.get(key);
        if (ids == null) {
            return null;
        }
        for (Integer id : ids) {
            Claim claim = claims.get(id);
            if (claim != null && claim.contains(location)) {
                return claim;
            }
        }
        return null;
    }

    public Claim getClaimBySignLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        Integer id = signIndex.get(formatLocation(location));
        if (id == null) {
            return null;
        }
        return claims.get(id);
    }

    public List<Claim> getOwnedClaims(UUID ownerId) {
        List<Claim> owned = new ArrayList<>();
        for (Claim claim : claims.values()) {
            if (claim.isOwner(ownerId)) {
                owned.add(claim);
            }
        }
        return owned;
    }

    public Collection<Claim> getAllClaims() {
        return claims.values();
    }

    public void saveClaims() {
        if (claimsConfig == null || claimsFile == null) {
            claimsFile = new File(plugin.getDataFolder(), "claims.yml");
            claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);
        }
        claimsConfig.set("next-claim-id", nextId);
        ConfigurationSection root = claimsConfig.createSection("claims");
        for (Claim claim : claims.values()) {
            ConfigurationSection section = root.createSection("claim-" + claim.getId());
            section.set("world", claim.getWorldName());
            section.set("x1", claim.getMinX());
            section.set("x2", claim.getMaxX());
            section.set("z1", claim.getMinZ());
            section.set("z2", claim.getMaxZ());
            section.set("price", claim.getPrice());
            section.set("owner", claim.getOwner() == null ? null : claim.getOwner().toString());
            section.set("purchased", claim.isPurchased());
            section.set("trusted", claim.getTrusted().stream().map(UUID::toString).toList());
            if (claim.getSignLocation() != null) {
                ConfigurationSection signSection = section.createSection("sign-location");
                signSection.set("world", claim.getSignLocation().getWorld().getName());
                signSection.set("x", claim.getSignLocation().getBlockX());
                signSection.set("y", claim.getSignLocation().getBlockY());
                signSection.set("z", claim.getSignLocation().getBlockZ());
            }
        }
        try {
            claimsConfig.save(claimsFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Unable to save claim data: " + exception.getMessage());
        }
    }

    public void removeClaim(int id) {
        Claim removed = claims.remove(id);
        if (removed != null) {
            unindexClaim(removed);
            saveClaims();
        }
    }

    public void updateClaim(Claim claim) {
        if (claim == null) {
            return;
        }
        saveClaims();
    }

    public void indexClaim(Claim claim) {
        if (claim == null) {
            return;
        }
        int fromChunkX = Math.floorDiv(claim.getMinX(), 16);
        int toChunkX = Math.floorDiv(claim.getMaxX(), 16);
        int fromChunkZ = Math.floorDiv(claim.getMinZ(), 16);
        int toChunkZ = Math.floorDiv(claim.getMaxZ(), 16);
        for (int chunkX = fromChunkX; chunkX <= toChunkX; chunkX++) {
            for (int chunkZ = fromChunkZ; chunkZ <= toChunkZ; chunkZ++) {
                ChunkKey key = ChunkKey.of(claim.getWorldName(), chunkX, chunkZ);
                chunkIndex.computeIfAbsent(key, ignored -> new HashSet<>()).add(claim.getId());
            }
        }
        if (claim.getSignLocation() != null) {
            signIndex.put(formatLocation(claim.getSignLocation()), claim.getId());
        }
    }

    public void unindexClaim(Claim claim) {
        int fromChunkX = Math.floorDiv(claim.getMinX(), 16);
        int toChunkX = Math.floorDiv(claim.getMaxX(), 16);
        int fromChunkZ = Math.floorDiv(claim.getMinZ(), 16);
        int toChunkZ = Math.floorDiv(claim.getMaxZ(), 16);
        for (int chunkX = fromChunkX; chunkX <= toChunkX; chunkX++) {
            for (int chunkZ = fromChunkZ; chunkZ <= toChunkZ; chunkZ++) {
                ChunkKey key = ChunkKey.of(claim.getWorldName(), chunkX, chunkZ);
                Set<Integer> set = chunkIndex.get(key);
                if (set != null) {
                    set.remove(claim.getId());
                    if (set.isEmpty()) {
                        chunkIndex.remove(key);
                    }
                }
            }
        }
        if (claim.getSignLocation() != null) {
            signIndex.remove(formatLocation(claim.getSignLocation()));
        }
    }

    public void updateSign(Claim claim) {
        if (claim == null || claim.getSignLocation() == null) {
            return;
        }
        Location signLocation = claim.getSignLocation();
        if (signLocation.getWorld() == null) {
            return;
        }
        Block block = signLocation.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Sign sign)) {
            return;
        }
        sign.line(0, net.kyori.adventure.text.Component.text("[Land]"));
        if (!claim.isPurchased()) {
            sign.line(1, net.kyori.adventure.text.Component.text("Land #" + claim.getId() + " For Sale"));
            sign.line(2, net.kyori.adventure.text.Component.text("$" + claim.getPrice()));
            sign.line(3, net.kyori.adventure.text.Component.text("Right click to buy"));
        } else {
            sign.line(1, net.kyori.adventure.text.Component.text("Owned " + claim.getOwnerName()));
            sign.line(2, net.kyori.adventure.text.Component.text("Claim #" + claim.getId()));
            sign.line(3, net.kyori.adventure.text.Component.text("Trusted: " + claim.getTrusted().size()));
        }
        sign.update(true);
    }

    public boolean isClaimAtLocation(Location location) {
        return getClaimAt(location) != null;
    }

    public FileConfiguration getClaimsConfig() {
        return claimsConfig;
    }

    private static String formatLocation(Location location) {
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private record ChunkKey(String world, int x, int z) {
        static ChunkKey of(String world, int x, int z) {
            return new ChunkKey(world, x, z);
        }
    }
}
