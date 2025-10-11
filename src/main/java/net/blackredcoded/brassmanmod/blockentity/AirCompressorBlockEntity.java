package net.blackredcoded.brassmanmod.blockentity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandBaseBlock;
import net.blackredcoded.brassmanmod.items.BrassBootsItem;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.blackredcoded.brassmanmod.items.BrassHelmetItem;
import net.blackredcoded.brassmanmod.items.BrassLeggingsItem;
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

    // NEW: Owner tracking
    private UUID ownerUUID;

    // Material storage: [brass, electronics, glass]
    private int[] materials = new int[3];

    // Inventory: [input slot, charging slot]
    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private static final int INPUT_SLOT = 0;
    private static final int CHARGING_SLOT = 1;

    public AirCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    // NEW: Owner methods
    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
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

        // Charge every 10 ticks if running
        if (shouldWork && tickCounter % 10 == 0 && Math.abs(getSpeed()) >= 1) {
            var armorStand = getArmorStandAbove();
            if (armorStand != null) {
                chargeArmorStand(armorStand);
            }
            // Universal battery charging
            chargeBatteryItems();
        }
    }

    private void chargeBatteryItems() {
        ItemStack chargingItem = inventory.get(CHARGING_SLOT);
        if (!chargingItem.isEmpty() && BatteryHelper.isBatteryItem(chargingItem)) {
            if (!BatteryHelper.isBatteryFull(chargingItem)) {
                float rpm = Math.abs(getSpeed());
                if (rpm < 1) return;
                float rate = Math.min(rpm / 32f, 8f);
                int maxBattery = BatteryHelper.getMaxBatteryCharge(chargingItem);
                int chargeAmount = Math.max(1, Math.round(rate * maxBattery / 100f));
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
            case 0 -> armor.getItem() instanceof BrassHelmetItem;
            case 1 -> armor.getItem() instanceof BrassChestplateItem;
            case 2 -> armor.getItem() instanceof BrassLeggingsItem;
            case 3 -> armor.getItem() instanceof BrassBootsItem;
            default -> false;
        };
        if (!validType) return false;

        int damageTaken = armor.getDamageValue();
        int maxDurability = armor.getMaxDamage();
        double damagePercent = (double) damageTaken / maxDurability;

        int brassCost, electronicsCost, glassCost;
        switch (slot) {
            case 0: // Helmet
                brassCost = (int) Math.ceil(damagePercent * 60 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 120 / 5) * 5;
                glassCost = (int) Math.ceil(damagePercent * 30 / 5) * 5;
                break;
            case 1: // Chestplate
                brassCost = (int) Math.ceil(damagePercent * 120 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 180 / 5) * 5;
                glassCost = (int) Math.ceil(damagePercent * 10 / 5) * 5;
                break;
            case 2: // Leggings
                brassCost = (int) Math.ceil(damagePercent * 100 / 5) * 5;
                electronicsCost = (int) Math.ceil(damagePercent * 150 / 5) * 5;
                glassCost = 0;
                break;
            case 3: // Boots
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
            if (a.getItem() instanceof BrassChestplateItem chest) {
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
        tag.putBoolean("WasWorking", wasWorking);
        tag.putIntArray("Materials", materials);
        tag.putString("CustomName", Component.Serializer.toJson(customName, regs));
        ContainerHelper.saveAllItems(tag, inventory, regs);

        // NEW: Save owner
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider regs, boolean clientPacket) {
        super.read(tag, regs, clientPacket);
        wasWorking = tag.getBoolean("WasWorking");
        materials = tag.getIntArray("Materials");
        if (materials.length != 3) materials = new int[3];
        if (tag.contains("CustomName")) {
            String nameJson = tag.getString("CustomName");
            Component loadedName = Component.Serializer.fromJson(nameJson, regs);
            customName = loadedName != null ? loadedName : Component.literal("Air Compressor");
        }
        ContainerHelper.loadAllItems(tag, inventory, regs);

        // NEW: Load owner
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
    }

    // Container methods
    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeItem(int slot, int amt) { return ContainerHelper.removeItem(inventory, slot, amt); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(inventory, slot); }
    @Override public void setItem(int slot, ItemStack stack) { inventory.set(slot, stack); setChanged(); }
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
        }
    }
}
