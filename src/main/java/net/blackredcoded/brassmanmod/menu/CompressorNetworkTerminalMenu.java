package net.blackredcoded.brassmanmod.menu;

import net.blackredcoded.brassmanmod.blockentity.CompressorNetworkTerminalBlockEntity;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class CompressorNetworkTerminalMenu extends AbstractContainerMenu {
    private final CompressorNetworkTerminalBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private List<BlockPos> compressorPositions = new ArrayList<>();
    private List<Component> compressorNames = new ArrayList<>();

    // Client constructor
    public CompressorNetworkTerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // Server constructor
    public CompressorNetworkTerminalMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.COMPRESSOR_NETWORK_TERMINAL_MENU.get(), containerId);

        if (blockEntity instanceof CompressorNetworkTerminalBlockEntity terminal) {
            this.blockEntity = terminal;
        } else {
            throw new IllegalStateException("Incorrect block entity type!");
        }

        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // No slots - this menu is purely for the button interface
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No slots to shift-click
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public CompressorNetworkTerminalBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public List<BlockPos> getCompressorPositions() {
        return blockEntity.getConnectedCompressors();
    }

    public List<Component> getCompressorNames() {
        return blockEntity.getCompressorNames();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        // Update cached data
        if (this.blockEntity != null) {
            this.compressorPositions = blockEntity.getConnectedCompressors();
            this.compressorNames = blockEntity.getCompressorNames();
        }
    }

    // Add this getter method
    public List<Boolean> getCompressorPowerStatus() {
        if (blockEntity != null) {
            return blockEntity.getCompressorPowerStatus();
        }
        return new ArrayList<>();
    }
}
