package net.blackredcoded.brassmanmod.blockentity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.blackredcoded.brassmanmod.items.KineticBatteryItem;
import net.blackredcoded.brassmanmod.menu.KineticMotorMenu;
import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KineticMotorBlockEntity extends GeneratingKineticBlockEntity implements Container, MenuProvider {

    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private static final int BATTERY_SLOT = 0;

    // Motor configuration (can be set via GUI or NBT)
    private float targetRPM = 64.0f; // Default RPM when powered
    private float maxStressCapacity = 256.0f; // Max stress this motor can handle

    // Runtime tracking
    private int ticksSinceLastDrain = 0;
    private static final int DRAIN_INTERVAL = 20; // Drain every second (20 ticks)

    public KineticMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        ItemStack battery = inventory.get(BATTERY_SLOT);

        if (!BatteryHelper.isBatteryItem(battery) || BatteryHelper.getBatteryCharge(battery) <= 0) {
            if (getGeneratedSpeed() != 0) {
                updateGeneratedRotation();
            }
            return;
        }

        // CRITICAL FIX: Drain EVERY TICK based on stress being provided
        // This makes the battery runtime predictable
        float stressProvided = 0;
        if (hasNetwork()) {
            stressProvided = Math.abs(stress); // Actual stress being used
        }

        // Only drain if actually providing power
        if (stressProvided > 0.1f) {
            // Drain = stress provided per tick
            // With 512k battery and 256 SU load = 2000 ticks = 100 seconds runtime
            int drainAmount = Math.max(1, Math.round(stressProvided));
            BatteryHelper.drainBattery(battery, drainAmount);

            // Only mark changed every 20 ticks to reduce lag
            if (level.getGameTime() % 20 == 0) {
                setChanged();
            }
        }

        // Force visual speed update
        if (level != null && !level.isClientSide) {
            updateGeneratedRotation();
        }

        updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        ItemStack battery = inventory.get(BATTERY_SLOT);

        // Only generate speed if battery has charge
        if (BatteryHelper.isBatteryItem(battery) && BatteryHelper.getBatteryCharge(battery) > 0) {
            return targetRPM; // Positive = clockwise
        }

        return 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        ItemStack battery = inventory.get(BATTERY_SLOT);

        // Only provide stress capacity if battery has charge
        if (BatteryHelper.isBatteryItem(battery) && BatteryHelper.getBatteryCharge(battery) > 0) {
            return maxStressCapacity;
        }

        return 0;
    }

    // Helper to get network stress being used
    private float getNetworkStress() {
        if (hasNetwork()) {
            // 'capacity' is inherited from KineticBlockEntity and represents current stress load
            return Math.abs(capacity);
        }
        return 0;
    }

    // Getters/setters for GUI control
    public float getTargetRPM() {
        return targetRPM;
    }

    public void setTargetRPM(float rpm) {
        this.targetRPM = Math.max(0, Math.min(256, rpm)); // Clamp between 0-256
        updateGeneratedRotation();
        setChanged();
    }

    public float getMaxStressCapacity() {
        return maxStressCapacity;
    }

    public void setMaxStressCapacity(float capacity) {
        this.maxStressCapacity = Math.max(0, capacity);
        updateGeneratedRotation();
        setChanged();
    }

    // Battery info for GUI
    public int getBatteryCharge() {
        ItemStack battery = inventory.get(BATTERY_SLOT);
        return BatteryHelper.isBatteryItem(battery) ? BatteryHelper.getBatteryCharge(battery) : 0;
    }

    public int getBatteryMaxCharge() {
        ItemStack battery = inventory.get(BATTERY_SLOT);
        return BatteryHelper.isBatteryItem(battery) ? BatteryHelper.getMaxBatteryCharge(battery) : 0;
    }

    public float getCurrentStress() {
        return getNetworkStress();
    }

    // Container methods
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return inventory.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(inventory, slot, amount); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(inventory, slot); }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        setChanged();
        updateGeneratedRotation();
    }

    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { inventory.clear(); }

    // NBT
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        tag.putFloat("TargetRPM", targetRPM);
        tag.putFloat("MaxStressCapacity", maxStressCapacity);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        targetRPM = tag.getFloat("TargetRPM");
        maxStressCapacity = tag.getFloat("MaxStressCapacity");
    }

    // Menu
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.brassmanmod.kinetic_motor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new KineticMotorMenu(id, playerInventory, this);
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(this, worldPosition);
    }

    @Override
    public boolean isSource() {
        return true; // This tells Create this is a power source
    }

    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    @Override
    protected boolean isNoisy() {
        return true; // Return true if you want sound effects when running
    }
}
