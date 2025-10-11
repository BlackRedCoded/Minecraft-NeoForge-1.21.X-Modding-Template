package net.blackredcoded.brassmanmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.blackredcoded.brassmanmod.blockentity.BrassArmorStandBlockEntity;
import net.blackredcoded.brassmanmod.blocks.BrassArmorStandBaseBlock;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class BrassArmorStandRenderer implements BlockEntityRenderer<BrassArmorStandBlockEntity> {

    private final HumanoidArmorModel<?> innerArmorModel;
    private final HumanoidArmorModel<?> outerArmorModel;

    public BrassArmorStandRenderer(BlockEntityRendererProvider.Context context) {
        this.innerArmorModel = new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        this.outerArmorModel = new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
    }

    @Override
    public void render(BrassArmorStandBlockEntity armorStand, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = armorStand.getBlockState().getValue(BrassArmorStandBaseBlock.FACING);
        poseStack.pushPose();

        poseStack.translate(0.5, 3, 0.5);

        // FIXED: Corrected EAST/WEST rotation
        float yRotation = switch (facing) {
            case NORTH -> 0.0F;      // Face NORTH
            case SOUTH -> 180.0F;    // Face SOUTH
            case WEST -> 90.0F;      // Face WEST
            case EAST -> 270.0F;     // Face EAST (was 90°, now 270°)
            default -> 0.0F;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        poseStack.scale(-1.95F, -1.95F, 1.95F);

        // Render armor pieces
        renderArmorPiece(armorStand, EquipmentSlot.FEET, poseStack, buffer, packedLight);
        renderArmorPiece(armorStand, EquipmentSlot.LEGS, poseStack, buffer, packedLight);
        renderArmorPiece(armorStand, EquipmentSlot.CHEST, poseStack, buffer, packedLight);
        renderArmorPiece(armorStand, EquipmentSlot.HEAD, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    private void renderArmorPiece(BrassArmorStandBlockEntity armorStand, EquipmentSlot equipmentSlot,
                                  PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ItemStack itemStack = armorStand.getArmor(getSlotIndex(equipmentSlot));
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ArmorItem armorItem)) {
            return;
        }

        Holder<ArmorMaterial> armorMaterial = armorItem.getMaterial();
        boolean isInnerModel = equipmentSlot == EquipmentSlot.LEGS;
        HumanoidArmorModel<?> model = isInnerModel ? innerArmorModel : outerArmorModel;
        ResourceLocation armorTexture = getArmorResource(armorMaterial, equipmentSlot, isInnerModel);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.armorCutoutNoCull(armorTexture));

        poseStack.pushPose();

        if (equipmentSlot == EquipmentSlot.HEAD) {
            poseStack.translate(0, 0.3, 0);
            poseStack.scale(0.65F, 0.65F, 0.65F);
        }

        setupArmorModel(model, equipmentSlot);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    private void setupArmorModel(HumanoidArmorModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);

        // Set armor stand pose
        model.rightArm.xRot = 0.0F;
        model.rightArm.yRot = 0.0F;
        model.rightArm.zRot = 0.1F;
        model.leftArm.xRot = 0.0F;
        model.leftArm.yRot = 0.0F;
        model.leftArm.zRot = -0.1F;
        model.rightLeg.xRot = 0.0F;
        model.rightLeg.yRot = 0.0F;
        model.rightLeg.zRot = 0.05F;
        model.leftLeg.xRot = 0.0F;
        model.leftLeg.yRot = 0.0F;
        model.leftLeg.zRot = -0.05F;
        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        model.head.zRot = 0.0F;
        model.body.xRot = 0.0F;
        model.body.yRot = 0.0F;
        model.body.zRot = 0.0F;

        // Show only relevant parts for this slot
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
        }
    }

    private int getSlotIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> -1;
        };
    }

    private ResourceLocation getArmorResource(Holder<ArmorMaterial> materialHolder, EquipmentSlot slot, boolean isInnerModel) {
        String layer = isInnerModel ? "_layer_2" : "_layer_1";
        ResourceLocation materialKey = BuiltInRegistries.ARMOR_MATERIAL.getKey(materialHolder.value());

        if (materialKey != null) {
            return ResourceLocation.fromNamespaceAndPath(
                    materialKey.getNamespace(),
                    "textures/models/armor/" + materialKey.getPath() + layer + ".png"
            );
        }

        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/models/armor/iron" + layer + ".png");
    }
}
