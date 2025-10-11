package net.blackredcoded.brassmanmod.blockentity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.jetbrains.annotations.Nullable;
import net.blackredcoded.brassmanmod.blocks.AirCompressorBlock;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DataLinkBlockEntity extends BlockEntity {

    // Single frequency item (not a slot, just stored data)
    private ItemStack frequencyItem = ItemStack.EMPTY;

    // Linked Air Compressor position
    private BlockPos linkedCompressorPos = null;

    public DataLinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DATA_LINK.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            scanForCompressor();
        }
    }

    /**
     * Scan for nearby Air Compressor to link to
     */
    public void scanForCompressor() {
        if (level == null || level.isClientSide) return;

        // Check all 6 adjacent positions
        for (var direction : net.minecraft.core.Direction.values()) {
            BlockPos checkPos = worldPosition.relative(direction);
            BlockState checkState = level.getBlockState(checkPos);

            // If it's an Air Compressor, link to it
            if (checkState.getBlock() instanceof AirCompressorBlock) {
                linkedCompressorPos = checkPos;
                setChanged();
                return;
            }
        }

        // No compressor found
        linkedCompressorPos = null;
        setChanged();
    }

    public ItemStack getFrequencyItem() {
        return frequencyItem;
    }

    public void setFrequencyItem(ItemStack item) {
        this.frequencyItem = item.isEmpty() ? ItemStack.EMPTY : item.copy();
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /**
     * Check if two Data Links have matching frequencies
     */
    public boolean hasSameFrequency(DataLinkBlockEntity other) {
        if (this.frequencyItem.isEmpty() || other.frequencyItem.isEmpty()) {
            return false;
        }

        return ItemStack.isSameItemSameComponents(this.frequencyItem, other.frequencyItem);
    }

    public BlockPos getLinkedCompressorPos() {
        return linkedCompressorPos;
    }

    public boolean isLinked() {
        return linkedCompressorPos != null && !frequencyItem.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        // Safe handling for empty ItemStack
        if (frequencyItem.isEmpty()) {
            tag.putBoolean("HasFrequency", false);
        } else {
            tag.putBoolean("HasFrequency", true);
            tag.put("FrequencyItem", frequencyItem.save(registries));
        }

        if (linkedCompressorPos != null) {
            tag.putLong("LinkedCompressor", linkedCompressorPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Load frequency item safely
        if (tag.getBoolean("HasFrequency") && tag.contains("FrequencyItem")) {
            frequencyItem = ItemStack.parse(registries, tag.getCompound("FrequencyItem")).orElse(ItemStack.EMPTY);
        } else {
            frequencyItem = ItemStack.EMPTY;
        }

        if (tag.contains("LinkedCompressor")) {
            linkedCompressorPos = BlockPos.of(tag.getLong("LinkedCompressor"));
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }
}
