package com.cosmeticskins.plugin.listener;

import com.cosmeticskins.plugin.CosmeticSkins;
import com.cosmeticskins.plugin.gui.CosmeticGUI;
import com.cosmeticskins.plugin.manager.SkinManager;
import com.cosmeticskins.plugin.model.SkinDefinition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GUIListener implements Listener {

    private final CosmeticSkins plugin;

    public GUIListener(CosmeticSkins plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof CosmeticGUI) {
            // Dragging across multiple slots at once is disallowed to keep the
            // "one token per slot" logic simple and predictable.
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (!(topHolder instanceof CosmeticGUI gui)) {
            return; // not our GUI
        }

        // Only handle clicks that land in the top (GUI) inventory.
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || clicked != event.getView().getTopInventory()) {
            // Click was in the player's own inventory while our GUI is open — allow it,
            // but block shift-clicking items up into the GUI to avoid bypassing the token check.
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        // Every click inside the GUI itself is handled manually.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int rawSlot = event.getSlot();
        if (!gui.isEquipSlot(rawSlot)) {
            return; // clicked the decorative border
        }

        if (event.isShiftClick() || event.getClick().isKeyboardClick()) {
            return; // keep interaction simple: left/right click with the cursor only
        }

        SkinManager skinManager = plugin.getSkinManager();
        Map<Integer, String> equipped = plugin.getPlayerDataManager().getEquipped(player.getUniqueId());
        String currentSkinId = equipped.get(rawSlot);
        ItemStack cursor = event.getCursor();

        boolean slotOccupied = currentSkinId != null;
        boolean cursorHasItem = cursor != null && !cursor.getType().isAir();

        if (!slotOccupied && cursorHasItem) {
            // Trying to place a token into an empty slot.
            String tokenSkinId = skinManager.readTokenSkinId(cursor);
            if (tokenSkinId == null) {
                player.sendMessage(plugin.getMainConfig().message("wrong-item"));
                return;
            }
            SkinDefinition skin = skinManager.get(tokenSkinId);
            if (skin == null) {
                player.sendMessage(plugin.getMainConfig().message("skin-not-found")
                        .replace("%id%", tokenSkinId));
                return;
            }

            // Consume exactly one token from the cursor stack.
            cursor.setAmount(cursor.getAmount() - 1);
            event.setCursor(cursor.getAmount() <= 0 ? null : cursor);

            plugin.getPlayerDataManager().setSlot(player.getUniqueId(), rawSlot, skin.getId());
            gui.refresh();
            plugin.getItemSkinListener().applyAllEquippedSkins(player);

            player.sendMessage(plugin.getMainConfig().message("skin-equipped")
                    .replace("%skin%", skin.getDisplayName()));

        } else if (slotOccupied && !cursorHasItem) {
            // Taking a token back out.
            SkinDefinition skin = skinManager.get(currentSkinId);

            plugin.getPlayerDataManager().clearSlot(player.getUniqueId(), rawSlot);
            gui.refresh();

            if (skin != null) {
                event.setCursor(skinManager.createToken(skin, 1));
                plugin.getItemSkinListener().revertSkin(player, skin);
                player.sendMessage(plugin.getMainConfig().message("skin-unequipped")
                        .replace("%skin%", skin.getDisplayName()));
            }
        }
        // Any other combination (swapping, occupied+cursor full, etc.) is ignored —
        // the player must remove the current token before placing a new one.
    }
}
