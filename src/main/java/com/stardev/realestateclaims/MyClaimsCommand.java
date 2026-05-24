package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MyClaimsCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public MyClaimsCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        if (!sender.hasPermission("realestate.command")) {
            player.sendMessage(Component.text("No permission to view your claims.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        plugin.openMyClaimsGui(player);
        return true;
    }
}
