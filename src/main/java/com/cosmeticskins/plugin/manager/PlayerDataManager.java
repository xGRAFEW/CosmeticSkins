package com.cosmeticskins.plugin.manager;

import com.cosmeticskins.plugin.CosmeticSkins;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks, per player, which skin id is equipped in which GUI slot, and
 * persists it to disk so it survives restarts.
 *
 * Storage layout: plugins/CosmeticSkins/playerdata/<uuid>.yml
 *   slots:
 *     0: starwar_shovel
 *     3: flame_sword
 */
public class PlayerDataManager {

    private final CosmeticSkins plugin;
    private final File folder;

    // uuid -> (slotIndex -> skinId)
    private final Map<UUID, Map<Integer, String>> cache = new ConcurrentHashMap<>();

    public PlayerDataManager(CosmeticSkins plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "playerdata");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public Map<Integer, String> getEquipped(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> loadFromDisk(id));
    }

    public void setSlot(UUID uuid, int slot, String skinId) {
        getEquipped(uuid).put(slot, skinId);
        saveToDisk(uuid);
    }

    public void clearSlot(UUID uuid, int slot) {
        getEquipped(uuid).remove(slot);
        saveToDisk(uuid);
    }

    public void unload(UUID uuid) {
        // Keep the in-memory cache small; data is already persisted on every change.
        cache.remove(uuid);
    }

    private Map<Integer, String> loadFromDisk(UUID uuid) {
        Map<Integer, String> result = new HashMap<>();
        File file = new File(folder, uuid + ".yml");
        if (!file.exists()) {
            return result;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        var section = config.getConfigurationSection("slots");
        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            try {
                int slot = Integer.parseInt(key);
                String skinId = section.getString(key);
                if (skinId != null) {
                    result.put(slot, skinId);
                }
            } catch (NumberFormatException ignored) {
                // corrupt key, skip it
            }
        }
        return result;
    }

    private void saveToDisk(UUID uuid) {
        File file = new File(folder, uuid + ".yml");
        FileConfiguration config = new YamlConfiguration();
        Map<Integer, String> equipped = cache.getOrDefault(uuid, Map.of());

        for (Map.Entry<Integer, String> entry : equipped.entrySet()) {
            config.set("slots." + entry.getKey(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player data for " + uuid + ": " + e.getMessage());
        }
    }
}
