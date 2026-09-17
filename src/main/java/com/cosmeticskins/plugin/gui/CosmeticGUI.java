package com.cosmeticskins.plugin.gui;

import com.cosmeticskins.plugin.CosmeticSkins;
import com.cosmeticskins.plugin.model.SkinCategory;
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
 * A multi-row equip menu, laid out entirely from config.yml's "categories"
 * section (see {@link com.cosmeticskins.plugin.manager.CategoryManager}).
 * Each configured category owns exactly one raw slot and only accepts skin
 * tokens tagged with that category's id; every other slot is decorative
 * border. The inventory grows automatically to fit whatever slot indices
 * the configured categories use, up to a full double chest (54 slots).
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
        Map<Integer, SkinCategory> slots = plugin.getCategoryManager().slotMap();

        int maxSlot = 8;
        for (int slot : slots.keySet()) {
            maxSlot = Math.max(maxSlot, slot);
        }
        int size = Math.min(54, ((maxSlot / 9) + 1) * 9);

        inventory = plugin.getServer().createInventory(this, size, plugin.getMainConfig().guiTitle());
        refresh();
    }

    /** The category bound to this raw slot, or null if it's decorative border. */
    public SkinCategory categoryAt(int rawSlot) {
        return plugin.getCategoryManager().slotMap().get(rawSlot);
    }

    /** Which raw slot indices are usable equip slots (rest is decorative border). */
    public boolean isEquipSlot(int rawSlot) {
        return categoryAt(rawSlot) != null;
    }

    public void refresh() {
        ItemStack border = borderItem();
        Map<Integer, SkinCategory> slots = plugin.getCategoryManager().slotMap();
        Map<Integer, String> equipped = plugin.getPlayerDataManager().getEquipped(owner);

        for (int rawSlot = 0; rawSlot < inventory.getSize(); rawSlot++) {
            SkinCategory category = slots.get(rawSlot);
            if (category == null) {
                inventory.setItem(rawSlot, border);
                continue;
            }

            String skinId = equipped.get(rawSlot);
            if (skinId != null) {
                SkinDefinition skin = plugin.getSkinManager().get(skinId);
                if (skin != null) {
                    inventory.setItem(rawSlot, plugin.getSkinManager().createToken(skin, 1));
                    continue;
                }
            }
            inventory.setItem(rawSlot, emptySlotItem(category));
        }
    }

    private ItemStack borderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack emptySlotItem(SkinCategory category) {
        String name = ChatColor.stripColor(category.getDisplayName());
        ItemStack item = new ItemStack(category.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Empty " + name + " Slot");
        meta.setLore(List.of(ChatColor.GRAY + "Drop a " + name + " skin token here to equip it."));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
