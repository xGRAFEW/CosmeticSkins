package com.cosmeticskins.plugin.manager;

import com.cosmeticskins.plugin.CosmeticSkins;
import com.cosmeticskins.plugin.model.SkinDefinition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads skin definitions from skins.yml and handles creating / recognising
 * the physical "skin token" items that players drag into the GUI.
 */
public class SkinManager {

    private final CosmeticSkins plugin;
    private final Map<String, SkinDefinition> skins = new HashMap<>();

    public final NamespacedKey keySkinId;      // tag on a TOKEN item: which skin it grants
    public final NamespacedKey keyAppliedSkin; // tag on a TOOL item: which skin is currently applied to it

    // Namespace is a fixed literal, not derived from plugin.getName(), so PDC tags on
    // items players have already reskinned keep resolving even if the plugin's display
    // name in plugin.yml changes (e.g. gains a version suffix) across releases.
    public SkinManager(CosmeticSkins plugin) {
        this.plugin = plugin;
        this.keySkinId = new NamespacedKey("cosmeticskins", "skin_token_id");
        this.keyAppliedSkin = new NamespacedKey("cosmeticskins", "applied_skin_id");
    }

    public void loadSkins() {
        skins.clear();

        File file = new File(plugin.getDataFolder(), "skins.yml");
        if (!file.exists()) {
            plugin.saveResource("skins.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("skins");
        if (section == null) {
            plugin.getLogger().warning("skins.yml has no 'skins' section — nothing loaded.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection skinSection = section.getConfigurationSection(id);
            if (skinSection == null) continue;

            String displayName = ChatColor.translateAlternateColorCodes('&',
                    skinSection.getString("display-name", id));
            int customModelData = skinSection.getInt("custom-model-data", 0);

            String category = skinSection.getString("category");
            if (category == null || plugin.getCategoryManager().get(category) == null) {
                plugin.getLogger().warning("Skin '" + id + "' has a missing or unknown 'category' ('"
                        + category + "') — skipping. Add a matching entry under config.yml's 'categories' section.");
                continue;
            }

            List<String> materialNames = skinSection.getStringList("allowed-materials");
            Set<Material> materials = EnumSet.noneOf(Material.class);
            for (String name : materialNames) {
                Material material = Material.matchMaterial(name);
                if (material != null) {
                    materials.add(material);
                } else {
                    plugin.getLogger().warning("Unknown material '" + name + "' in skin '" + id + "'.");
                }
            }

            if (materials.isEmpty()) {
                plugin.getLogger().warning("Skin '" + id + "' has no valid allowed-materials — skipping.");
                continue;
            }

            String tokenIconName = skinSection.getString("token-icon");
            Material tokenIcon = null;
            if (tokenIconName != null) {
                tokenIcon = Material.matchMaterial(tokenIconName);
            }
            if (tokenIcon == null) {
                tokenIcon = plugin.getMainConfig().tokenMaterial();
            }

            skins.put(id, new SkinDefinition(id, displayName, customModelData, materials, tokenIcon, category));
        }

        plugin.getLogger().info("Loaded " + skins.size() + " cosmetic skin(s).");
    }

    public SkinDefinition get(String id) {
        return skins.get(id);
    }

    public Collection<SkinDefinition> all() {
        return skins.values();
    }

    /** Builds a physical "skin token" item stack for the given skin. */
    public ItemStack createToken(SkinDefinition skin, int amount) {
        ItemStack item = new ItemStack(skin.getTokenIcon(), amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(skin.getDisplayName());
        meta.setLore(List.of(
                ChatColor.GRAY + "Cosmetic Skin Token",
                ChatColor.DARK_GRAY + "Drop this into a /cosmetic slot",
                ChatColor.DARK_GRAY + "to apply this skin to a matching item."
        ));
        meta.getPersistentDataContainer().set(keySkinId, PersistentDataType.STRING, skin.getId());
        item.setItemMeta(meta);
        return item;
    }

    /** Returns the skin id stored on this item if it's a valid token, otherwise null. */
    public String readTokenSkinId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(keySkinId, PersistentDataType.STRING);
    }

    public boolean isToken(ItemStack item) {
        return readTokenSkinId(item) != null;
    }
}
