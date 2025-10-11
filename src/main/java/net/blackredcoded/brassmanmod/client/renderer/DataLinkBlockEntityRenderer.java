package net.blackredcoded.brassmanmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blackredcoded.brassmanmod.blockentity.DataLinkBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

public class DataLinkBlockEntityRenderer implements BlockEntityRenderer<DataLinkBlockEntity> {
    private final ItemRenderer itemRenderer;

    public DataLinkBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(DataLinkBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        ItemStack frequencyItem = blockEntity.getFrequencyItem();
        if (frequencyItem.isEmpty()) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);

        poseStack.pushPose();

        // Position OUTSIDE the block face
        switch (facing) {
            case NORTH -> {
                poseStack.translate(0.5, 0.5, 0.87);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, 0, 0));
            }
            case SOUTH -> {
                poseStack.translate(0.5, 0.5, 0.13);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, (float)Math.PI, 0));
            }
            case WEST -> {
                poseStack.translate(0.87, 0.5, 0.5);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, (float)Math.PI / 2, 0));
            }
            case EAST -> {
                poseStack.translate(0.13, 0.5, 0.5);
                poseStack.mulPose(new Quaternionf().rotationXYZ(0, -(float)Math.PI / 2, 0));
            }
            case UP -> {
                poseStack.translate(0.5, 0.13, 0.5);
                poseStack.mulPose(new Quaternionf().rotationXYZ((float)Math.PI / 2, 0, 0));
            }
            case DOWN -> {
                poseStack.translate(0.5, 0.87, 0.5);
                poseStack.mulPose(new Quaternionf().rotationXYZ(-(float)Math.PI / 2, 0, 0));
            }
        }

        // Scale to reasonable size for 3D model
        poseStack.scale(0.35f, 0.35f, 0.35f);

        // Render as 3D item model instead of flat GUI texture
        this.itemRenderer.renderStatic(frequencyItem, ItemDisplayContext.FIXED, packedLight,
                packedOverlay, poseStack, buffer, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}
