package com.stardev.realestateclaims;

public record LandSelection(String worldName, int x1, int z1, int x2, int z2) {
    public int minX() {
        return Math.min(x1, x2);
    }

    public int maxX() {
        return Math.max(x1, x2);
    }

    public int minZ() {
        return Math.min(z1, z2);
    }

    public int maxZ() {
        return Math.max(z1, z2);
    }
}
