package net.blackredcoded.brassmanmod.menu;

import net.blackredcoded.brassmanmod.items.BrassManChestplateItem;
import net.blackredcoded.brassmanmod.items.upgrades.UpgradeModuleItem;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModificationStationMenu extends AbstractContainerMenu {
    private final Container container;

    public static final int ARMOR_SLOT = 0;
    public static final int UPGRADE_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    public ModificationStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(3));
    }

    public ModificationStationMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.MODIFICATION_STATION.get(), containerId);
        this.container = container;
        checkContainerSize(container, 3);
        container.startOpen(playerInventory.player);

        // Armor input slot
        this.addSlot(new Slot(container, ARMOR_SLOT, 27, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return true;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ModificationStationMenu.this.slotsChanged(container);
            }
        });

        // Upgrade module slot
        this.addSlot(new Slot(container, UPGRADE_SLOT, 76, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof UpgradeModuleItem;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ModificationStationMenu.this.slotsChanged(container);
            }
        });

        // Result slot
        this.addSlot(new Slot(container, RESULT_SLOT, 134, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                ItemStack upgradeStack = container.getItem(UPGRADE_SLOT);

                // Store upgrade info BEFORE shrinking
                String upgradeType = null;
                if (upgradeStack.getItem() instanceof UpgradeModuleItem upgradeItem) {
                    upgradeType = upgradeItem.getUpgradeType();
                }

                if (!upgradeStack.isEmpty()) {
                    upgradeStack.shrink(1);
                }

                // Now check the stored type
                if ("remote_assembly".equals(upgradeType)) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        int newLevel = ArmorUpgradeHelper.getRemoteAssemblyLevel(stack);
                        System.out.println("DEBUG: Remote Assembly upgraded to level " + newLevel + " - syncing to set");
                        ArmorUpgradeHelper.syncUpgradeAcrossSet(serverLevel, stack, newLevel);
                    }
                }

                container.setItem(ARMOR_SLOT, ItemStack.EMPTY);
                ModificationStationMenu.this.slotsChanged(container);
                super.onTake(player, stack);
            }
        });

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        ItemStack armorStack = container.getItem(ARMOR_SLOT);
        ItemStack upgradeStack = container.getItem(UPGRADE_SLOT);

        if (armorStack.isEmpty() || upgradeStack.isEmpty()) {
            container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            return;
        }

        if (!(armorStack.getItem() instanceof ArmorItem)) {
            container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            return;
        }

        if (!(upgradeStack.getItem() instanceof UpgradeModuleItem upgradeItem)) {
            container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            return;
        }

        String upgradeType = upgradeItem.getUpgradeType();
        int maxAllowed = upgradeItem.getMaxStacksPerArmor();

        // SPECIAL CASE: Remote Assembly
        if ("remote_assembly".equals(upgradeType)) {
            if (!(armorStack.getItem() instanceof BrassManChestplateItem)) {
                container.setItem(RESULT_SLOT, ItemStack.EMPTY);
                return;
            }

            int currentLevel = ArmorUpgradeHelper.getRemoteAssemblyLevel(armorStack);
            if (currentLevel >= maxAllowed) {
                container.setItem(RESULT_SLOT, ItemStack.EMPTY);
                return;
            }

            ItemStack result = armorStack.copy();
            ArmorUpgradeHelper.setRemoteAssemblyLevel(result, currentLevel + 1);
            container.setItem(RESULT_SLOT, result);
            return;
        }

        // STANDARD UPGRADES
        int currentCount = ArmorUpgradeHelper.getUpgradeCount(armorStack, upgradeType);
        int totalUpgrades = ArmorUpgradeHelper.getTotalUpgradeCount(armorStack);

        if (currentCount >= maxAllowed) {
            container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            return;
        }

        if (totalUpgrades >= ArmorUpgradeHelper.MAX_TOTAL_UPGRADES) {
            container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            return;
        }

        ItemStack result = armorStack.copy();
        boolean success = ArmorUpgradeHelper.addUpgrade(result, upgradeType, maxAllowed);
        if (success) {
            container.setItem(RESULT_SLOT, result);
        } else {
            container.setItem(RESULT_SLOT, ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemStack);
            } else if (index >= 3) {
                if (slotStack.getItem() instanceof ArmorItem) {
                    if (!this.moveItemStackTo(slotStack, ARMOR_SLOT, ARMOR_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotStack.getItem() instanceof UpgradeModuleItem) {
                    if (!this.moveItemStackTo(slotStack, UPGRADE_SLOT, UPGRADE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 30) {
                    if (!this.moveItemStackTo(slotStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.clearContainer(player, this.container);
    }
}
