package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class LcWandCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcWandCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        if (!sender.hasPermission("realestate.admin")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Land Claim Wand", net.kyori.adventure.text.format.NamedTextColor.GOLD));
            meta.lore(java.util.List.of(
                    Component.text("Left click a block to set position 1."),
                    Component.text("Right click a block to set position 2.")));
            meta.getPersistentDataContainer().set(plugin.getWandKey(), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        player.sendMessage(Component.text("Claim wand given. Use it to select your land region.").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
