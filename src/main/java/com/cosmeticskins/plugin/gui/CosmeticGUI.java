package com.cosmeticskins.plugin.gui;

import com.cosmeticskins.plugin.CosmeticSkins;
import com.cosmeticskins.plugin.model.SkinDefinition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A single-row equip menu. Each of the middle slots accepts a skin token;
 * everything else is a locked decorative border.
 *
 * Layout for equip-slots = 9 (fills the whole row):
 *   [0][1][2][3][4][5][6][7][8]   <- all equip slots
 *
 * Layout for equip-slots = 5 (border padding either side):
 *   [x][0][1][2][3][4][x][x][x]
 */
public class CosmeticGUI implements InventoryHolder {

    private final CosmeticSkins plugin;
    private final UUID owner;
    private Inventory inventory;

    public CosmeticGUI(CosmeticSkins plugin, Player owner) {
        this.plugin = plugin;
        this.owner = owner.getUniqueId();
        build();
    }

    private void build() {
        int size = 9; // single row
        inventory = plugin.getServer().createInventory(this, size, plugin.getMainConfig().guiTitle());
        refresh();
    }

    /** Which raw slot indices are usable equip slots (rest is decorative border). */
    public boolean isEquipSlot(int rawSlot) {
        int equipCount = plugin.getMainConfig().equipSlots();
        int start = (9 - equipCount) / 2;
        return rawSlot >= start && rawSlot < start + equipCount;
    }

    public void refresh() {
        int equipCount = plugin.getMainConfig().equipSlots();
        int start = (9 - equipCount) / 2;

        ItemStack border = borderItem();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
        }

        Map<Integer, String> equipped = plugin.getPlayerDataManager().getEquipped(owner);

        for (int i = 0; i < equipCount; i++) {
            int rawSlot = start + i;
            String skinId = equipped.get(rawSlot);

            if (skinId != null) {
                SkinDefinition skin = plugin.getSkinManager().get(skinId);
                if (skin != null) {
                    inventory.setItem(rawSlot, plugin.getSkinManager().createToken(skin, 1));
                    continue;
                }
            }
            inventory.setItem(rawSlot, emptySlotItem());
        }
    }

    private ItemStack borderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack emptySlotItem() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Empty Skin Slot");
        meta.setLore(List.of(ChatColor.GRAY + "Drop a skin token here to equip it."));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
