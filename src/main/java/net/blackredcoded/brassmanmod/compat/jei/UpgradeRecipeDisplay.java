package net.blackredcoded.brassmanmod.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class UpgradeRecipeDisplay {
    private final List<ItemStack> baseItems;  // Different levels (0, 1, 2, 3, 4 upgrades)
    private final ItemStack upgradeModule;
    private final List<ItemStack> results;    // Different result levels (1, 2, 3, 4, 5 upgrades)

    public UpgradeRecipeDisplay(List<ItemStack> baseItems, ItemStack upgradeModule, List<ItemStack> results) {
        this.baseItems = baseItems;
        this.upgradeModule = upgradeModule;
        this.results = results;
    }

    public List<ItemStack> getBaseItems() {
        return baseItems;
    }

    public ItemStack getUpgradeModule() {
        return upgradeModule;
    }

    public List<ItemStack> getResults() {
        return results;
    }
}
