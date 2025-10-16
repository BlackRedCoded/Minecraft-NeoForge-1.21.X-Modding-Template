package net.blackredcoded.brassmanmod.event;

import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandBaseBlock;
import net.blackredcoded.brassmanmod.entity.SentryArmorEntity;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.List;
import java.util.UUID;

public class ArmorReturnHandler {

    @SubscribeEvent
    public static void onItemDropped(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (!isUpgradedBrassArmorPiece(stack)) return;

        // Don't check upgrade level - register ALL named armor
        String setName = BrassArmorStandBlockEntity.getSetName(stack);
        UUID owner = BrassArmorStandBlockEntity.getSetOwner(stack);

        System.out.println("DEBUG: Armor dropped - Set: " + setName + ", Owner: " + owner);

        if (setName == null || setName.isEmpty() || owner == null) return;

        System.out.println("DEBUG: Armor registered for auto-return!");

        CompoundTag persistentData = itemEntity.getPersistentData();
        persistentData.putBoolean("BrassManReturning", true);
        persistentData.putString("BrassManSetName", setName);
        persistentData.putUUID("BrassManOwner", owner);
        persistentData.putInt("BrassManReturnDelay", 60);
    }

    public static void tickArmorReturns(ServerLevel level) {
        for (Entity itemEntity : level.getAllEntities()) {
            if (!(itemEntity instanceof ItemEntity item)) continue;

            CompoundTag data = item.getPersistentData();
            if (!data.getBoolean("BrassManReturning")) continue;

            int delay = data.getInt("BrassManReturnDelay");

            if (delay > 0) {
                data.putInt("BrassManReturnDelay", delay - 1);
                continue;
            }

            System.out.println("DEBUG: Processing armor return");

            String setName = data.getString("BrassManSetName");
            UUID owner = data.getUUID("BrassManOwner");

            BlockPos targetStand = findMatchingArmorStand(level, item.blockPosition(), setName);

            if (targetStand != null) {
                System.out.println("DEBUG: Found matching armor stand");
                returnToArmorStand(level, item, targetStand);
            } else {
                System.out.println("DEBUG: No armor stand found, becoming sentry");
                becomeSentry(level, item, setName);
            }
        }
    }

    private static BlockPos findMatchingArmorStand(ServerLevel level, BlockPos searchPos, String setName) {
        for (BlockPos scanPos : BlockPos.betweenClosed(
                searchPos.offset(-64, -32, -64),
                searchPos.offset(64, 32, 64))) {

            if (level.getBlockEntity(scanPos) instanceof AirCompressorBlockEntity compressor) {
                String compressorName = compressor.getCustomName().getString();

                if (setName.equals(compressorName)) {
                    BlockPos above = scanPos.above();
                    if (level.getBlockState(above).getBlock() instanceof BrassArmorStandBaseBlock) {
                        return above;
                    }
                }
            }
        }
        return null;
    }

    private static void returnToArmorStand(ServerLevel level, ItemEntity itemEntity, BlockPos standPos) {
        BrassArmorStandBlockEntity stand = (BrassArmorStandBlockEntity) level.getBlockEntity(standPos);
        if (stand == null) return;

        ItemStack stack = itemEntity.getItem();
        int slot = getArmorSlot(stack);

        if (slot >= 0 && stand.getArmor(slot).isEmpty()) {
            stand.setArmor(slot, stack.copy());
            itemEntity.discard();
        } else {
            CompoundTag data = itemEntity.getPersistentData();
            becomeSentry(level, itemEntity, data.getString("BrassManSetName"));
        }
    }

    private static void becomeSentry(ServerLevel level, ItemEntity itemEntity, String setName) {
        BlockPos pos = itemEntity.blockPosition();
        ItemStack helmet = ItemStack.EMPTY;
        ItemStack chestplate = ItemStack.EMPTY;
        ItemStack leggings = ItemStack.EMPTY;
        ItemStack boots = ItemStack.EMPTY;

        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class,
                itemEntity.getBoundingBox().inflate(5.0));

        for (ItemEntity nearby : nearbyItems) {
            CompoundTag nearbyData = nearby.getPersistentData();
            if (!nearbyData.getBoolean("BrassManReturning")) continue;
            if (!nearbyData.getString("BrassManSetName").equals(setName)) continue;

            ItemStack nearbyStack = nearby.getItem();

            if (nearbyStack.getItem() instanceof BrassManHelmetItem) {
                helmet = nearbyStack.copy();
                nearby.discard();
            } else if (nearbyStack.getItem() instanceof BrassManChestplateItem) {
                chestplate = nearbyStack.copy();
                nearby.discard();
            } else if (nearbyStack.getItem() instanceof BrassManLeggingsItem) {
                leggings = nearbyStack.copy();
                nearby.discard();
            } else if (nearbyStack.getItem() instanceof BrassManBootsItem) {
                boots = nearbyStack.copy();
                nearby.discard();
            }
        }

        ItemStack currentStack = itemEntity.getItem();
        if (currentStack.getItem() instanceof BrassManHelmetItem && helmet.isEmpty()) {
            helmet = currentStack.copy();
        } else if (currentStack.getItem() instanceof BrassManChestplateItem && chestplate.isEmpty()) {
            chestplate = currentStack.copy();
        } else if (currentStack.getItem() instanceof BrassManLeggingsItem && leggings.isEmpty()) {
            leggings = currentStack.copy();
        } else if (currentStack.getItem() instanceof BrassManBootsItem && boots.isEmpty()) {
            boots = currentStack.copy();
        }
        itemEntity.discard();

        SentryArmorEntity sentry = new SentryArmorEntity(level, pos, setName, helmet, chestplate, leggings, boots);
        level.addFreshEntity(sentry);
    }

    private static int getArmorSlot(ItemStack stack) {
        if (stack.getItem() instanceof BrassManHelmetItem) return 0;
        if (stack.getItem() instanceof BrassManChestplateItem) return 1;
        if (stack.getItem() instanceof BrassManLeggingsItem) return 2;
        if (stack.getItem() instanceof BrassManBootsItem) return 3;
        return -1;
    }

    private static boolean isUpgradedBrassArmorPiece(ItemStack stack) {
        return stack.getItem() instanceof BrassManHelmetItem ||
                stack.getItem() instanceof BrassManChestplateItem ||
                stack.getItem() instanceof BrassManLeggingsItem ||
                stack.getItem() instanceof BrassManBootsItem;
    }
}
