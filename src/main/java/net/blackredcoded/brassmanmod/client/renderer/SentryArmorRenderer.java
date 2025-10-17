package net.blackredcoded.brassmanmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blackredcoded.brassmanmod.entity.SentryArmorEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class SentryArmorRenderer extends LivingEntityRenderer<SentryArmorEntity, HumanoidModel<SentryArmorEntity>> {

    private static final ResourceLocation INVISIBLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("brassmanmod", "textures/entity/sentry_invisible.png");

    public SentryArmorRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.0f); // 0 shadow

        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(SentryArmorEntity entity) {
        return INVISIBLE_TEXTURE;
    }

    @Override
    public void render(SentryArmorEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Don't render the body model at all - only render armor layers
        poseStack.pushPose();

        // Setup model animations
        this.model.attackTime = this.getAttackAnim(entity, partialTick);
        this.model.riding = entity.isPassenger();
        this.model.young = entity.isBaby();

        // Only render armor layers, skip the body
        for (var layer : this.layers) {
            layer.render(poseStack, buffer, packedLight, entity,
                    entity.yBodyRot, entity.getYRot(), partialTick,
                    entity.tickCount + partialTick, entity.getYHeadRot() - entity.getYRot(),
                    entity.getXRot());
        }

        poseStack.popPose();
    }
}
