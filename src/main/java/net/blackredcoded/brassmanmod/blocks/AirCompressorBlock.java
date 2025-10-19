package net.blackredcoded.brassmanmod.blocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.blackredcoded.brassmanmod.blockentity.AirCompressorBlockEntity;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;
import net.blackredcoded.brassmanmod.util.CompressorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AirCompressorBlock extends DirectionalKineticBlock implements IBE<AirCompressorBlockEntity> {
    public static final MapCodec<AirCompressorBlock> CODEC = simpleCodec(AirCompressorBlock::new);
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 16, 16);

    public AirCompressorBlock(Properties properties) {
        super(properties.of()
                .strength(3, 6)
                .requiresCorrectToolForDrops()
                .sound(SoundType.WOOD));
    }

    @Override
    public @NotNull MapCodec<? extends AirCompressorBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getNearestLookingDirection().getOpposite();
        if (direction == Direction.UP || direction == Direction.DOWN) {
            direction = context.getHorizontalDirection().getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player && level instanceof ServerLevel serverLevel) {
            CompressorRegistry.registerCompressor(serverLevel, player.getUUID(), pos);
            if (level.getBlockEntity(pos) instanceof AirCompressorBlockEntity compressor) {
                compressor.setOwner(player.getUUID());
                compressor.setChanged();
                level.getChunkAt(pos).setUnsaved(true);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (level.getBlockEntity(pos) instanceof AirCompressorBlockEntity compressor) {
                if (compressor.getOwner() != null) {
                    CompressorRegistry.unregisterCompressor(serverLevel, compressor.getOwner(), pos);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction blockFacing = state.getValue(FACING);
        if (face == Direction.UP) {
            return false;
        }
        return face != blockFacing;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof AirCompressorBlockEntity blockEntity) {
                serverPlayer.openMenu(blockEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.SLOW;
    }

    @Override
    public Class<AirCompressorBlockEntity> getBlockEntityClass() {
        return AirCompressorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AirCompressorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.AIR_COMPRESSOR.get();
    }
}
