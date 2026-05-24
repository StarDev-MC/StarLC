package com.stardev.realestateclaims;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetNewLcCommand implements CommandExecutor {
    private final RealEstateClaims plugin;

    public SetNewLcCommand(RealEstateClaims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!sender.hasPermission("realestate.admin")) {
            player.sendMessage(Component.text("You need realestate.admin permission.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        LandSelection selection = plugin.getSelection(player.getUniqueId());
        if (selection == null) {
            player.sendMessage(Component.text("You must select two corners with /lcwand first.").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
            return true;
        }
        Location signLocation = findSignLocation(player);
        if (signLocation == null) {
            player.sendMessage(Component.text("Please stand near an empty block to place the claim sign.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        signLocation.getBlock().setType(Material.OAK_SIGN, false);
        Block block = signLocation.getBlock();
        if (!(block.getState() instanceof Sign sign)) {
            player.sendMessage(Component.text("Unable to place a sign at this location.").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return true;
        }
        Claim claim = plugin.getClaimManager().createClaim(selection, signLocation);
        plugin.getClaimManager().updateSign(claim);
        player.sendMessage(Component.text("Created claim #" + claim.getId() + " with default price $" + claim.getPrice()).color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        return true;
    }

    private Location findSignLocation(Player player) {
        Location base = player.getLocation();
        Block block = base.getBlock();
        if (block.isEmpty()) {
            return block.getLocation();
        }
        Block above = block.getRelative(org.bukkit.block.BlockFace.UP);
        if (above.isEmpty()) {
            return above.getLocation();
        }
        return null;
    }
}
