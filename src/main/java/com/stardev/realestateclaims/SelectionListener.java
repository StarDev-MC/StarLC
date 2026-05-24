package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public final class SelectionListener implements Listener {
    private final RealEstateClaims plugin;

    public SelectionListener(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getItem() == null || event.getItem().getType() != Material.BLAZE_ROD) {
            return;
        }
        var meta = event.getItem().getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(plugin.getWandKey(), PersistentDataType.BYTE)) {
            return;
        }
        event.setCancelled(true);
        var player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            LandSelection existing = plugin.getSelection(player.getUniqueId());
            int x2 = existing == null ? block.getX() : existing.x2();
            int z2 = existing == null ? block.getZ() : existing.z2();
            plugin.setSelection(player.getUniqueId(), new LandSelection(block.getWorld().getName(), block.getX(), block.getZ(), x2, z2));
            player.sendMessage(Component.text("Position 1 set to X:" + block.getX() + " Z:" + block.getZ()).color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            LandSelection existing = plugin.getSelection(player.getUniqueId());
            int x1 = existing == null ? block.getX() : existing.x1();
            int z1 = existing == null ? block.getZ() : existing.z1();
            plugin.setSelection(player.getUniqueId(), new LandSelection(block.getWorld().getName(), x1, z1, block.getX(), block.getZ()));
            player.sendMessage(Component.text("Position 2 set to X:" + block.getX() + " Z:" + block.getZ()).color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        }
    }
}
