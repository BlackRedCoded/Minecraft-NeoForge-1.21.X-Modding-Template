package net.blackredcoded.brassmanmod.blocks;

import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassArmorStandBaseBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(2, 0, 2, 14, 1, 14),
            Block.box(5, 1, 7, 7, 12, 9),
            Block.box(9, 1, 7, 11, 12, 9),
            Block.box(4, 12, 7, 12, 14, 9),
            Block.box(5, 14, 7, 7, 16, 9),
            Block.box(9, 14, 7, 11, 16, 9)
    );
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(2, 0, 2, 14, 1, 14),
            Block.box(9, 1, 7, 11, 12, 9),
            Block.box(5, 1, 7, 7, 12, 9),
            Block.box(4, 12, 7, 12, 14, 9),
            Block.box(9, 14, 7, 11, 16, 9),
            Block.box(5, 14, 7, 7, 16, 9)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(2, 0, 2, 14, 1, 14),
            Block.box(7, 1, 5, 9, 12, 7),
            Block.box(7, 1, 9, 9, 12, 11),
            Block.box(7, 12, 4, 9, 14, 12),
            Block.box(7, 14, 5, 9, 16, 7),
            Block.box(7, 14, 9, 9, 16, 11)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(2, 0, 2, 14, 1, 14),
            Block.box(7, 1, 9, 9, 12, 11),
            Block.box(7, 1, 5, 9, 12, 7),
            Block.box(7, 12, 4, 9, 14, 12),
            Block.box(7, 14, 9, 9, 16, 11),
            Block.box(7, 14, 5, 9, 16, 7)
    );

    public BrassArmorStandBaseBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState s, @NotNull BlockGetter g, @NotNull BlockPos p, @NotNull CollisionContext c) {
        return switch (s.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos top = ctx.getClickedPos().above();
        Level world = ctx.getLevel();
        if (!world.getBlockState(top).isAir()) return null;
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState s, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BrassArmorStandBlockEntity stand) {
            return handleInteraction(player, stand, hit, pos);
        }
        return InteractionResult.PASS;
    }

    public InteractionResult handleInteraction(Player player, BrassArmorStandBlockEntity stand, BlockHitResult hit, BlockPos basePos) {
        ItemStack held = player.getMainHandItem();
        boolean shift = player.isShiftKeyDown();

        // SHIFT + EMPTY HAND = swap all armor
        if (shift && held.isEmpty()) {
            boolean hasAny = false;
            for (int i = 0; i < 4; i++) {
                if (!stand.getArmor(i).isEmpty()) { hasAny = true; break; }
            }
            return hasAny ? swapAllArmor(player, stand) : giveAllFromPlayer(player, stand);
        }

        // FIXED: Use the base block position for Y calculation, regardless of which block was clicked
        double clickY = hit.getLocation().y - basePos.getY();
        int slot = getSlot(clickY);

        // EMPTY HAND (no shift) = remove single piece to INVENTORY
        if (held.isEmpty()) {
            ItemStack piece = stand.getArmor(slot);
            if (!piece.isEmpty()) {
                stand.setArmor(slot, ItemStack.EMPTY);
                player.getInventory().add(piece.copy());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // HOLDING ARMOR = place on stand
        if (held.getItem() instanceof ArmorItem armorItem) {
            int st = stand.getSlotForArmorType(armorItem.getType());
            if (stand.getArmor(st).isEmpty()) {
                stand.setArmor(st, held.copyWithCount(1));
                held.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult swapAllArmor(Player p, BrassArmorStandBlockEntity s) {
        boolean moved = false;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) {
            ItemStack standArmor = s.getArmor(i);
            ItemStack playerArmor = p.getItemBySlot(slots[i]);
            if (!standArmor.isEmpty() || !playerArmor.isEmpty()) {
                s.setArmor(i, playerArmor.isEmpty() ? ItemStack.EMPTY : playerArmor.copy());
                p.setItemSlot(slots[i], standArmor.isEmpty() ? ItemStack.EMPTY : standArmor.copy());
                moved = true;
            }
        }
        return moved ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private InteractionResult giveAllFromPlayer(Player p, BrassArmorStandBlockEntity s) {
        boolean moved = false;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) {
            ItemStack piece = p.getItemBySlot(slots[i]);
            if (!piece.isEmpty() && s.getArmor(i).isEmpty()) {
                s.setArmor(i, piece.copyWithCount(1));
                p.setItemSlot(slots[i], ItemStack.EMPTY);
                moved = true;
            }
        }
        return moved ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * FIXED: Proper slot mapping based on absolute Y position from BASE block
     * Total height is 0.0 to 2.0 (base block + top block)
     */
    private int getSlot(double y) {
        if (y >= 1.6) return 0;   // helmet (top of stand)
        if (y >= 1.0) return 1;   // chestplate (upper torso)
        if (y >= 0.5) return 2;   // leggings (lower torso)
        return 3;                 // boots (bottom)
    }

    @Override
    public void onPlace(@NotNull BlockState bs, Level lvl, @NotNull BlockPos pos, @NotNull BlockState old, boolean moved) {
        if (!lvl.isClientSide) {
            BlockPos top = pos.above();
            if (lvl.getBlockState(top).isAir()) {
                lvl.setBlock(top,
                        ModBlocks.BRASS_ARMOR_STAND_TOP.get()
                                .defaultBlockState()
                                .setValue(FACING, bs.getValue(FACING)),
                        3
                );
            }
        }
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level lvl, @NotNull BlockPos pos, @NotNull BlockState bs, @NotNull Player pl) {
        if (!lvl.isClientSide && lvl.getBlockEntity(pos) instanceof BrassArmorStandBlockEntity stand) {
            for (int i = 0; i < 4; i++) {
                ItemStack a = stand.getArmor(i);
                if (!a.isEmpty()) Block.popResource(lvl, pos, a);
            }
            BlockPos top = pos.above();
            if (lvl.getBlockState(top).getBlock() == ModBlocks.BRASS_ARMOR_STAND_TOP.get()) {
                lvl.destroyBlock(top, false);
            }
        }
        return super.playerWillDestroy(lvl, pos, bs, pl);
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState bs, net.minecraft.world.level.storage.loot.LootParams.@NotNull Builder b) {
        return List.of(new ItemStack(ModBlocks.BRASS_ARMOR_STAND.get()));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState bs) {
        return new BrassArmorStandBlockEntity(pos, bs);
    }
}
