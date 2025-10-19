package net.blackredcoded.brassmanmod.util;

import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.items.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class ChestArmorScanner {

    public static class ArmorLocation {
        public BlockPos chestPos;
        public int slot;
        public ItemStack armor;

        public ArmorLocation(BlockPos pos, int slot, ItemStack armor) {
            this.chestPos = pos.immutable(); // CRITICAL: Make immutable copy!
            this.slot = slot;
            this.armor = armor;
        }
    }

    public static class SuitSet {
        public ArmorLocation helmet;
        public ArmorLocation chestplate;
        public ArmorLocation leggings;
        public ArmorLocation boots;

        public boolean isComplete() {
            return helmet != null && chestplate != null && leggings != null && boots != null;
        }

        public boolean hasAnyPiece() {
            return helmet != null || chestplate != null || leggings != null || boots != null;
        }
    }

    public static SuitSet findArmorInChests(ServerLevel level, BlockPos playerPos, String setName, UUID ownerUUID, int radius) {
        SuitSet result = new SuitSet();

        for (BlockPos scanPos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius/2, -radius),
                playerPos.offset(radius, radius/2, radius))) {

            BlockEntity be = level.getBlockEntity(scanPos);
            if (be instanceof Container container) {
                scanContainer(container, scanPos, setName, ownerUUID, result);
            }
        }

        return result;
    }

    private static void scanContainer(Container container, BlockPos pos, String setName, UUID ownerUUID, SuitSet result) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            String stackSetName = BrassArmorStandBlockEntity.getSetName(stack);
            UUID stackOwner = BrassArmorStandBlockEntity.getSetOwner(stack);

            if (setName.equals(stackSetName) && ownerUUID.equals(stackOwner)) {
                if (stack.getItem() instanceof BrassManHelmetItem && result.helmet == null) {
                    result.helmet = new ArmorLocation(pos, i, stack.copy());
                } else if (stack.getItem() instanceof BrassManChestplateItem && result.chestplate == null) {
                    result.chestplate = new ArmorLocation(pos, i, stack.copy());
                } else if (stack.getItem() instanceof BrassManLeggingsItem && result.leggings == null) {
                    result.leggings = new ArmorLocation(pos, i, stack.copy());
                } else if (stack.getItem() instanceof BrassManBootsItem && result.boots == null) {
                    result.boots = new ArmorLocation(pos, i, stack.copy());
                }
            }
        }
    }

    public static void removeArmorFromChests(ServerLevel level, SuitSet suitSet) {
        if (suitSet.helmet != null) {
            removeFromChest(level, suitSet.helmet);
        }
        if (suitSet.chestplate != null) {
            removeFromChest(level, suitSet.chestplate);
        }
        if (suitSet.leggings != null) {
            removeFromChest(level, suitSet.leggings);
        }
        if (suitSet.boots != null) {
            removeFromChest(level, suitSet.boots);
        }
    }

    private static void removeFromChest(ServerLevel level, ArmorLocation location) {
        BlockEntity be = level.getBlockEntity(location.chestPos);
        if (be instanceof Container container) {
            container.setItem(location.slot, ItemStack.EMPTY);
            container.setChanged(); // CRITICAL: Mark the container as modified!
            level.sendBlockUpdated(location.chestPos, level.getBlockState(location.chestPos), level.getBlockState(location.chestPos), 3);
        }
    }
}
