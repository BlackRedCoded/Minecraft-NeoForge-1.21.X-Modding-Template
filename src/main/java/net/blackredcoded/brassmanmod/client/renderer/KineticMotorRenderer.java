package net.blackredcoded.brassmanmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blackredcoded.brassmanmod.blockentity.KineticMotorBlockEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.client.renderer.LevelRenderer;

public class KineticMotorRenderer implements BlockEntityRenderer<KineticMotorBlockEntity> {

    public KineticMotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(KineticMotorBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING);
        float angle = AnimationTickHolder.getRenderTime(be.getLevel()) * be.getSpeed() * 3f / 10f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Apply the same rotation as the blockstate does
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(270));
        }

        // Now apply shaft rotation on Z axis (the shaft's axis in the model)
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));

        poseStack.translate(-0.5, -0.5, -0.5);

        // Render shaft model
        ModelResourceLocation modelLocation = ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath("brassmanmod", "block/kinetic_motor_shaft")
        );
        BakedModel shaftModel = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        int blockLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos());

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.cutout()),
                null,
                shaftModel,
                1f, 1f, 1f,
                blockLight,
                overlay
        );

        poseStack.popPose();
    }
}
