package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LcPriceCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcPriceCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("realestate.admin")) {
            sender.sendMessage(Component.text("No permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /lcprice <id> <price>").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        int id;
        double price;
        try {
            id = Integer.parseInt(args[0]);
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid id or price.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaim(id);
        if (claim == null) {
            sender.sendMessage(Component.text("Claim not found.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        claim.setPrice(price);
        plugin.getClaimManager().updateClaim(claim);
        plugin.getClaimManager().updateSign(claim);
        sender.sendMessage(Component.text("Claim #" + id + " price set to $" + price).color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
