package com.cosmeticskins.plugin.model;

import org.bukkit.Material;

/**
 * A single named slot in the /cosmetic GUI (e.g. "helmet", "pickaxe"),
 * loaded from config.yml's "categories" section. Only skin tokens whose
 * {@link SkinDefinition#getCategory()} matches this category's id can be
 * placed in its slot.
 */
public class SkinCategory {

    private final String id;
    private final String displayName;
    private final int slot;
    private final Material icon;

    public SkinCategory(String id, String displayName, int slot, Material icon) {
        this.id = id;
        this.displayName = displayName;
        this.slot = slot;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlot() {
        return slot;
    }

    public Material getIcon() {
        return icon;
    }
}
