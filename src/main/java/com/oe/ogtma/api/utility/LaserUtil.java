package com.oe.ogtma.api.utility;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class LaserUtil {

    public static void renderLaser(Vector3f ray, PoseStack poseStack, MultiBufferSource bufferSource,
                                   float red, float green, float blue, float alpha, float partialTick, long gameTime) {
        renderLaser(ray, poseStack, bufferSource, red, green, blue, alpha, partialTick, gameTime,
                BeaconRenderer.BEAM_LOCATION);
    }

    public static void renderLaser(Vector3f ray, PoseStack poseStack, MultiBufferSource bufferSource,
                                   float red, float green, float blue, float alpha, float partialTick, long gameTime,
                                   ResourceLocation texture) {
        poseStack.pushPose();
        // calculate rotation to point in ray direction
        var beamDirection = new Vector3f(0, 1, 0);
        var wantedNorm = ray.normalize(new Vector3f());
        var axisAngle = MathUtils.getRotationAxisAndAngle(beamDirection, wantedNorm);
        poseStack.mulPose(Axis.of(axisAngle.getLeft()).rotation(axisAngle.getRight()));
        // calculate beam length and self rotation
        var beamLength = ray.length();
        var step = Math.floorMod(gameTime, 40) + partialTick;
        var vRelated = Mth.frac(step * 0.2F - (float) Mth.floor(step * 0.1F));
        var maxV = -1.0F + vRelated;
        var minV = beamLength * (0.5F / .1f) + maxV;
        poseStack.mulPose(Axis.YP.rotationDegrees(step * 2.25F - 45.0F));
        renderPart(poseStack, bufferSource.getBuffer(RenderType.beaconBeam(texture, false)),
                red, green, blue, alpha, 0, beamLength,
                0, .1f, .1f, 0, -.1f, 0, 0, -.1f, 0, 1, minV, maxV);

        poseStack.popPose();
    }

    public static void renderPart(PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue,
                                  float alpha, float minY, float maxY, float x0, float z0, float x1, float z1, float x2,
                                  float z2, float x3, float z3, float minU, float maxU, float minV, float maxV) {
        var pose = poseStack.last();
        var mat = pose.pose();
        var normalMat = pose.normal();
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x0, z0, x1, z1,
                minU, maxU, minV, maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x3, z3, x2, z2,
                minU, maxU, minV, maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x1, z1, x3, z3,
                minU, maxU, minV, maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x2, z2, x0, z0,
                minU, maxU, minV, maxV);
    }

    public static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen,
                                  float pBlue, float pAlpha, float pMinY, float pMaxY, float pMinX, float pMinZ,
                                  float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, pMaxU, pMinV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, pMaxU, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, pMinU, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, pMinU, pMinV);
    }

    public static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen,
                                 float pBlue, float pAlpha, float pY, float pX, float pZ, float pU, float pV) {
        pConsumer.vertex(pPose, pX, pY, pZ)
                .color(pRed, pGreen, pBlue, pAlpha)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(pNormal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
