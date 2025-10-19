package net.blackredcoded.brassmanmod.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.items.upgrades.UpgradeModuleItem;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JeiPlugin
public class JEIBrassManPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(BrassManMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new UpgradeRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<UpgradeRecipeDisplay> recipes = generateAllUpgradeRecipes();
        registration.addRecipes(UpgradeRecipeCategory.RECIPE_TYPE, recipes);
    }

    private List<UpgradeRecipeDisplay> generateAllUpgradeRecipes() {
        List<UpgradeRecipeDisplay> recipes = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof UpgradeModuleItem upgradeModule) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

                if (itemId == null || !itemId.getNamespace().equals(BrassManMod.MOD_ID)) {
                    continue;
                }

                String upgradeType = upgradeModule.getUpgradeType();
                int maxStacks = upgradeModule.getMaxStacksPerArmor();
                Set<Item> applicableItems = upgradeModule.getApplicableItems();

                for (Item applicableItem : applicableItems) {
                    List<ItemStack> baseItems = new ArrayList<>();
                    List<ItemStack> results = new ArrayList<>();

                    for (int level = 0; level < maxStacks; level++) {
                        ItemStack baseItem = createPreparedStack(applicableItem);

                        // SPECIAL CASE: Remote Assembly doesn't use the standard upgrade system!
                        if (upgradeType.equals("remote_assembly") && applicableItem instanceof BrassManChestplateItem) {
                            ArmorUpgradeHelper.setRemoteAssemblyLevel(baseItem, level);
                        } else {
                            // Regular upgrades - add them normally
                            for (int i = 0; i < level; i++) {
                                ArmorUpgradeHelper.addUpgrade(baseItem, upgradeType, maxStacks);
                            }
                        }

                        baseItems.add(baseItem);

                        ItemStack result = createPreparedStack(applicableItem);

                        if (upgradeType.equals("remote_assembly") && applicableItem instanceof BrassManChestplateItem) {
                            ArmorUpgradeHelper.setRemoteAssemblyLevel(result, level + 1);
                        } else {
                            for (int i = 0; i <= level; i++) {
                                ArmorUpgradeHelper.addUpgrade(result, upgradeType, maxStacks);
                            }
                        }

                        results.add(result);
                    }

                    if (!baseItems.isEmpty() && !results.isEmpty()) {
                        ItemStack upgradeItem = new ItemStack(upgradeModule);
                        recipes.add(new UpgradeRecipeDisplay(baseItems, upgradeItem, results));
                    }
                }
            }
        }

        return recipes;
    }

    /**
     * Creates a properly initialized ItemStack for JEI display.
     * For chestplates, sets Air and Power to max so tooltip displays correctly.
     */
    private ItemStack createPreparedStack(Item item) {
        ItemStack stack = new ItemStack(item);

        // Special handling for Brass Man Chestplate - set air and power to max
        if (item instanceof BrassManChestplateItem chestplate) {
            int maxAir = BrassManChestplateItem.getMaxAir(stack);
            int maxPower = BrassManChestplateItem.getMaxPower(stack);
            chestplate.setAirAndPower(stack, maxAir, maxPower);
        }

        return stack;
    }
}
