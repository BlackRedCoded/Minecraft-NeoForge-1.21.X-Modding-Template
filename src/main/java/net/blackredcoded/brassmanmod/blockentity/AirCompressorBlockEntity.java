package net.blackredcoded.brassmanmod.blockentity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandBaseBlock;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.menu.AirCompressorMenu;
import net.blackredcoded.brassmanmod.registry.MaterialConverter;
import net.blackredcoded.brassmanmod.util.BatteryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import java.util.UUID;

public class AirCompressorBlockEntity extends KineticBlockEntity implements Container, MenuProvider {

    private static final float BASE_STRESS_IMPACT = 6.0f;
    private boolean wasWorking = false;
    private int tickCounter = 0;
    private Component customName = Component.literal("Air Compressor");

    // Owner tracking
    private UUID ownerUUID;

    // Material storage: [brass, electronics, glass]
    private int[] materials = new int[3];

    // Inventory: [input slot, charging slot]
    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private static final int INPUT_SLOT = 0;
    private static final int CHARGING_SLOT = 1;

    // Redstone control
    private int redstoneSignal = 0;
    private int selectedArmorSlot = 0;

    public AirCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    // CRITICAL: Force save when block entity loads
    @Override
    public void onLoad() {
        super.onLoad();

        if (!level.isClientSide() && ownerUUID != null) {
            setChanged();
            level.getChunkAt(worldPosition).setUnsaved(true);
        }
    }

    // Owner methods
    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.getChunkAt(worldPosition).setUnsaved(true);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getOwner() {
        return ownerUUID;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        tickCounter++;
        boolean shouldWork = shouldConsumeStress();
        if (shouldWork != wasWorking) {
            wasWorking = shouldWork;
            setChanged();
        }

        // Read redstone signal and handle automation
        redstoneSignal = level.getBestNeighborSignal(worldPosition);
        handleRedstoneControl();

        // Charge every 10 ticks if running
        if (shouldWork && tickCounter % 10 == 0 && Math.abs(getSpeed()) >= 1) {
            var armorStand = getArmorStandAbove();
            if (armorStand != null) {
                chargeArmorStand(armorStand);
            }
        }

        // Universal battery charging
        chargeBatteryItems();
    }

    // Handle redstone automation
    private void handleRedstoneControl() {
        if (redstoneSignal >= 10 && redstoneSignal <= 15) {
            convertInputToMaterials();
        } else if (redstoneSignal >= 5 && redstoneSignal <= 9) {
            repairArmorSlot(selectedArmorSlot);
        } else if (redstoneSignal >= 1 && redstoneSignal <= 4) {
            selectedArmorSlot = 3 - (redstoneSignal - 1);
        }
    }

    private void chargeBatteryItems() {
        ItemStack chargingItem = inventory.get(CHARGING_SLOT);
        if (chargingItem.isEmpty()) return;

        if (chargingItem.getItem() instanceof KineticBatteryItem && !BatteryHelper.isBatteryItem(chargingItem)) {
            BatteryHelper.initBattery(chargingItem, KineticBatteryItem.BASE_MAX_SU);
            setChanged();
            return;
        }

        if (BatteryHelper.isBatteryItem(chargingItem)) {
            if (!BatteryHelper.isBatteryFull(chargingItem)) {
                float rpm = Math.abs(getSpeed());
                float multiplier = BatteryHelper.getChargeRateMultiplier(chargingItem);
                int baseChargeAmount = 8;
                if (rpm < 1) return;

                int chargeAmount = Math.round(baseChargeAmount * multiplier);
                BatteryHelper.chargeBattery(chargingItem, chargeAmount);
                setChanged();
            }
        }
    }

    public void convertInputToMaterials() {
        ItemStack input = inventory.get(INPUT_SLOT);
        if (input.isEmpty()) return;

        int[] conv = MaterialConverter.getMaterials(input.getItem());
        if (conv[0] == 0 && conv[1] == 0 && conv[2] == 0) return;

        int count = input.getCount();
        for (int i = 0; i < 3; i++) {
            materials[i] += conv[i] * count;
        }

        input.shrink(count);
        setChanged();
    }

