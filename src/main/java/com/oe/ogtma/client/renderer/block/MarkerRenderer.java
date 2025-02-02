package com.oe.ogtma.client.renderer.block;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.oe.ogtma.api.utility.LaserUtil;
import com.oe.ogtma.common.blockentity.marker.MarkerBlockEntity;
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

        var selfPos = marker.getSelfPos();
        var positions = marker.getPositions();
        var gameTime = marker.getLevel().getGameTime();
        poseStack.translate(Mth.frac(selfPos.x), Mth.frac(selfPos.y), Mth.frac(selfPos.z));
        for (int i = 0; i < positions.length; i++) {
            if (marker.isEmpty(i)) {
                continue;
            }
            var target = positions[i];
            var ray = new Vector3f((float) (target.x - selfPos.x), (float) (target.y - selfPos.y),
                    (float) (target.z - selfPos.z));
            LaserUtil.renderLaser(ray, poseStack, bufferSource, .08f, .32f, 1f, .7f, partialTick, gameTime);
        }

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
