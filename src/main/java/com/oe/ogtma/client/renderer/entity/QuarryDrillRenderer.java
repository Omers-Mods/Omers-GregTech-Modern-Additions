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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oe.ogtma.OGTMA;
import com.oe.ogtma.api.utility.LaserUtil;
import com.oe.ogtma.client.layers.OALayers;
import com.oe.ogtma.client.model.entity.quarry.DrillModel;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryDrillRenderer extends EntityRenderer<QuarryDrillEntity> {

    public static final ResourceLocation TEXTURE = OGTMA.id("textures/entity/quarry/drill.png");

    protected EntityModel<QuarryDrillEntity> model;
    protected BlockRenderDispatcher blockRenderer;
    protected float yOffset;

    public QuarryDrillRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DrillModel(context.bakeLayer(OALayers.QUARRY_LAYER));
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(QuarryDrillEntity drill, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        var moveTarget = drill.getMoveTarget().above(4).getCenter();
        var posDelta = drill.position().vectorTo(moveTarget);
        packedLight = LightTexture.pack(Math.max(LightTexture.block(packedLight), 3), LightTexture.sky(packedLight));
        poseStack.translate(0, .5, 0);
        yOffset = .5f;
        if (posDelta.y < 0) {
            renderPipesX(drill, poseStack, bufferSource, packedLight, partialTick);
            renderPipesZ(drill, poseStack, bufferSource, packedLight, partialTick);
            renderPipesY(poseStack, bufferSource, packedLight, posDelta);
        } else {
            poseStack.translate(0, posDelta.y, 0);
        }
        renderDrill(drill, poseStack, bufferSource, packedLight);
        if (drill.getDeltaMovement().length() < .5 || drill.shouldTargetAir()) {
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
        poseStack.translate(-.375, -.5, -.375);
        yOffset -= .5f;
        poseStack.scale(.75f, 1, .75f);
        int i;
        for (i = 0; i < -(delta.y + .5); i++) {
            blockRenderer.renderSingleBlock(Blocks.COPPER_BLOCK.defaultBlockState(), poseStack, bufferSource,
                    packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0, -1, 0);
            yOffset--;
        }

        poseStack.scale(4f / 3f, 1, 4f / 3f);
        poseStack.translate(.375, 0, .375);
    }

    protected void renderPipesX(QuarryDrillEntity drill, PoseStack poseStack, MultiBufferSource bufferSource,
                                int packedLight, float partialTick) {
        poseStack.pushPose();

        // do stuff like placing blocks and shit
        var pX = drill.getQuarryBox().minX - Mth.lerp(partialTick, drill.xOld, drill.getX());
        poseStack.translate(pX + .5, -.375, -.375);
        poseStack.scale(1, .74f, .74f);
        int i;
        for (i = 0; i < drill.getQuarryBox().getXsize(); i++) {
            blockRenderer.renderSingleBlock(Blocks.COPPER_BLOCK.defaultBlockState(), poseStack, bufferSource,
                    packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.translate(1, 0, 0);
        }
        var leftOver = drill.getQuarryBox().getXsize() - i;
        if (leftOver > .2) {
            poseStack.scale((float) leftOver, 1, 1);
            blockRenderer.renderSingleBlock(Blocks.COPPER_BLOCK.defaultBlockState(), poseStack, bufferSource,
                    packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }

    protected void renderPipesZ(QuarryDrillEntity drill, PoseStack poseStack, MultiBufferSource bufferSource,
                                int packedLight, float partialTick) {
        poseStack.pushPose();

        // do stuff like placing blocks and shit
        var pZ = drill.getQuarryBox().minZ - Mth.lerp(partialTick, drill.zOld, drill.getZ());
        poseStack.translate(-.375, -.375, pZ + .5);
        poseStack.scale(.74f, .74f, 1);
        int i;
        for (i = 0; i < drill.getQuarryBox().getZsize(); i++) {
            blockRenderer.renderSingleBlock(Blocks.COPPER_BLOCK.defaultBlockState(), poseStack, bufferSource,
                    packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0, 0, 1);
        }
        var leftOver = drill.getQuarryBox().getZsize() - i;
        if (leftOver > .2) {
            poseStack.scale(1, 1, (float) leftOver);
            blockRenderer.renderSingleBlock(Blocks.COPPER_BLOCK.defaultBlockState(), poseStack, bufferSource,
                    packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }

    protected void handleLasers(QuarryDrillEntity drill, float partialTick, PoseStack poseStack,
                                MultiBufferSource bufferSource) {
        var level = drill.level();
        var gameTime = level.getGameTime();
        var pos = drill.position().add(0, yOffset, 0);
        var center = new Vector3f();
        int color;
        var quarryPos = drill.getQuarryPos();
        for (var target : drill.getTargets()) {
            if (target.equals(quarryPos)) {
                break;
            }
            center.set(target.getX() + .5f, target.getY() + .9f, target.getZ() + .5f);
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
            LaserUtil.renderLaser(center.sub((float) pos.x, (float) pos.y, (float) pos.z), poseStack, bufferSource,
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
