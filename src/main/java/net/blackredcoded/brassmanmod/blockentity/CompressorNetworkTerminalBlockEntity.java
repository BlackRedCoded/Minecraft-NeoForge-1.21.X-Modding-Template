package net.blackredcoded.brassmanmod.blockentity;

import net.blackredcoded.brassmanmod.menu.CompressorNetworkTerminalMenu;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompressorNetworkTerminalBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_CONNECTIONS = 24;
    private static final int SCAN_RADIUS = 128; // Scan within 128 blocks radius (8 chunks)

    // Frequency item for matching with Data Links
    private ItemStack frequencyItem = ItemStack.EMPTY;

    // Connected compressor positions and their custom names & stats
    private final List<BlockPos> connectedCompressors = new ArrayList<>();
    private final List<Component> compressorNames = new ArrayList<>();
    private final List<Boolean> compressorPowerStatus = new ArrayList<>();

    // Update counter
    private int tickCounter = 0;

    public CompressorNetworkTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSOR_NETWORK_TERMINAL.get(), pos, state);
    }

    // This is called by the ticker in the block class
    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, CompressorNetworkTerminalBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.tickCounter++;

        // Update connections every 20 ticks (1 second)
        if (blockEntity.tickCounter % 20 == 0) {
            blockEntity.updateConnections();
        }
    }

    /**
     * Scan all loaded Data Links with matching frequency and collect their compressor positions
     */
    private void updateConnections() {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel) || frequencyItem.isEmpty()) {
            if (!connectedCompressors.isEmpty()) {
                connectedCompressors.clear();
                compressorNames.clear();
                compressorPowerStatus.clear(); // Clear power status too
                setChanged();
            }
            return;
        }

        List<BlockPos> newConnections = new ArrayList<>();
        List<Component> newNames = new ArrayList<>();
        List<Boolean> newPowerStatus = new ArrayList<>(); // NEW: Create this list

        // Calculate chunk range to scan
        int startChunkX = (worldPosition.getX() - SCAN_RADIUS) >> 4;
        int endChunkX = (worldPosition.getX() + SCAN_RADIUS) >> 4;
        int startChunkZ = (worldPosition.getZ() - SCAN_RADIUS) >> 4;
        int endChunkZ = (worldPosition.getZ() + SCAN_RADIUS) >> 4;

        // Iterate through loaded chunks in range
        for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
            for (int chunkZ = startChunkZ; chunkZ <= endChunkZ; chunkZ++) {
                if (serverLevel.hasChunk(chunkX, chunkZ)) {
                    LevelChunk chunk = serverLevel.getChunk(chunkX, chunkZ);

                    // Iterate through all block entities in this chunk
                    for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof DataLinkBlockEntity dataLink) {
                            // Check if frequencies match
                            if (hasSameFrequency(dataLink) && dataLink.isLinked()) {
                                BlockPos compressorPos = dataLink.getLinkedCompressorPos();

                                if (compressorPos != null && newConnections.size() < MAX_CONNECTIONS) {
                                    // Avoid duplicates
                                    if (!newConnections.contains(compressorPos)) {
                                        newConnections.add(compressorPos);

                                        // Get custom name from Air Compressor
                                        Component name = getCompressorName(compressorPos);
                                        newNames.add(name);

                                        // NEW: Check if compressor has power
                                        boolean hasPower = checkCompressorPower(compressorPos);
                                        newPowerStatus.add(hasPower);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Update if connections, names, OR power status changed
        boolean connectionsChanged = !newConnections.equals(connectedCompressors);
        boolean namesChanged = !newNames.equals(compressorNames);
        boolean powerChanged = !newPowerStatus.equals(compressorPowerStatus); // NEW

        if (connectionsChanged || namesChanged || powerChanged) {
            connectedCompressors.clear();
            connectedCompressors.addAll(newConnections);
            compressorNames.clear();
            compressorNames.addAll(newNames);
            compressorPowerStatus.clear(); // NEW
            compressorPowerStatus.addAll(newPowerStatus); // NEW
            setChanged();
        }
    }

    /**
     * Get the custom name of an Air Compressor
     */
    private Component getCompressorName(BlockPos pos) {
        if (level != null && level.getBlockEntity(pos) instanceof AirCompressorBlockEntity compressor) {
            return compressor.getCustomName();
        }
        return Component.literal("Air Compressor #" + (connectedCompressors.size() + 1));
    }

    /**
     * Check if this terminal's frequency matches a Data Link's frequency
     */
    private boolean hasSameFrequency(DataLinkBlockEntity dataLink) {
        if (frequencyItem.isEmpty() || dataLink.getFrequencyItem().isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(frequencyItem, dataLink.getFrequencyItem());
    }

    public ItemStack getFrequencyItem() {
        return frequencyItem;
    }

    public void setFrequencyItem(ItemStack item) {
        this.frequencyItem = item.isEmpty() ? ItemStack.EMPTY : item.copy();
        this.connectedCompressors.clear();
        this.compressorNames.clear();
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public List<BlockPos> getConnectedCompressors() {
        return new ArrayList<>(connectedCompressors);
    }

    public List<Component> getCompressorNames() {
        return new ArrayList<>(compressorNames);
    }

    // NEW GETTER
    public List<Boolean> getCompressorPowerStatus() {
        return new ArrayList<>(compressorPowerStatus);
    }

    public int getConnectionCount() {
        return connectedCompressors.size();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        // Save frequency
        if (frequencyItem.isEmpty()) {
            tag.putBoolean("HasFrequency", false);
        } else {
            tag.putBoolean("HasFrequency", true);
            tag.put("FrequencyItem", frequencyItem.save(registries));
        }

        // Save connected compressor positions
        ListTag posList = new ListTag();
        for (BlockPos pos : connectedCompressors) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            posList.add(posTag);
        }
        tag.put("Connections", posList);

        // Save custom names
        ListTag namesList = new ListTag();
        for (Component name : compressorNames) {
            CompoundTag nameTag = new CompoundTag();
            nameTag.putString("Name", Component.Serializer.toJson(name, registries));
            namesList.add(nameTag);
        }
        tag.put("Names", namesList);

        // NEW: Save power status
        ListTag powerList = new ListTag();
        for (Boolean hasPower : compressorPowerStatus) {
            CompoundTag powerTag = new CompoundTag();
            powerTag.putBoolean("HasPower", hasPower);
            powerList.add(powerTag);
        }
        tag.put("PowerStatus", powerList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Load frequency
        if (tag.getBoolean("HasFrequency") && tag.contains("FrequencyItem")) {
            frequencyItem = ItemStack.parse(registries, tag.getCompound("FrequencyItem")).orElse(ItemStack.EMPTY);
        } else {
            frequencyItem = ItemStack.EMPTY;
        }

        // Load connected compressor positions
        connectedCompressors.clear();
        ListTag posList = tag.getList("Connections", Tag.TAG_COMPOUND);
        for (int i = 0; i < posList.size(); i++) {
            CompoundTag posTag = posList.getCompound(i);
            connectedCompressors.add(BlockPos.of(posTag.getLong("Pos")));
        }

        // Load custom names
        compressorNames.clear();
        ListTag namesList = tag.getList("Names", Tag.TAG_COMPOUND);
        for (int i = 0; i < namesList.size(); i++) {
            CompoundTag nameTag = namesList.getCompound(i);
            String nameJson = nameTag.getString("Name");
            Component name = Component.Serializer.fromJson(nameJson, registries);
            compressorNames.add(name != null ? name : Component.literal("Air Compressor"));
        }
        // NEW: Load power status
        compressorPowerStatus.clear();
        ListTag powerList = tag.getList("PowerStatus", Tag.TAG_COMPOUND);
        for (int i = 0; i < powerList.size(); i++) {
            CompoundTag powerTag = powerList.getCompound(i);
            compressorPowerStatus.add(powerTag.getBoolean("HasPower"));
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.brassmanmod.compressor_network_terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CompressorNetworkTerminalMenu(id, playerInventory, this);
    }

    /**
     * Check if an Air Compressor has rotational power
     */
    private boolean checkCompressorPower(BlockPos pos) {
        if (level != null && level.getBlockEntity(pos) instanceof AirCompressorBlockEntity compressor) {
            // Create mod uses getSpeed() to check if kinetic block is powered
            // If speed > 0, it has power
            return Math.abs(compressor.getSpeed()) > 0;
        }
        return false;
    }

}
