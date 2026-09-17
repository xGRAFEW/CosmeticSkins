package com.cosmeticskins.plugin.model;

import org.bukkit.Material;

import java.util.Set;

/**
 * Immutable definition of one skin, loaded from skins.yml.
 */
public class SkinDefinition {

    private final String id;
    private final String displayName;
    private final int customModelData;
    private final Set<Material> allowedMaterials;
    private final Material tokenIcon;
    private final String category;

    public SkinDefinition(String id, String displayName, int customModelData,
                           Set<Material> allowedMaterials, Material tokenIcon, String category) {
        this.id = id;
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.allowedMaterials = allowedMaterials;
        this.tokenIcon = tokenIcon;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public Set<Material> getAllowedMaterials() {
        return allowedMaterials;
    }

    public Material getTokenIcon() {
        return tokenIcon;
    }

    /** Id of the GUI category (see {@link com.cosmeticskins.plugin.model.SkinCategory}) this skin's token belongs in. */
    public String getCategory() {
        return category;
    }

    public boolean supports(Material material) {
        return allowedMaterials.contains(material);
    }
}