    public boolean repairArmorSlot(int slot) {
        var stand = getArmorStandAbove();
        if (stand == null) return false;

        ItemStack armor = stand.getArmor(slot);
        if (armor.isEmpty() || !armor.isDamaged()) return false;

        boolean validType = switch (slot) {
            case 0 -> armor.getItem() instanceof BrassManHelmetItem;
            case 1 -> armor.getItem() instanceof BrassManChestplateItem;
            case 2 -> armor.getItem() instanceof BrassManLeggingsItem;
            case 3 -> armor.getItem() instanceof BrassManBootsItem;
            default -> false;
        };

        if (!validType) return false;

        int damageTaken = armor.getDamageValue();
        int maxDurability = armor.getMaxDamage();
        double damagePercent = (double) damageTaken / maxDurability;
        int brassCost, electronicsCost, glassCost;

        switch (slot) {
            case 0:
                brassCost = (int) Math.ceil(damagePercent * 60 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 120 / 5) * 5;
                glassCost = (int) Math.ceil(damagePercent * 30 / 5) * 5;
                break;
            case 1:
                brassCost = (int) Math.ceil(damagePercent * 120 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 180 / 5) * 5;
                glassCost = (int) Math.ceil(damagePercent * 10 / 5) * 5;
                break;
            case 2:
                brassCost = (int) Math.ceil(damagePercent * 100 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 150 / 5) * 5;
                glassCost = 0;
                break;
            case 3:
                brassCost = (int) Math.ceil(damagePercent * 50 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 90 / 5) * 5;
                glassCost = 0;
                break;
            default:
                return false;
        }

        if (!consumeMaterials(brassCost, electronicsCost, glassCost)) return false;

        armor.setDamageValue(0);
        stand.setChanged();
        setChanged();
        return true;
    }

    public boolean repairHelmet() { return repairArmorSlot(0); }
    public boolean repairChestplate() { return repairArmorSlot(1); }
    public boolean repairLeggings() { return repairArmorSlot(2); }
    public boolean repairBoots() { return repairArmorSlot(3); }

    public int getMaterial(int type) { return materials[type]; }
    public void setMaterial(int type, int amt) { materials[type] = Math.max(0, amt); setChanged(); }

    public boolean consumeMaterials(int b, int e, int g) {
        if (materials[0] >= b && materials[1] >= e && materials[2] >= g) {
            materials[0] -= b;
            materials[1] -= e;
            materials[2] -= g;
            setChanged();
            return true;
        }
        return false;
    }

    private boolean shouldConsumeStress() {
        var stand = getArmorStandAbove();
        if (stand != null && stand.needsChargingOrRepair()) return true;

        ItemStack chargingItem = inventory.get(CHARGING_SLOT);
        if (!chargingItem.isEmpty() && BatteryHelper.isBatteryItem(chargingItem)) {
            if (!BatteryHelper.isBatteryFull(chargingItem)) return true;
        }
        return false;
    }

    private BrassArmorStandBlockEntity getArmorStandAbove() {
        BlockPos above = worldPosition.above();
        if (level.getBlockState(above).getBlock() instanceof BrassArmorStandBaseBlock &&
                level.getBlockEntity(above) instanceof BrassArmorStandBlockEntity stand) {
            return stand;
        }
        return null;
    }

