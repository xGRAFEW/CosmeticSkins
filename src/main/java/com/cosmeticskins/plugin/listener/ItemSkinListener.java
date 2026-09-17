package com.cosmeticskins.plugin.listener;

import com.cosmeticskins.plugin.CosmeticSkins;
import com.cosmeticskins.plugin.manager.SkinManager;
import com.cosmeticskins.plugin.model.SkinDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;

/**
 * Keeps every matching item in a player's inventory visually in sync with
 * whichever skins they currently have equipped, without touching enchantments,
 * durability, item name/lore, or anything else about the item.
 */
public class ItemSkinListener implements Listener {

    private final CosmeticSkins plugin;

    public ItemSkinListener(CosmeticSkins plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /** Re-applies every skin currently equipped by this player to all matching items they own. */
    public void applyAllEquippedSkins(Player player) {
        Map<Integer, String> equipped = plugin.getPlayerDataManager().getEquipped(player.getUniqueId());
        if (equipped.isEmpty()) return;

        SkinManager skinManager = plugin.getSkinManager();
        for (String skinId : equipped.values()) {
            SkinDefinition skin = skinManager.get(skinId);
            if (skin != null) {
                applySkin(player, skin);
            }
        }
    }

    /** Applies a single skin to every matching item the player currently owns. */
    public void applySkin(Player player, SkinDefinition skin) {
        forEachSlot(player.getInventory(), item -> {
            if (item == null || item.getType() == Material.AIR) return null;
            if (!skin.supports(item.getType())) return null;
            return withSkinApplied(item, skin);
        });
    }

    /** Removes this skin's model data from every item the player owns that currently has it. */
    public void revertSkin(Player player, SkinDefinition skin) {
        forEachSlot(player.getInventory(), item -> {
            if (item == null || item.getType() == Material.AIR) return null;
            if (!skin.supports(item.getType())) return null;
            if (!hasAppliedSkin(item, skin.getId())) return null;
            return withSkinRemoved(item);
        });
    }

    // ---------------------------------------------------------------
    // Item mutation helpers
    // ---------------------------------------------------------------

    private ItemStack withSkinApplied(ItemStack item, SkinDefinition skin) {
        if (hasAppliedSkin(item, skin.getId())) {
            return null; // already correct, don't touch it
        }
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(skin.getCustomModelData());
        meta.getPersistentDataContainer().set(
                plugin.getSkinManager().keyAppliedSkin, PersistentDataType.STRING, skin.getId());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack withSkinRemoved(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(null);
        meta.getPersistentDataContainer().remove(plugin.getSkinManager().keyAppliedSkin);
        item.setItemMeta(meta);
        return item;
    }

    private boolean hasAppliedSkin(ItemStack item, String skinId) {
        if (!item.hasItemMeta()) return false;
        String applied = item.getItemMeta().getPersistentDataContainer()
                .get(plugin.getSkinManager().keyAppliedSkin, PersistentDataType.STRING);
        return skinId.equals(applied);
    }

    /**
     * Runs the given mutator over every storage slot, armor slot, and the off-hand
     * of a player's inventory, writing back any slot the mutator changed.
     */
    private void forEachSlot(PlayerInventory inventory, java.util.function.Function<ItemStack, ItemStack> mutator) {
        ItemStack[] storage = inventory.getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            ItemStack updated = mutator.apply(storage[i]);
            if (updated != null) storage[i] = updated;
        }
        inventory.setStorageContents(storage);

        ItemStack[] armor = inventory.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack updated = mutator.apply(armor[i]);
            if (updated != null) armor[i] = updated;
        }
        inventory.setArmorContents(armor);

        ItemStack offHand = inventory.getItemInOffHand();
        ItemStack updatedOffHand = mutator.apply(offHand);
        if (updatedOffHand != null) inventory.setItemInOffHand(updatedOffHand);
    }

    // ---------------------------------------------------------------
    // Events that should trigger a re-sync
    // ---------------------------------------------------------------

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Run next tick so the player's inventory is fully loaded first.
        plugin.getServer().getScheduler().runTask(plugin, () -> applyAllEquippedSkins(player));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> applyAllEquippedSkins(player));
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        applyAllEquippedSkins(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Covers crafting tables, anvils, chests, etc. — anything that could have
        // handed the player a fresh, unskinned copy of an item they have a skin for.
        if (event.getPlayer() instanceof Player player) {
            applyAllEquippedSkins(player);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            plugin.getServer().getScheduler().runTask(plugin, () -> applyAllEquippedSkins(player));
        }
    }
}
