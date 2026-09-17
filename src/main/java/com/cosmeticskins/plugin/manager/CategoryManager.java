package com.cosmeticskins.plugin.manager;

import com.cosmeticskins.plugin.CosmeticSkins;
import com.cosmeticskins.plugin.model.SkinCategory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads the /cosmetic GUI's category layout from config.yml's "categories"
 * section. Each category is one named slot (helmet, pickaxe, etc.) that only
 * accepts skin tokens tagged with a matching category id. Server owners can
 * add, remove, rename, reposition, or re-icon categories entirely from
 * config.yml — no code changes needed.
 */
public class CategoryManager {

    private final CosmeticSkins plugin;
    private final Map<String, SkinCategory> categories = new LinkedHashMap<>();
    private final Map<Integer, SkinCategory> bySlot = new LinkedHashMap<>();

    public CategoryManager(CosmeticSkins plugin) {
        this.plugin = plugin;
    }

    public void loadCategories() {
        categories.clear();
        bySlot.clear();

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("categories");
        if (section == null) {
            plugin.getLogger().warning("config.yml has no 'categories' section — the /cosmetic GUI will be empty.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection catSection = section.getConfigurationSection(id);
            if (catSection == null) continue;

            int slot = catSection.getInt("slot", -1);
            if (slot < 0 || slot > 53) {
                plugin.getLogger().warning("Category '" + id + "' has a missing or invalid 'slot' "
                        + "(must be 0-53) — skipping.");
                continue;
            }

            if (bySlot.containsKey(slot)) {
                plugin.getLogger().warning("Category '" + id + "' shares slot " + slot
                        + " with '" + bySlot.get(slot).getId() + "' — keeping '"
                        + bySlot.get(slot).getId() + "' and skipping '" + id + "'.");
                continue;
            }

            String displayName = ChatColor.translateAlternateColorCodes('&',
                    catSection.getString("display-name", id));

            Material icon = Material.matchMaterial(catSection.getString("icon", "PAPER"));
            if (icon == null) {
                plugin.getLogger().warning("Category '" + id + "' has an unknown 'icon' material — defaulting to PAPER.");
                icon = Material.PAPER;
            }

            SkinCategory category = new SkinCategory(id, displayName, slot, icon);
            categories.put(id, category);
            bySlot.put(slot, category);
        }

        plugin.getLogger().info("Loaded " + categories.size()
                + " GUI categor" + (categories.size() == 1 ? "y" : "ies") + ".");
    }

    public SkinCategory get(String id) {
        return categories.get(id);
    }

    public Collection<SkinCategory> all() {
        return categories.values();
    }

    /** Raw GUI slot index -> the category bound to it. */
    public Map<Integer, SkinCategory> slotMap() {
        return bySlot;
    }
}
