package net.blackredcoded.brassmanmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class CompressorRegistry extends SavedData {
    private static final String DATA_NAME = "brassmanmod_compressor_registry";

    // Maps player UUID to list of their compressor positions
    private final Map<UUID, Set<BlockPos>> playerCompressors = new HashMap<>();

    // Required constructor
    public CompressorRegistry() {
        super();
    }

    // Get or create the registry for a world
    public static CompressorRegistry get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(
                                CompressorRegistry::new,
                                CompressorRegistry::load
                        ),
                        DATA_NAME
                );
    }

    // Load data from disk
    public static CompressorRegistry load(CompoundTag tag, HolderLookup.Provider provider) {
        CompressorRegistry registry = new CompressorRegistry();
        ListTag playersList = tag.getList("Players", Tag.TAG_COMPOUND);

        for (int i = 0; i < playersList.size(); i++) {
            CompoundTag playerTag = playersList.getCompound(i);
            UUID playerUUID = playerTag.getUUID("UUID");
            ListTag positionsList = playerTag.getList("Positions", Tag.TAG_COMPOUND);

            Set<BlockPos> positions = new HashSet<>();
            for (int j = 0; j < positionsList.size(); j++) {
                CompoundTag posTag = positionsList.getCompound(j);
                BlockPos pos = NbtUtils.readBlockPos(posTag, "Pos").orElse(null);
                if (pos != null) {
                    positions.add(pos);
                }
            }

            if (!positions.isEmpty()) {
                registry.playerCompressors.put(playerUUID, positions);
            }
        }

        return registry;
    }

    // Save data to disk
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag playersList = new ListTag();

        for (Map.Entry<UUID, Set<BlockPos>> entry : playerCompressors.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", entry.getKey());

            ListTag positionsList = new ListTag();
            for (BlockPos pos : entry.getValue()) {
                CompoundTag container = new CompoundTag();
                container.put("Pos", NbtUtils.writeBlockPos(pos));
                positionsList.add(container);
            }

            playerTag.put("Positions", positionsList);
            playersList.add(playerTag);
        }

        tag.put("Players", playersList);
        return tag;
    }

    // Register a compressor
    public void registerCompressor(UUID playerUUID, BlockPos pos) {
        playerCompressors.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(pos);
        setDirty(); // Mark as changed so it saves!
    }

    // Unregister a compressor
    public void unregisterCompressor(UUID playerUUID, BlockPos pos) {
        Set<BlockPos> compressors = playerCompressors.get(playerUUID);
        if (compressors != null) {
            compressors.remove(pos);
            if (compressors.isEmpty()) {
                playerCompressors.remove(playerUUID);
            }
            setDirty(); // Mark as changed!
        }
    }

    // Get compressor positions
    public Set<BlockPos> getPlayerCompressors(UUID playerUUID) {
        return new HashSet<>(playerCompressors.getOrDefault(playerUUID, Collections.emptySet()));
    }

    // Static helper methods
    public static void registerCompressor(ServerLevel level, UUID playerUUID, BlockPos pos) {
        get(level).registerCompressor(playerUUID, pos);
    }

    public static void unregisterCompressor(ServerLevel level, UUID playerUUID, BlockPos pos) {
        get(level).unregisterCompressor(playerUUID, pos);
    }

    public static Set<BlockPos> getPlayerCompressors(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            return get(serverLevel).getPlayerCompressors(player.getUUID());
        }
        return Collections.emptySet();
    }
}
