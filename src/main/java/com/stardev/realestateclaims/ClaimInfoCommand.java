package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ClaimInfoCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public ClaimInfoCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        if (!sender.hasPermission("realestate.command")) {
            player.sendMessage(Component.text("No permission to check claim info.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(Component.text("You are not standing inside a claim.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        plugin.openClaimInfoGui(player, claim);
        return true;
    }
}
