package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class LcUntrustCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcUntrustCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        if (!sender.hasPermission("realestate.command")) {
            player.sendMessage(Component.text("No permission to untrust players.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(Component.text("Usage: /lcuntrust <player>").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(Component.text("You must stand inside your claim to untrust someone.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (!claim.isOwner(player.getUniqueId()) && !player.hasPermission("realestate.admin")) {
            player.sendMessage(Component.text("You are not the owner of this claim.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Player not online.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        UUID targetId = target.getUniqueId();
        if (!claim.isTrusted(targetId)) {
            player.sendMessage(Component.text("This player is not trusted.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        claim.getTrusted().remove(targetId);
        plugin.getClaimManager().updateClaim(claim);
        player.sendMessage(Component.text(target.getName() + " is no longer trusted in claim #" + claim.getId()).color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
