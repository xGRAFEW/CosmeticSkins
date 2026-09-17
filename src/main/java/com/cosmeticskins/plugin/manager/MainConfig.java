package com.cosmeticskins.plugin.manager;

import com.cosmeticskins.plugin.CosmeticSkins;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class MainConfig {

    private final CosmeticSkins plugin;

    public MainConfig(CosmeticSkins plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String guiTitle() {
        return ChatColor.translateAlternateColorCodes('&', cfg().getString("gui.title", "&5&lCosmetic Skins"));
    }

    public Material tokenMaterial() {
        Material material = Material.matchMaterial(cfg().getString("token-material", "PAPER"));
        return material != null ? material : Material.PAPER;
    }

    public String message(String key) {
        String raw = cfg().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', cfg().getString("messages.prefix", "") + raw);
    }
}
