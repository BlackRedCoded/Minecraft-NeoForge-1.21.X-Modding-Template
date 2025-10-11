package net.blackredcoded.brassmanmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class CompressorRegistry {
    // Maps player UUID to list of their compressor positions
    private static final Map<UUID, Set<BlockPos>> PLAYER_COMPRESSORS = new HashMap<>();

    public static void registerCompressor(UUID playerUUID, BlockPos pos) {
        PLAYER_COMPRESSORS.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(pos);
    }

    public static void unregisterCompressor(UUID playerUUID, BlockPos pos) {
        Set<BlockPos> compressors = PLAYER_COMPRESSORS.get(playerUUID);
        if (compressors != null) {
            compressors.remove(pos);
            if (compressors.isEmpty()) {
                PLAYER_COMPRESSORS.remove(playerUUID);
            }
        }
    }

    public static Set<BlockPos> getPlayerCompressors(ServerPlayer player) {
        return PLAYER_COMPRESSORS.getOrDefault(player.getUUID(), Collections.emptySet());
    }

    public static void clear() {
        PLAYER_COMPRESSORS.clear();
    }
}