    private void chargeArmorStand(BrassArmorStandBlockEntity stand) {
        float rpm = Math.abs(getSpeed());
        if (rpm < 1) return;

        float rate = Math.min(rpm / 32f, 8f);
        for (int i = 0; i < 4; i++) {
            ItemStack a = stand.getArmor(i);
            if (a.getItem() instanceof BrassManChestplateItem chest) {
                int air = chest.air(a), pw = chest.power(a);
                int maxA = chest.getMaxAir(a), maxP = chest.getMaxPower(a);
                int chargeA = Math.max(1, Math.round(rate * maxA / 100f));
                int chargeP = Math.max(1, Math.round(rate * maxP / 100f));
                if (air < maxA || pw < maxP) {
                    chest.setAirAndPower(a,
                            Math.min(air + chargeA, maxA),
                            Math.min(pw + chargeP, maxP)
                    );
                    stand.setChanged();
                }
            }
        }
    }

    @Override
    public float calculateStressApplied() {
        return shouldConsumeStress() ? BASE_STRESS_IMPACT : 0;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider regs, boolean clientPacket) {
        super.write(tag, regs, clientPacket);
        tag.putInt("Brass", materials[0]);
        tag.putInt("Electronics", materials[1]);
        tag.putInt("Glass", materials[2]);
        tag.putInt("RedstoneSignal", redstoneSignal);
        tag.putInt("SelectedArmorSlot", selectedArmorSlot);

        // Simple string save instead of Component JSON
        if (!customName.getString().equals("Air Compressor")) {
            tag.putString("CustomNameText", customName.getString());
        }

        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        ContainerHelper.saveAllItems(tag, inventory, regs);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider regs, boolean clientPacket) {
        super.read(tag, regs, clientPacket);
        materials[0] = tag.getInt("Brass");
        materials[1] = tag.getInt("Electronics");
        materials[2] = tag.getInt("Glass");
        redstoneSignal = tag.getInt("RedstoneSignal");
        selectedArmorSlot = tag.getInt("SelectedArmorSlot");

        // Simple string load instead of Component JSON
        if (tag.contains("CustomNameText")) {
            customName = Component.literal(tag.getString("CustomNameText"));
        }

        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        ContainerHelper.loadAllItems(tag, inventory, regs);
    }

    // Minecraft's native system - for DISK SAVES (called when chunks save/load)
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);

        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
            System.out.println("GET UPDATE TAG - UUID: " + ownerUUID);
        }

        return tag;
    }

    // Load from disk - receives data from getUpdateTag
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
            System.out.println("LOAD ADDITIONAL - UUID: " + ownerUUID);
        } else {
            System.out.println("LOAD ADDITIONAL - NO UUID");
        }
    }

    // Container methods
    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeItem(int slot, int amt) { return ContainerHelper.removeItem(inventory, slot, amt); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(inventory, slot); }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof KineticBatteryItem && !BatteryHelper.isBatteryItem(stack)) {
            BatteryHelper.initBattery(stack, KineticBatteryItem.BASE_MAX_SU);
            setChanged();
        }

        if (slot == INPUT_SLOT && !stack.isEmpty()) {
            if (stack.getItem() instanceof CompressorNetworkTabletItem || BatteryHelper.isBatteryItem(stack)) {
                if (inventory.get(CHARGING_SLOT).isEmpty()) {
                    inventory.set(CHARGING_SLOT, stack);
                    inventory.set(INPUT_SLOT, ItemStack.EMPTY);
                    setChanged();
                    return;
                }
            }
        }

        inventory.set(slot, stack);
        setChanged();
    }

    @Override public boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent() { inventory.clear(); }

    // MenuProvider
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.brassmanmod.air_compressor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new AirCompressorMenu(id, inv, this);
    }

    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(Component name) {
        this.customName = name;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            // NEW: Tag armor above when renamed
            tagArmorAbove();
        }
    }

    public int getSelectedArmorSlot() {
        return selectedArmorSlot;
    }

    // NEW: Tag armor above with set name when compressor is renamed
    public void tagArmorAbove() {
        if (level == null || level.isClientSide) return;

        BrassArmorStandBlockEntity stand = getArmorStandAbove();
        if (stand == null) return;

        // Apply set name and owner to all armor pieces
        String setName = customName.getString();
        stand.applySetNameToArmor(setName, ownerUUID);
    }

}
