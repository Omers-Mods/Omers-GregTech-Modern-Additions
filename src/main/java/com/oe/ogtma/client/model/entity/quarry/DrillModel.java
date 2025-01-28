package com.oe.ogtma.client.model.entity.quarry;
// Made with Blockbench 4.12.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DrillModel extends EntityModel<QuarryDrillEntity> {

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into
    // this model's constructor
    private final ModelPart DrillModel;
    private final ModelPart Cover;
    private final ModelPart Connectors;

    public DrillModel(ModelPart root) {
        this.DrillModel = root.getChild("DrillModel");
        this.Cover = this.DrillModel.getChild("Cover");
        this.Connectors = this.DrillModel.getChild("Connectors");
    }

    public static LayerDefinition createBodyLayer() {
        var meshdefinition = new MeshDefinition();
        var partdefinition = meshdefinition.getRoot();

        var DrillModel = partdefinition.addOrReplaceChild("DrillModel", CubeListBuilder.create()
                .texOffs(4, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        var Cover = DrillModel.addOrReplaceChild("Cover", CubeListBuilder.create()
                .texOffs(12, 9).addBox(-3.0F, -9.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(-3.0F, -9.0F, -2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(-3.0F, -9.0F, -1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(-3.0F, -9.0F, 0.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(-3.0F, -9.0F, 1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(-3.0F, -9.0F, 2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(-2.0F, -9.0F, 2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(-1.0F, -9.0F, 2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(0.0F, -9.0F, 2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(1.0F, -9.0F, 2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(2.0F, -9.0F, 2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(2.0F, -9.0F, 1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(2.0F, -9.0F, 0.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(2.0F, -9.0F, -1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(2.0F, -9.0F, -2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(2.0F, -9.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(1.0F, -9.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(0.0F, -9.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 9).addBox(-1.0F, -9.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 9).addBox(-2.0F, -9.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var Connectors = DrillModel.addOrReplaceChild("Connectors", CubeListBuilder.create()
                .texOffs(-4, 0).addBox(-2.0F, -9.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-4, 0).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(QuarryDrillEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        DrillModel.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
