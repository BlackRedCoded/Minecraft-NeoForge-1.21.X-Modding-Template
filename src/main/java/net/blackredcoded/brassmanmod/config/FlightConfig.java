package net.blackredcoded.brassmanmod.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class FlightConfig {

    // Client-side cached config
    public static final PlayerFlightData CLIENT_CONFIG = new PlayerFlightData();

    public static class PlayerFlightData {
        public int flightSpeed = 20;
        public boolean flightEnabled = false;
        public boolean hoverEnabled = false;
        public boolean fallsaveEnabled = false;
        public boolean nightvisionEnabled = false;
        public int speedBoost = 0;
        public int jumpBoost = 0;

        public boolean fallsaveHover = false;
        public boolean fallsaveFlight = false;
        public int fallsavePowerToAir = 0;

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("FlightSpeed", flightSpeed);
            tag.putBoolean("FlightEnabled", flightEnabled);
            tag.putBoolean("HoverEnabled", hoverEnabled);
            tag.putBoolean("FallsaveEnabled", fallsaveEnabled);
            tag.putBoolean("NightvisionEnabled", nightvisionEnabled);
            tag.putInt("SpeedBoost", speedBoost);
            tag.putInt("JumpBoost", jumpBoost);
            tag.putBoolean("FallsaveHover", fallsaveHover);
            tag.putBoolean("FallsaveFlight", fallsaveFlight);
            tag.putInt("FallsavePowerToAir", fallsavePowerToAir);
            return tag;
        }

        public void load(CompoundTag tag) {
            flightSpeed = tag.getInt("FlightSpeed");
            flightEnabled = tag.getBoolean("FlightEnabled");
            hoverEnabled = tag.getBoolean("HoverEnabled");
            fallsaveEnabled = tag.getBoolean("FallsaveEnabled");
            nightvisionEnabled = tag.getBoolean("NightvisionEnabled");
            speedBoost = tag.getInt("SpeedBoost");
            jumpBoost = tag.getInt("JumpBoost");
            fallsaveHover = tag.getBoolean("FallsaveHover");
            fallsaveFlight = tag.getBoolean("FallsaveFlight");
            fallsavePowerToAir = tag.getInt("FallsavePowerToAir");
        }
    }

    private static final String JARVIS_DATA_KEY = "BrassManJarvisConfig";

    public static PlayerFlightData get(Player player) {
        if (player == null) {
            return new PlayerFlightData();
        }

        // Client-side: return cached config
        if (player.level().isClientSide) {
            return CLIENT_CONFIG;
        }

        // Server-side: load from persistent data
        PlayerFlightData data = new PlayerFlightData();
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag persistentData = serverPlayer.getPersistentData();
            if (persistentData.contains(JARVIS_DATA_KEY)) {
                data.load(persistentData.getCompound(JARVIS_DATA_KEY));
            }
        }
        return data;
    }

    public static void save(Player player, PlayerFlightData data) {
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag persistentData = serverPlayer.getPersistentData();
            persistentData.put(JARVIS_DATA_KEY, data.save());

            // Sync to client
            syncToClient(serverPlayer, data);
        }
    }

    private static void syncToClient(ServerPlayer player, PlayerFlightData data) {
        // Send packet to client - you need to import your packet sender here
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player,
                new net.blackredcoded.brassmanmod.network.SyncFlightConfigPacket(data.save())
        );
    }

    public static void setFlightSpeed(Player player, int speed) {
        PlayerFlightData data = get(player);
        data.flightSpeed = Math.max(1, Math.min(100, speed));
        save(player, data);
    }

    public static void setFlightEnabled(Player player, boolean enabled) {
        PlayerFlightData data = get(player);
        data.flightEnabled = enabled;
        save(player, data);
    }

    public static void setHoverEnabled(Player player, boolean enabled) {
        PlayerFlightData data = get(player);
        data.hoverEnabled = enabled;
        save(player, data);
    }

    public static void setFallsaveEnabled(Player player, boolean enabled) {
        PlayerFlightData data = get(player);
        data.fallsaveEnabled = enabled;
        save(player, data);
    }

    public static void setNightvisionEnabled(Player player, boolean enabled) {
        PlayerFlightData data = get(player);
        data.nightvisionEnabled = enabled;
        save(player, data);
    }

    public static void setSpeedBoost(Player player, int boost) {
        PlayerFlightData data = get(player);
        data.speedBoost = Math.max(0, Math.min(100, boost));
        save(player, data);
    }

    public static void setJumpBoost(Player player, int boost) {
        PlayerFlightData data = get(player);
        data.jumpBoost = Math.max(0, Math.min(100, boost));
        save(player, data);
    }

    public static int getFlightSpeed(Player player) {
        return get(player).flightSpeed;
    }

    public static boolean isFlightEnabled(Player player) {
        return player != null && get(player).flightEnabled;
    }

    public static boolean isHoverEnabled(Player player) {
        return player != null && get(player).hoverEnabled;
    }

    public static boolean isFallsaveEnabled(Player player) {
        return player != null && get(player).fallsaveEnabled;
    }

    public static boolean isNightvisionEnabled(Player player) {
        return player != null && get(player).nightvisionEnabled;
    }

    public static int getSpeedBoost(Player player) {
        return get(player).speedBoost;
    }

    public static int getJumpBoost(Player player) {
        return get(player).jumpBoost;
    }
}
