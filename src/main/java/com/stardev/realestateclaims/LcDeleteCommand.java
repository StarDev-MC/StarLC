package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LcDeleteCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcDeleteCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("realestate.admin")) {
            sender.sendMessage(Component.text("No permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /lcdelete <id>").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
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
        if (claim.getSignLocation() != null && claim.getSignLocation().getBlock().getState() instanceof org.bukkit.block.Sign) {
            Block signBlock = claim.getSignLocation().getBlock();
            signBlock.setType(org.bukkit.Material.AIR, false);
        }
        plugin.getClaimManager().removeClaim(id);
        sender.sendMessage(Component.text("Claim #" + id + " deleted.").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
