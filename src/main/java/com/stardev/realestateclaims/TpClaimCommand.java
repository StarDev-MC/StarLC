package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TpClaimCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public TpClaimCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        if (args.length == 0) {
            // open player's GUI
            plugin.openMyClaimsGui(player);
            return true;
        }
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid claim id.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaim(id);
        if (claim == null) {
            player.sendMessage(Component.text("Claim not found.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        boolean isAdmin = player.hasPermission("realestate.admin");
        if (!isAdmin && !claim.isOwner(player.getUniqueId())) {
            player.sendMessage(Component.text("You do not own this claim.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (claim.getSignLocation() == null) {
            player.sendMessage(Component.text("Claim sign is not available.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (!isAdmin && plugin.hasTeleportCooldown(player.getUniqueId())) {
            long remaining = plugin.getTeleportCooldownRemaining(player.getUniqueId());
            player.sendMessage(Component.text("Teleport cooldown active. Wait " + remaining + " seconds.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        if (isAdmin) {
            plugin.openTeleportConfirmGui(player, claim);
            return true;
        }
        return true;
    }
}
