package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LcRemoveCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcRemoveCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("realestate.admin")) {
            sender.sendMessage(Component.text("No permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /lcremove <id|all>").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        if (args[0].equalsIgnoreCase("all")) {
            plugin.getClaimManager().resetAllClaims();
            for (Claim claim : plugin.getClaimManager().getAllClaims()) {
                plugin.getClaimManager().updateSign(claim);
            }
            sender.sendMessage(Component.text("All claim owners and renters have been removed.").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
            return true;
        }
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid claim id. Use a numeric id or 'all'.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaim(id);
        if (claim == null) {
            sender.sendMessage(Component.text("Claim not found.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        plugin.getClaimManager().resetClaim(id);
        plugin.getClaimManager().updateSign(claim);
        sender.sendMessage(Component.text("Claim #" + id + " owner and renter have been removed.").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
