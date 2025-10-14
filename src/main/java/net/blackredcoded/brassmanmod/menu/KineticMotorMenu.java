package net.blackredcoded.brassmanmod.menu;

import net.blackredcoded.brassmanmod.blockentity.KineticMotorBlockEntity;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KineticMotorMenu extends AbstractContainerMenu {
    private final KineticMotorBlockEntity blockEntity;

    // Server constructor
    public KineticMotorMenu(int id, Inventory playerInventory, KineticMotorBlockEntity blockEntity) {
        super(ModMenuTypes.KINETIC_MOTOR_MENU.get(), id);
        this.blockEntity = blockEntity;

        // Battery slot (centered)
        this.addSlot(new Slot(blockEntity, 0, 80, 35));

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

    // Client constructor
    public KineticMotorMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, (KineticMotorBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 0) {
                // From battery slot to player inventory
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to battery slot
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.blockEntity.stillValid(player);
    }

    public KineticMotorBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
