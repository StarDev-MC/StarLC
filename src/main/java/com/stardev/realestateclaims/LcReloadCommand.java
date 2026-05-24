package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LcReloadCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public LcReloadCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("realestate.admin")) {
            sender.sendMessage(Component.text("No permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        plugin.reloadConfig();
        plugin.getClaimManager().reloadClaims();
        sender.sendMessage(Component.text("RealEstateClaims configuration and claims reloaded.").color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }
}
