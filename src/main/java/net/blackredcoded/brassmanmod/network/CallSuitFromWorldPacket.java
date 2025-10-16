package net.blackredcoded.brassmanmod.network;

import io.netty.buffer.ByteBuf;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.entity.FlyingSuitEntity;
import net.blackredcoded.brassmanmod.items.*;
import net.blackredcoded.brassmanmod.util.ArmorUpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record CallSuitFromWorldPacket() implements CustomPacketPayload {

    public static final Type<CallSuitFromWorldPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("brassmanmod", "call_suit_from_world"));

    public static final StreamCodec<ByteBuf, CallSuitFromWorldPacket> STREAM_CODEC =
            StreamCodec.unit(new CallSuitFromWorldPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CallSuitFromWorldPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // FIXED: Check if wearing brass chestplate with Stage 2 (Field Assembly)
            ItemStack currentChest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(currentChest.getItem() instanceof BrassManChestplateItem)) {
                player.displayClientMessage(
                        Component.literal("You need to be wearing a Brass Man Chestplate!")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            int remoteLevel = ArmorUpgradeHelper.getRemoteAssemblyLevel(currentChest);
            if (remoteLevel < 2) {
                player.displayClientMessage(
                        Component.literal("⚠ Field Assembly Not Installed!")
                                .withStyle(ChatFormatting.RED),
                        false
                );
                player.displayClientMessage(
                        Component.literal("You need ⭐⭐⭐ Stage 2 (MK 42) to call suits from anywhere!")
                                .withStyle(ChatFormatting.GRAY),
                        false
                );
                return;
            }

            // Scan for suits in the world (32 block radius)
            ServerLevel level = (ServerLevel) player.level();
            BlockPos playerPos = player.blockPosition();
            List<SuitSource> availableSuits = new ArrayList<>();

            // 1. Check armor stands
            for (int x = -32; x <= 32; x++) {
                for (int y = -32; y <= 32; y++) {
                    for (int z = -32; z <= 32; z++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        BlockEntity be = level.getBlockEntity(pos);

                        if (be instanceof BrassArmorStandBlockEntity armorStand) {
                            ItemStack helmet = armorStand.getArmor(0);
                            ItemStack chestplate = armorStand.getArmor(1);
                            ItemStack leggings = armorStand.getArmor(2);
                            ItemStack boots = armorStand.getArmor(3);

                            if (isBrassSuit(helmet, chestplate, leggings, boots) &&
                                    ArmorUpgradeHelper.getRemoteAssemblyLevel(chestplate) >= 1) {
                                availableSuits.add(new SuitSource(pos, armorStand, helmet, chestplate, leggings, boots, "Armor Stand"));
                            }
                        }

                        // 2. Check chests/containers
                        if (be instanceof ChestBlockEntity chest) {
                            SuitPieces pieces = findSuitInContainer(chest);
                            if (pieces != null && ArmorUpgradeHelper.getRemoteAssemblyLevel(pieces.chestplate) >= 2) {
                                availableSuits.add(new SuitSource(pos, chest, pieces.helmet, pieces.chestplate, pieces.leggings, pieces.boots, "Chest"));
                            }
                        }
                    }
                }
            }

            // 3. Check dropped items on ground
            AABB searchBox = new AABB(playerPos).inflate(32);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchBox);
            SuitPieces groundSuit = findSuitInItems(items);
            if (groundSuit != null && ArmorUpgradeHelper.getRemoteAssemblyLevel(groundSuit.chestplate) >= 2) {
                availableSuits.add(new SuitSource(playerPos, items, groundSuit.helmet, groundSuit.chestplate, groundSuit.leggings, groundSuit.boots, "Ground"));
            }

            if (availableSuits.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("No callable suits found nearby! (32 block radius)")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            // Find closest suit
            SuitSource closest = availableSuits.stream()
                    .min((a, b) -> Double.compare(
                            a.pos.distSqr(playerPos),
                            b.pos.distSqr(playerPos)
                    ))
                    .orElse(null);

            if (closest == null) return;

            // Spawn flying suit
            FlyingSuitEntity flyingSuit = new FlyingSuitEntity(
                    level, closest.pos, player,
                    closest.helmet, closest.chestplate, closest.leggings, closest.boots
            );
            level.addFreshEntity(flyingSuit);

            // Remove armor from source
            if (closest.source instanceof BrassArmorStandBlockEntity armorStand) {
                armorStand.setArmor(0, ItemStack.EMPTY);
                armorStand.setArmor(1, ItemStack.EMPTY);
                armorStand.setArmor(2, ItemStack.EMPTY);
                armorStand.setArmor(3, ItemStack.EMPTY);
            } else if (closest.source instanceof Container container) {
                removeArmorFromContainer(container, closest.helmet, closest.chestplate, closest.leggings, closest.boots);
            } else if (closest.source instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<ItemEntity> itemList = (List<ItemEntity>) list;
                removeArmorFromGround(itemList, closest.helmet, closest.chestplate, closest.leggings, closest.boots);
            }

            player.displayClientMessage(
                    Component.literal("⭐⭐⭐ Field Assembly: Suit incoming from " + closest.sourceType + "!")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
        });
    }

    private static boolean isBrassSuit(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        if (!(chestplate.getItem() instanceof BrassManChestplateItem)) return false;
        if (!helmet.isEmpty() && !(helmet.getItem() instanceof BrassManHelmetItem)) return false;
        if (!leggings.isEmpty() && !(leggings.getItem() instanceof BrassManLeggingsItem)) return false;
        if (!boots.isEmpty() && !(boots.getItem() instanceof BrassManBootsItem)) return false;
        return true;
    }

    private static SuitPieces findSuitInContainer(Container container) {
        ItemStack helmet = ItemStack.EMPTY, chestplate = ItemStack.EMPTY, leggings = ItemStack.EMPTY, boots = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof BrassManHelmetItem && helmet.isEmpty()) helmet = stack;
            else if (stack.getItem() instanceof BrassManChestplateItem && chestplate.isEmpty()) chestplate = stack;
            else if (stack.getItem() instanceof BrassManLeggingsItem && leggings.isEmpty()) leggings = stack;
            else if (stack.getItem() instanceof BrassManBootsItem && boots.isEmpty()) boots = stack;
        }

        if (chestplate.isEmpty()) return null;
        return new SuitPieces(helmet, chestplate, leggings, boots);
    }

    private static SuitPieces findSuitInItems(List<ItemEntity> items) {
        ItemStack helmet = ItemStack.EMPTY, chestplate = ItemStack.EMPTY, leggings = ItemStack.EMPTY, boots = ItemStack.EMPTY;

        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            if (stack.getItem() instanceof BrassManHelmetItem && helmet.isEmpty()) helmet = stack;
            else if (stack.getItem() instanceof BrassManChestplateItem && chestplate.isEmpty()) chestplate = stack;
            else if (stack.getItem() instanceof BrassManLeggingsItem && leggings.isEmpty()) leggings = stack;
            else if (stack.getItem() instanceof BrassManBootsItem && boots.isEmpty()) boots = stack;
        }

        if (chestplate.isEmpty()) return null;
        return new SuitPieces(helmet, chestplate, leggings, boots);
    }

    private static void removeArmorFromContainer(Container container, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, helmet) ||
                    ItemStack.isSameItemSameComponents(stack, chestplate) ||
                    ItemStack.isSameItemSameComponents(stack, leggings) ||
                    ItemStack.isSameItemSameComponents(stack, boots)) {
                container.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private static void removeArmorFromGround(List<ItemEntity> items, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            if (ItemStack.isSameItemSameComponents(stack, helmet) ||
                    ItemStack.isSameItemSameComponents(stack, chestplate) ||
                    ItemStack.isSameItemSameComponents(stack, leggings) ||
                    ItemStack.isSameItemSameComponents(stack, boots)) {
                entity.discard();
            }
        }
    }

    private record SuitSource(BlockPos pos, Object source, ItemStack helmet, ItemStack chestplate,
                              ItemStack leggings, ItemStack boots, String sourceType) {}

    private record SuitPieces(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {}
}
