package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LcAdminListCommand implements CommandExecutor {
    private final RealEstateClaims plugin;
    private final AdminClaimListGui adminGui;

    public LcAdminListCommand(RealEstateClaims plugin, AdminClaimListGui adminGui) {
        this.plugin = plugin;
        this.adminGui = adminGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("realestate.admin")) {
            sender.sendMessage(Component.text("No permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by a player.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid page number.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return true;
            }
        }

        adminGui.openAdminClaimList(player, page);
        return true;
    }
}
