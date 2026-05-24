package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class LcTrustCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcTrustCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        if (!sender.hasPermission("realestate.command")) {
            player.sendMessage(Component.text("No permission to trust players.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(Component.text("Usage: /lctrust <player>").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(Component.text("You must stand inside your claim to trust someone.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (!claim.isOwner(player.getUniqueId()) && !player.hasPermission("realestate.admin")) {
            player.sendMessage(Component.text("You are not the owner of this claim.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Player not online.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        UUID targetId = target.getUniqueId();
        if (claim.isTrusted(targetId)) {
            player.sendMessage(Component.text("This player is already trusted.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        claim.getTrusted().add(targetId);
        plugin.getClaimManager().updateClaim(claim);
        player.sendMessage(Component.text(target.getName() + " is now trusted in claim #" + claim.getId()).color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
