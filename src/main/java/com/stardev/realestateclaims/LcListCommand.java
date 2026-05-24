package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class LcListCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcListCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        if (!sender.hasPermission("realestate.command")) {
            player.sendMessage(Component.text("No permission to list trusted players.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(Component.text("You must stand inside a claim to list trusted players.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (!claim.isOwner(player.getUniqueId()) && !player.hasPermission("realestate.admin")) {
            player.sendMessage(Component.text("You are not the owner of this claim.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        player.sendMessage(Component.text("Trusted players for claim #" + claim.getId()).color(net.kyori.adventure.text.format.NamedTextColor.GOLD));
        if (claim.getTrusted().isEmpty()) {
            player.sendMessage(Component.text("No trusted players."));
            return true;
        }
        for (UUID trustedId : claim.getTrusted()) {
            player.sendMessage(Component.text("- " + org.bukkit.Bukkit.getOfflinePlayer(trustedId).getName()));
        }
        return true;
    }
}
