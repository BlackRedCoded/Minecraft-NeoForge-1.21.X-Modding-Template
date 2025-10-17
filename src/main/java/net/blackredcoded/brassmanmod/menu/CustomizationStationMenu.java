package net.blackredcoded.brassmanmod.menu;

import com.simibubi.create.AllItems;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.blackredcoded.brassmanmod.util.ArmorStyleHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class CustomizationStationMenu extends AbstractContainerMenu {

    private final DataSlot selectedStyle = DataSlot.standalone();

    public CustomizationStationMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.CUSTOMIZATION_STATION.get(), containerId);
        this.addDataSlot(selectedStyle);

        // Set initial style based on what player is wearing
        String currentStyle = getCurrentArmorStyle(playerInventory.player);
        selectedStyle.set(getStyleIndex(currentStyle));
    }

    private String getCurrentArmorStyle(Player player) {
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (chestplate.getItem() instanceof BrassManChestplateItem) {
            return ArmorStyleHelper.getArmorStyle(chestplate);
        }
        return ArmorStyleHelper.BRASS;
    }

    private int getStyleIndex(String style) {
        return switch (style) {
            case ArmorStyleHelper.AQUA -> 1;
            case ArmorStyleHelper.DARK_AQUA -> 2;
            case ArmorStyleHelper.FLAMING -> 3;
            default -> 0; // BRASS
        };
    }

    public void setSelectedStyle(int index) {
        selectedStyle.set(index);
    }

    public int getSelectedStyle() {
        return selectedStyle.get();
    }

    public String getSelectedStyleName() {
        return switch (selectedStyle.get()) {
            case 1 -> ArmorStyleHelper.AQUA;
            case 2 -> ArmorStyleHelper.DARK_AQUA;
            case 3 -> ArmorStyleHelper.FLAMING;
            default -> ArmorStyleHelper.BRASS;
        };
    }

    public boolean canAffordStyle(Player player, String style) {
        ItemStack[] costs = getStyleCosts(style);
        for (ItemStack cost : costs) {
            if (!hasEnoughItems(player, cost)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasEnoughItems(Player player, ItemStack required) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                count += stack.getCount();
            }
        }
        return count >= required.getCount();
    }

    public ItemStack[] getStyleCosts(String style) {
        return switch (style) {
            case ArmorStyleHelper.BRASS -> new ItemStack[]{
                    new ItemStack(Items.YELLOW_DYE, 3),
                    new ItemStack(Items.ORANGE_DYE, 3),
                    new ItemStack(AllItems.BRASS_INGOT.get(), 1) // Replace with Create brass when available
            };
            case ArmorStyleHelper.AQUA -> new ItemStack[]{
                    new ItemStack(Items.CYAN_DYE, 3),
                    new ItemStack(Items.LIGHT_BLUE_DYE, 3),
                    new ItemStack(Items.DIAMOND, 1)
            };
            case ArmorStyleHelper.DARK_AQUA -> new ItemStack[]{
                    new ItemStack(Items.CYAN_DYE, 3),
                    new ItemStack(Items.BLUE_DYE, 3),
                    new ItemStack(Items.LAPIS_LAZULI, 1)
            };
            case ArmorStyleHelper.FLAMING -> new ItemStack[]{
                    new ItemStack(Items.RED_DYE, 3),
                    new ItemStack(Items.YELLOW_DYE, 3),
                    new ItemStack(Items.BLAZE_POWDER, 1)
            };
            default -> new ItemStack[0];
        };
    }

    public boolean isWearingBrassArmor(Player player) {
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof BrassManHelmetItem &&
                player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof BrassManChestplateItem &&
                player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() instanceof BrassManLeggingsItem &&
                player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof BrassManBootsItem;
    }

    public void purchaseStyle(Player player, String style) {
        if (!isWearingBrassArmor(player)) return;
        if (!canAffordStyle(player, style)) return;

        // Consume costs
        ItemStack[] costs = getStyleCosts(style);
        for (ItemStack cost : costs) {
            removeItems(player, cost);
        }

        // Apply style to all worn armor
        applyStyleToArmor(player, style);
    }

    private void removeItems(Player player, ItemStack required) {
        int remaining = required.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    private void applyStyleToArmor(Player player, String style) {
        ArmorStyleHelper.setArmorStyle(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), style);
        ArmorStyleHelper.setArmorStyle(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST), style);
        ArmorStyleHelper.setArmorStyle(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS), style);
        ArmorStyleHelper.setArmorStyle(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET), style);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
