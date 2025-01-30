package com.oe.ogtma.client.renderer.entity;

import com.gregtechceu.gtceu.api.block.MaterialBlock;

import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oe.ogtma.OGTMA;
import com.oe.ogtma.api.utility.LaserUtil;
import com.oe.ogtma.client.layers.OALayers;
import com.oe.ogtma.client.model.entity.quarry.DrillModel;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;
import org.apache.commons.lang3.function.TriFunction;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryDrillRenderer extends EntityRenderer<QuarryDrillEntity> {

    public static final ResourceLocation TEXTURE = OGTMA.id("textures/entity/quarry/drill.png");
    protected static final TriFunction<BlockState, Level, BlockPos, TextureAtlasSprite> getSprite = Minecraft
            .getInstance().getBlockRenderer().getBlockModelShaper()::getTexture;

    protected EntityModel<QuarryDrillEntity> model;

    public QuarryDrillRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new DrillModel(context.bakeLayer(OALayers.QUARRY_LAYER));
    }

    @Override
    public void render(QuarryDrillEntity drill, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        if (drill.getDeltaMovement().length() < .25) {
            var level = drill.level();
            var gameTime = level.getGameTime();
            var pos = drill.position();
            var center = new Vector3f();
            int color;
            var quarryPos = drill.getQuarryPos();
            for (var target : drill.getTargets()) {
                if (target.equals(quarryPos)) {
                    break;
                }
                if (target.getY() > pos.y - 1) {
                    continue;
                }
                center.set(target.getX() + .5f, target.getY() + .5f, target.getZ() + .5f);
                var state = level.getBlockState(target);
                if (state.isAir()) {
                    continue;
                }
                if (state.getBlock() instanceof MaterialBlock materialBlock) {
                    color = materialBlock.material.getMaterialRGB();
                } else {
                    color = state.getMapColor(level, target).col;
                }
                LaserUtil.renderLaser(center.sub((float) pos.x, (float) pos.y, (float) pos.z), poseStack, bufferSource,
                        ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), 1, partialTick,
                        gameTime);
            }
        }
        var bb = drill.getBoundingBox();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(0, -3, 0);
        poseStack.scale((float) bb.getXsize(), (float) bb.getYsize(), (float) bb.getZsize());
        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(model.renderType(getTextureLocation(drill))),
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(QuarryDrillEntity entity, Frustum pCamera, double pCamX, double pCamY,
                                double pCamZ) {
        return pCamera.isVisible(entity.getQuarryBox());
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
