package com.cosmeticskins.plugin;

import com.cosmeticskins.plugin.command.CosmeticCommand;
import com.cosmeticskins.plugin.listener.GUIListener;
import com.cosmeticskins.plugin.listener.ItemSkinListener;
import com.cosmeticskins.plugin.manager.CategoryManager;
import com.cosmeticskins.plugin.manager.MainConfig;
import com.cosmeticskins.plugin.manager.PlayerDataManager;
import com.cosmeticskins.plugin.manager.SkinManager;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CosmeticSkins extends JavaPlugin {

    private MainConfig mainConfig;
    private CategoryManager categoryManager;
    private SkinManager skinManager;
    private PlayerDataManager playerDataManager;
    private ItemSkinListener itemSkinListener;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // creates config.yml on first run; skins.yml is handled by SkinManager

        this.mainConfig = new MainConfig(this);
        this.categoryManager = new CategoryManager(this);
        this.skinManager = new SkinManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.itemSkinListener = new ItemSkinListener(this);

        categoryManager.loadCategories();
        skinManager.loadSkins();

        getServer().getPluginManager().registerEvents(itemSkinListener, this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onQuit(PlayerQuitEvent event) {
                playerDataManager.unload(event.getPlayer().getUniqueId());
            }
        }, this);

        CosmeticCommand executor = new CosmeticCommand(this);
        getCommand("cosmetic").setExecutor(executor);

        getLogger().info("CosmeticSkins enabled.");
    }

    /** Reloads config.yml and skins.yml from disk without a server restart. */
    public void reloadPlugin() {
        reloadConfig();
        categoryManager.loadCategories();
        skinManager.loadSkins();
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ItemSkinListener getItemSkinListener() {
        return itemSkinListener;
    }
}
