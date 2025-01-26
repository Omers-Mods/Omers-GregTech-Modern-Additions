package com.oe.ogtma.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.oe.ogtma.common.blockentity.MarkerBlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MarkerRenderer implements BlockEntityRenderer<MarkerBlockEntity> {

    protected BlockEntityRendererProvider.Context context;

    public MarkerRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(MarkerBlockEntity marker, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (marker.isEmpty() || marker.getLevel() == null) {
            return;
        }

        poseStack.pushPose();

        // modify matrix
        poseStack.translate(.5, .6, .5);

        var positions = marker.getPositions();
        for (var pos : positions) {
            if (!pos.equals(marker.getBlockPos())) {
                renderLaser(marker, pos, poseStack, bufferSource, partialTick, marker.getLevel().getGameTime());
            }
        }

        poseStack.popPose();
    }

    public static Direction getDirection(Vec3i offset) {
        if (offset.getX() > 0) {
            return Direction.EAST;
        } else if (offset.getX() < 0) {
            return Direction.WEST;
        } else if (offset.getY() > 0) {
            return Direction.UP;
        } else if (offset.getY() < 0) {
            return Direction.DOWN;
        } else if (offset.getZ() > 0) {
            return Direction.SOUTH;
        } else {
            return Direction.NORTH;
        }
    }

    public static void renderLaser(MarkerBlockEntity marker, BlockPos dest, PoseStack poseStack,
                                   MultiBufferSource bufferSource, float partialTick, long gameTime) {
        var offset = dest.subtract(marker.getBlockPos());
        var direction = getDirection(offset);
        poseStack.pushPose();
        int beamHeight;
        switch (direction.getAxis()) {
            case X -> {
                if (direction.getStepX() < 0) {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                } else {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                }
                beamHeight = Math.abs(offset.getX());
            }
            case Y -> {
                if (direction.getStepY() < 0) {
                    poseStack.translate(0, -1, 0);
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                } else {
                    offset = offset.below();
                }
                beamHeight = Math.abs(offset.getY());
            }
            case Z -> {
                if (direction.getStepZ() < 0) {
                    poseStack.mulPose(Axis.XN.rotationDegrees(90));
                } else {
                    poseStack.mulPose(Axis.XN.rotationDegrees(-90));
                }
                beamHeight = Math.abs(offset.getZ());
            }
            default -> beamHeight = 0;
        }
        var step = Math.floorMod(gameTime, 40) + partialTick;
        var signedStep = direction.getAxisDirection().getStep() < 0 ? step : -step;
        var vRelated = Mth.frac(signedStep * 0.2F - (float) Mth.floor(signedStep * 0.1F));
        var maxV = -1.0F + vRelated;
        var minV = beamHeight * (0.5F / .1f) + maxV;
        poseStack.mulPose(Axis.YP.rotationDegrees(step * 2.25F - 45.0F));
        renderPart(poseStack, bufferSource.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, true)), .08f,
                .32f, 1f, .7f, 0, beamHeight, 0, .1f, .1f, 0, -.1f, 0, 0, -.1f, 0, 1, minV, maxV);

        poseStack.popPose();
    }

    public static void renderPart(PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue,
                                  float alpha, int minY, int maxY, float x0, float z0, float x1, float z1, float x2,
                                  float z2, float x3, float z3, float minU, float maxU, float minV, float maxV) {
        var pose = poseStack.last();
        var mat = pose.pose();
        var normalMat = pose.normal();
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x0, z0, x1, z1, minU, maxU, minV,
                maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x3, z3, x2, z2, minU, maxU, minV,
                maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x1, z1, x3, z3, minU, maxU, minV,
                maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x2, z2, x0, z0, minU, maxU, minV,
                maxV);
    }

    public static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen,
                                  float pBlue, float pAlpha, int pMinY, int pMaxY, float pMinX, float pMinZ,
                                  float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, pMaxU, pMinV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, pMaxU, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, pMinU, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, pMinU, pMinV);
    }

    public static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen,
                                 float pBlue, float pAlpha, int pY, float pX, float pZ, float pU, float pV) {
        pConsumer.vertex(pPose, pX, (float) pY, pZ)
                .color(pRed, pGreen, pBlue, pAlpha)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(pNormal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    protected boolean shouldRender(BlockPos pos, Vec3 cameraPos) {
        return Vec3.atCenterOf(pos).closerThan(cameraPos, getViewDistance());
    }

    @Override
    public boolean shouldRender(MarkerBlockEntity marker, Vec3 cameraPos) {
        if (shouldRender(marker.getBlockPos(), cameraPos)) {
            return true;
        }
        for (var pos : marker.getPositions()) {
            if (shouldRender(pos, cameraPos)) {
                return true;
            }
        }
        return false;
    }
}
