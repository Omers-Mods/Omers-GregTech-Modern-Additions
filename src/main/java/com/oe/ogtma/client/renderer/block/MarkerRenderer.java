package com.oe.ogtma.client.renderer.block;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oe.ogtma.api.utility.LaserUtil;
import com.oe.ogtma.common.blockentity.MarkerBlockEntity;
import org.joml.Vector3f;

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

        var positions = marker.getPositions();
        for (int i = 0; i < positions.length; i++) {
            if (!marker.isEmpty(i)) {
                renderLaser(marker, positions[i], poseStack, bufferSource, partialTick,
                        marker.getLevel().getGameTime());
            }
        }

        poseStack.popPose();
    }

    public static Direction getDirection(Vector3f offset) {
        if (offset.x >= 1) {
            return Direction.EAST;
        } else if (offset.x <= -1) {
            return Direction.WEST;
        } else if (offset.y >= 1) {
            return Direction.UP;
        } else if (offset.y <= -1) {
            return Direction.DOWN;
        } else if (offset.z >= 1) {
            return Direction.SOUTH;
        } else {
            return Direction.NORTH;
        }
    }

    public static void renderLaser(MarkerBlockEntity marker, Vector3f dest, PoseStack poseStack,
                                   MultiBufferSource bufferSource, float partialTick, long gameTime) {
        var offset = dest.sub(marker.getSelfPos(), new Vector3f());
        var direction = getDirection(offset);
        if (direction.getAxisDirection().getStep() < 0) {
            return;
        }
        poseStack.pushPose();
        // adjust starting position to accommodate wall markers
        float beamHeight = 0;
        var selfPos = marker.getSelfPos();
        poseStack.translate(Mth.frac(selfPos.x), Mth.frac(selfPos.y), Mth.frac(selfPos.z));
        // calculate rotations if necessary
        if (direction.getAxis() != Direction.Axis.Y) {
            var beamDirection = new Vector3f(0, 1, 0);
            var wantedNorm = offset.normalize(new Vector3f());
            var rotAngle = (float) Math.acos(beamDirection.dot(wantedNorm));
            var rotAxis = beamDirection.cross(wantedNorm).normalize();
            poseStack.mulPose(Axis.of(rotAxis).rotation(rotAngle));
        }
        // adjust angles and beam height to match destination
        switch (direction.getAxis()) {
            case X -> beamHeight += Math.abs(offset.x);
            case Y -> beamHeight += Math.abs(offset.y);
            case Z -> beamHeight += Math.abs(offset.z);
        }
        var step = Math.floorMod(gameTime, 40) + partialTick;
        var signedStep = direction.getAxisDirection().getStep() < 0 ? step : -step;
        var vRelated = Mth.frac(signedStep * 0.2F - (float) Mth.floor(signedStep * 0.1F));
        var maxV = -1.0F + vRelated;
        var minV = beamHeight * (0.5F / .1f) + maxV;
        poseStack.mulPose(Axis.YP.rotationDegrees(step * 2.25F - 45.0F));
        LaserUtil.renderPart(poseStack,
                bufferSource.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, true)), .08f,
                .32f, 1f, .7f, 0, beamHeight, 0, .1f, .1f, 0, -.1f, 0, 0, -.1f, 0, 1, minV, maxV);

        poseStack.popPose();
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
            if (shouldRender(BlockPos.containing(pos.x, pos.y, pos.z), cameraPos)) {
                return true;
            }
        }
        return false;
    }
}
