package net.blackredcoded.brassmanmod.blockentity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandBaseBlock;
import net.blackredcoded.brassmanmod.menu.AirCompressorMenu;
import net.blackredcoded.brassmanmod.items.BrassBootsItem;
import net.blackredcoded.brassmanmod.items.BrassChestplateItem;
import net.blackredcoded.brassmanmod.items.BrassHelmetItem;
import net.blackredcoded.brassmanmod.items.BrassLeggingsItem;
import net.blackredcoded.brassmanmod.registry.MaterialConverter;
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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AirCompressorBlockEntity extends KineticBlockEntity implements Container, MenuProvider {
    private static final float BASE_STRESS_IMPACT = 6.0f;
    private boolean wasWorking = false;
    private int tickCounter = 0;

    // Material storage: [brass, electronics, glass]
    private int[] materials = new int[3];

    // Single input slot for material-to-resource conversion
    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    public AirCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

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
            if (armorStand != null) chargeArmorStand(armorStand);
        }
    }

    /** Converts input stack into material counts when button 0 is clicked */
    public void convertInputToMaterials() {
        ItemStack input = inventory.get(0);
        if (input.isEmpty()) return;
        int[] conv = MaterialConverter.getMaterials(input.getItem());
        if (conv[0] == 0 && conv[1] == 0 && conv[2] == 0) return;
        int count = input.getCount();
        for (int i = 0; i < 3; i++) materials[i] += conv[i] * count;
        input.shrink(count);
        setChanged();
    }

    /** Repair brass helmet (button 1) */
    public boolean repairHelmet() {
        var stand = getArmorStandAbove();
        if (stand == null) return false;
        ItemStack helmet = stand.getArmor(0);
        if (helmet.isEmpty() || !helmet.isDamaged() || !(helmet.getItem() instanceof BrassHelmetItem))
            return false;
        int pct = (helmet.getDamageValue() * 100) / helmet.getMaxDamage();
        int b = round5(pct / 2), e = round5(pct / 3), g = round5(pct / 5);
        if (!consumeMaterials(b, e, g)) return false;
        helmet.setDamageValue(0);
        stand.setChanged();
        return true;
    }

    /** Repair brass chestplate (button 2) */
    public boolean repairChestplate() {
        var stand = getArmorStandAbove();
        if (stand == null) return false;
        ItemStack cp = stand.getArmor(1);
        if (cp.isEmpty() || !cp.isDamaged() || !(cp.getItem() instanceof BrassChestplateItem))
            return false;
        int pct = (cp.getDamageValue() * 100) / cp.getMaxDamage();
        int b = round5((pct * 2) / 3), e = round5(pct / 2);
        if (!consumeMaterials(b, e, 0)) return false;
        cp.setDamageValue(0);
        stand.setChanged();
        return true;
    }

    /** Repair brass leggings (button 3) */
    public boolean repairLeggings() {
        var stand = getArmorStandAbove();
        if (stand == null) return false;
        ItemStack lg = stand.getArmor(2);
        if (lg.isEmpty() || !lg.isDamaged() || !(lg.getItem() instanceof BrassLeggingsItem))
            return false;
        int pct = (lg.getDamageValue() * 100) / lg.getMaxDamage();
        int b = round5(pct / 2), e = round5(pct / 3);
        if (!consumeMaterials(b, e, 0)) return false;
        lg.setDamageValue(0);
        stand.setChanged();
        return true;
    }

    /** Repair brass boots (button 4) */
    public boolean repairBoots() {
        var stand = getArmorStandAbove();
        if (stand == null) return false;
        ItemStack bt = stand.getArmor(3);
        if (bt.isEmpty() || !bt.isDamaged() || !(bt.getItem() instanceof BrassBootsItem))
            return false;
        int pct = (bt.getDamageValue() * 100) / bt.getMaxDamage();
        int b = round5(pct / 3), e = round5(pct / 4);
        if (!consumeMaterials(b, e, 0)) return false;
        bt.setDamageValue(0);
        stand.setChanged();
        return true;
    }

    private int round5(int v) {
        return v == 0 ? 0 : ((v + 4) / 5) * 5;
    }

    public int getMaterial(int type) { return materials[type]; }
    public void setMaterial(int type, int amt) { materials[type] = Math.max(0, amt); setChanged(); }

    public boolean consumeMaterials(int b, int e, int g) {
        if (materials[0] >= b && materials[1] >= e && materials[2] >= g) {
            materials[0] -= b; materials[1] -= e; materials[2] -= g;
            setChanged();
            return true;
        }
        return false;
    }

    private boolean shouldConsumeStress() {
        var stand = getArmorStandAbove();
        return stand != null && stand.needsChargingOrRepair();
    }

    private BrassArmorStandBlockEntity getArmorStandAbove() {
        BlockPos above = worldPosition.above();
        if (level.getBlockState(above).getBlock() instanceof BrassArmorStandBaseBlock &&
                level.getBlockEntity(above) instanceof BrassArmorStandBlockEntity stand)
            return stand;
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
        ContainerHelper.saveAllItems(tag, inventory, regs);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider regs, boolean clientPacket) {
        super.read(tag, regs, clientPacket);
        wasWorking = tag.getBoolean("WasWorking");
        materials = tag.getIntArray("Materials");
        if (materials.length != 3) materials = new int[3];
        ContainerHelper.loadAllItems(tag, inventory, regs);
    }

    // Container methods
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return inventory.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeItem(int slot, int amt) { return ContainerHelper.removeItem(inventory, slot, amt); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(inventory, slot); }
    @Override public void setItem(int slot, ItemStack stack) { inventory.set(slot, stack); setChanged(); }
    @Override public boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent() { inventory.clear(); }

    // MenuProvider
    @Override public Component getDisplayName() {
        return Component.translatable("container.brassmanmod.air_compressor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new AirCompressorMenu(id, inv, this);
    }
}
