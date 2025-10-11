package net.blackredcoded.brassmanmod.menu;

import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RemoteSuitMenu extends AbstractContainerMenu {
    private final BlockPos compressorPos;
    private final Level level;
    private final ContainerData data;

    // Client constructor
    public RemoteSuitMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
    }

    // Server constructor
    public RemoteSuitMenu(int containerId, Inventory playerInventory, BlockPos compressorPos) {
        super(ModMenuTypes.REMOTE_SUIT_MENU.get(), containerId);
        this.compressorPos = compressorPos;
        this.level = playerInventory.player.level();
        this.data = new SimpleContainerData(3);
        this.addDataSlots(this.data);
    }

    @Override
    public boolean stillValid(Player player) {
        // Always valid - no distance check!
        return true;
    }

    public BlockPos getCompressorPos() {
        return compressorPos;
    }

    public AirCompressorBlockEntity getBlockEntity() {
        if (level != null) {
            BlockEntity be = level.getBlockEntity(compressorPos);
            if (be instanceof AirCompressorBlockEntity compressor) {
                return compressor;
            }
        }
        return null;
    }

    public int getMaterial(int type) {
        return this.data.get(type);
    }

    public ItemStack[] getArmorStacks() {
        // Get the armor stand block entity (1 block above the compressor)
        BlockPos armorStandPos = compressorPos.above();
        if (level != null) {
            BlockEntity be = level.getBlockEntity(armorStandPos);
            if (be instanceof BrassArmorStandBlockEntity armorStand) {
                return new ItemStack[] {
                        armorStand.getArmor(0).copy(), // helmet
                        armorStand.getArmor(1).copy(), // chestplate
                        armorStand.getArmor(2).copy(), // leggings
                        armorStand.getArmor(3).copy()  // boots
                };
            }
        }

        // Return empty stacks if armor stand not found
        return new ItemStack[] {
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY
        };
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // Only update on server side
        if (!level.isClientSide) {
            AirCompressorBlockEntity blockEntity = getBlockEntity();
            if (blockEntity != null) {
                for (int i = 0; i < 3; i++) {
                    this.data.set(i, blockEntity.getMaterial(i));
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No slots to move
    }
}
