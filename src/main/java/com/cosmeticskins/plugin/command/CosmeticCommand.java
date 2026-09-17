package com.cosmeticskins.plugin.command;

import com.cosmeticskins.plugin.CosmeticSkins;
import com.cosmeticskins.plugin.gui.CosmeticGUI;
import com.cosmeticskins.plugin.model.SkinDefinition;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CosmeticCommand implements CommandExecutor {

    private final CosmeticSkins plugin;

    public CosmeticCommand(CosmeticSkins plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return openMenu(sender);
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(ChatColor.RED + "Usage: /cosmetic | give <player> <skinId> [amount] | list | reload");
        }
        return true;
    }

    private boolean openMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can open the cosmetic menu.");
            return true;
        }
        if (!player.hasPermission("cosmeticskins.use")) {
            player.sendMessage(plugin.getMainConfig().message("no-permission"));
            return true;
        }
        CosmeticGUI gui = new CosmeticGUI(plugin, player);
        player.openInventory(gui.getInventory());
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cosmeticskins.admin")) {
            sender.sendMessage(plugin.getMainConfig().message("no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /cosmetic give <player> <skinId> [amount]");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMainConfig().message("player-not-found"));
            return;
        }

        SkinDefinition skin = plugin.getSkinManager().get(args[2]);
        if (skin == null) {
            sender.sendMessage(plugin.getMainConfig().message("skin-not-found").replace("%id%", args[2]));
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                return;
            }
        }

        ItemStack token = plugin.getSkinManager().createToken(skin, amount);
        target.getInventory().addItem(token);

        sender.sendMessage(plugin.getMainConfig().message("skin-given")
                .replace("%player%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%skin%", skin.getDisplayName()));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Registered skins (" + plugin.getSkinManager().all().size() + "):");
        for (SkinDefinition skin : plugin.getSkinManager().all()) {
            sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.RESET + skin.getId()
                    + ChatColor.GRAY + " (" + skin.getDisplayName() + ChatColor.GRAY + ")");
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("cosmeticskins.admin")) {
            sender.sendMessage(plugin.getMainConfig().message("no-permission"));
            return;
        }
        plugin.reloadPlugin();
        sender.sendMessage(plugin.getMainConfig().message("reloaded"));
    }
}
