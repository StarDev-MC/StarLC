package com.stardev.realestateclaims;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Claim {
    private final int id;
    private final String worldName;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private double price;
    private UUID owner;
    private final Set<UUID> trusted;
    private boolean purchased;
    private Location signLocation;

    public Claim(int id, String worldName, int minX, int maxX, int minZ, int maxZ, double price, UUID owner, Set<UUID> trusted, boolean purchased, Location signLocation) {
        this.id = id;
        this.worldName = worldName;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.price = price;
        this.owner = owner;
        this.trusted = trusted == null ? new HashSet<>() : new HashSet<>(trusted);
        this.purchased = purchased;
        this.signLocation = signLocation;
    }

    public int getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = Math.max(0, price);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.purchased = owner != null;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    public boolean isOwner(UUID playerId) {
        return owner != null && owner.equals(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return trusted.contains(playerId);
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean isMember(UUID playerId) {
        return playerId != null && (isOwner(playerId) || isTrusted(playerId));
    }

    public String getOwnerName() {
        if (owner == null) {
            return null;
        }
        return Bukkit.getOfflinePlayer(owner).getName();
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public void setSignLocation(Location signLocation) {
        this.signLocation = signLocation;
    }
}
