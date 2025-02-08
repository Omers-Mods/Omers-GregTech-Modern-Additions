package com.oe.ogtma.client.renderer.entity;

import com.gregtechceu.gtceu.api.block.MaterialBlock;

import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oe.ogtma.OGTMA;
import com.oe.ogtma.api.utility.LaserUtil;
import com.oe.ogtma.client.layers.OALayers;
import com.oe.ogtma.client.model.entity.quarry.DrillModel;
import com.oe.ogtma.common.data.OAMaterialBlocks;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;
import com.oe.ogtma.config.OAConfig;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryDrillRenderer extends EntityRenderer<QuarryDrillEntity> {

    public static final ResourceLocation TEXTURE = OGTMA.id("textures/entity/quarry/drill.png");

    protected EntityModel<QuarryDrillEntity> model;
    protected BlockRenderDispatcher blockRenderer;
    protected float yOffset;
    protected int tier;

    public QuarryDrillRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DrillModel(context.bakeLayer(OALayers.QUARRY_LAYER));
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(QuarryDrillEntity drill, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (!OAConfig.INSTANCE.quarry.renderQuarryDrill) {
            return;
        }
        tier = drill.getTier();
        var target = drill.getMoveTarget();
        var pos = drill.position();
        var posDelta = new Vec3(target.getX() - pos.x, target.getY() + 3 - pos.y, target.getZ() - pos.z);
        if (posDelta.y >= 0) {
            return;
        }
        poseStack.pushPose();
        packedLight = LightTexture.pack(Math.max(LightTexture.block(packedLight), 1), LightTexture.sky(packedLight));
        poseStack.translate(0, .5, 0);
        yOffset = .5f;
        renderPipesX(drill, poseStack, bufferSource, packedLight, partialTick);
        renderPipesZ(drill, poseStack, bufferSource, packedLight, partialTick);
        renderPipesY(poseStack, bufferSource, packedLight, posDelta);
        renderDrill(drill, poseStack, bufferSource, packedLight);
        if (drill.getDeltaMovement().length() < 1 || drill.shouldTargetAir()) {
            handleLasers(drill, partialTick, poseStack, bufferSource);
        }
        poseStack.popPose();
    }

    protected void renderDrill(QuarryDrillEntity drill, PoseStack poseStack, MultiBufferSource bufferSource,
                               int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(0, -3, 0);
        poseStack.scale(2.01F, 2F, 2.01F);
        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(model.renderType(getTextureLocation(drill))),
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
    }

    protected void renderPipesY(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Vec3 delta) {
        poseStack.translate(-.4, -.5, -.4);
        yOffset -= .5f;
        poseStack.scale(.8f, 1, .8f);
        int i;
        for (i = 0; i < -(delta.y + .5); i++) {
            blockRenderer.renderSingleBlock(OAMaterialBlocks.QUARRY_BLOCKS[tier].getDefaultState(), poseStack,
                    bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0, -1, 0);
            yOffset--;
        }

        poseStack.scale(1.25f, 1, 1.25f);
        poseStack.translate(.4, 0, .4);
    }

    protected void renderPipesX(QuarryDrillEntity drill, PoseStack poseStack, MultiBufferSource bufferSource,
                                int packedLight, float partialTick) {
        poseStack.pushPose();

        // do stuff like placing blocks and shit
        var pX = drill.getQuarryBox().minX - Mth.lerp(partialTick, drill.xOld, drill.getX());
        poseStack.translate(pX + .5, -.35, -.35);
        poseStack.scale(1, .7f, .7f);
        int i;
        for (i = 0; i < drill.getQuarryBox().getXsize(); i++) {
            blockRenderer.renderSingleBlock(OAMaterialBlocks.QUARRY_BLOCKS[tier].getDefaultState(), poseStack,
                    bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.translate(1, 0, 0);
        }
        var leftOver = drill.getQuarryBox().getXsize() - i;
        if (leftOver > .2) {
            poseStack.scale((float) leftOver, 1, 1);
            blockRenderer.renderSingleBlock(OAMaterialBlocks.QUARRY_BLOCKS[tier].getDefaultState(), poseStack,
                    bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }

    protected void renderPipesZ(QuarryDrillEntity drill, PoseStack poseStack, MultiBufferSource bufferSource,
                                int packedLight, float partialTick) {
        poseStack.pushPose();

        // do stuff like placing blocks and shit
        var pZ = drill.getQuarryBox().minZ - Mth.lerp(partialTick, drill.zOld, drill.getZ());
        poseStack.translate(-.35, -.35, pZ + .5);
        poseStack.scale(.7f, .7f, 1);
        int i;
        for (i = 0; i < drill.getQuarryBox().getZsize(); i++) {
            blockRenderer.renderSingleBlock(OAMaterialBlocks.QUARRY_BLOCKS[tier].getDefaultState(), poseStack,
                    bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0, 0, 1);
        }
        var leftOver = drill.getQuarryBox().getZsize() - i;
        if (leftOver > .2) {
            poseStack.scale(1, 1, (float) leftOver);
            blockRenderer.renderSingleBlock(OAMaterialBlocks.QUARRY_BLOCKS[tier].getDefaultState(), poseStack,
                    bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }

    protected void handleLasers(QuarryDrillEntity drill, float partialTick, PoseStack poseStack,
                                MultiBufferSource bufferSource) {
        var level = drill.level();
        var gameTime = level.getGameTime();
        var selfPos = drill.position();
        var pos = new Vec3(Mth.lerp(partialTick, drill.xOld, selfPos.x),
                Mth.lerp(partialTick, drill.yOld, selfPos.y) + yOffset, Mth.lerp(partialTick, drill.zOld, selfPos.z));
        var center = new Vector3f();
        int color;
        var quarryPos = drill.getQuarryPos();
        for (var target : drill.getTargets()) {
            if (target.equals(quarryPos)) {
                break;
            }
            double x = target.getX() + .5, y = target.getY() + .9, z = target.getZ() + .5;
            var state = level.getBlockState(target);
            var isAir = state.isAir();
            if (isAir && !drill.shouldTargetAir()) {
                continue;
            }
            if (state.getBlock() instanceof MaterialBlock materialBlock) {
                color = materialBlock.material.getMaterialRGB();
            } else if (!isAir) {
                color = state.getMapColor(level, target).col;
            } else {
                color = drill.getAirColor();
            }
            LaserUtil.renderLaser(center.set(x - pos.x, y - pos.y, z - pos.z), poseStack, bufferSource,
                    ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), 1, partialTick,
                    gameTime);
        }
    }

    @Override
    protected int getBlockLightLevel(QuarryDrillEntity pEntity, BlockPos pPos) {
        return Math.max(super.getBlockLightLevel(pEntity, pPos), 3);
    }

    @Override
    protected boolean shouldShowName(QuarryDrillEntity entity) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(QuarryDrillEntity entity) {
        return TEXTURE;
    }
}
