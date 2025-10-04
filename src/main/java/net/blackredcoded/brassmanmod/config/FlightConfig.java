package net.blackredcoded.brassmanmod.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightConfig {
    private static final Map<UUID, PlayerFlightData> CONFIG_MAP = new HashMap<>();

    public static class PlayerFlightData {
        public int flightSpeed = 20;
        public boolean flightEnabled = false;
        public boolean hoverEnabled = false;
        public boolean fallsaveEnabled = false;
        public boolean nightvisionEnabled = false;
        public int speedBoost = 0;
        public int jumpBoost = 0;

        // NEW: Fallsave options
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

    public static PlayerFlightData get(Player player) {
        if (player == null) {
            return new PlayerFlightData();
        }
        return CONFIG_MAP.computeIfAbsent(player.getUUID(), k -> new PlayerFlightData());
    }

    public static void save(Player player, PlayerFlightData data) {
        if (player != null) {
            CONFIG_MAP.put(player.getUUID(), data);
        }
    }

    public static void setFlightSpeed(Player player, int speed) {
        if (player != null) {
            get(player).flightSpeed = Math.max(1, Math.min(100, speed));
        }
    }

    public static void setFlightEnabled(Player player, boolean enabled) {
        if (player != null) {
            get(player).flightEnabled = enabled;
        }
    }

    public static void setHoverEnabled(Player player, boolean enabled) {
        if (player != null) {
            get(player).hoverEnabled = enabled;
        }
    }

    public static void setFallsaveEnabled(Player player, boolean enabled) {
        if (player != null) {
            get(player).fallsaveEnabled = enabled;
        }
    }

    public static void setNightvisionEnabled(Player player, boolean enabled) {
        if (player != null) {
            get(player).nightvisionEnabled = enabled;
        }
    }

    public static void setSpeedBoost(Player player, int boost) {
        if (player != null) {
            get(player).speedBoost = Math.max(0, Math.min(100, boost));
        }
    }

    public static void setJumpBoost(Player player, int boost) {
        if (player != null) {
            get(player).jumpBoost = Math.max(0, Math.min(100, boost));
        }
    }

    public static int getFlightSpeed(Player player) {
        return player != null ? get(player).flightSpeed : 20;
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
        return player != null ? get(player).speedBoost : 0;
    }

    public static int getJumpBoost(Player player) {
        return player != null ? get(player).jumpBoost : 0;
    }
}
