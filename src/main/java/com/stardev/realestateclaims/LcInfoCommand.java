package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LcInfoCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcInfoCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("realestate.admin")) {
            sender.sendMessage(Component.text("No permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /lcinfo <id>").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid claim id.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaim(id);
        if (claim == null) {
            sender.sendMessage(Component.text("Claim not found.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        sender.sendMessage(Component.text("Claim #" + claim.getId()).color(net.kyori.adventure.text.format.NamedTextColor.GOLD));
        sender.sendMessage(Component.text("World: " + claim.getWorldName()));
        sender.sendMessage(Component.text("X: " + claim.getMinX() + " to " + claim.getMaxX()));
        sender.sendMessage(Component.text("Z: " + claim.getMinZ() + " to " + claim.getMaxZ()));
        sender.sendMessage(Component.text("Price: $" + claim.getPrice()));
        sender.sendMessage(Component.text("Owner: " + (claim.getOwnerName() == null ? "None" : claim.getOwnerName())));
        sender.sendMessage(Component.text("Trusted: " + claim.getTrusted().stream().map(UUID -> Bukkit.getOfflinePlayer(UUID).getName()).filter(name -> name != null).toList()));
        sender.sendMessage(Component.text("Purchased: " + claim.isPurchased()));
        return true;
    }
}
