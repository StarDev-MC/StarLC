package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerClaimsCommand implements CommandExecutor {
    private final RealEstateClaims plugin;
    private final PlayerClaimListGui playerGui;

    public PlayerClaimsCommand(RealEstateClaims plugin, PlayerClaimListGui playerGui) {
        this.plugin = plugin;
        this.playerGui = playerGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("realestate.admin")) {
            sender.sendMessage(Component.text("No permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player admin)) {
            sender.sendMessage(Component.text("This command must be run by a player.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            admin.sendMessage(Component.text("Usage: /playerclaims <player> [page]").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        String targetName = args[0];
        var offline = Bukkit.getOfflinePlayer(targetName);
        UUID ownerId = offline.getUniqueId();
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                admin.sendMessage(Component.text("Invalid page number.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                return true;
            }
        }
        playerGui.openPlayerClaimList(admin, ownerId, page);
        return true;
    }
}
