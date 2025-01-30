package com.oe.ogtma.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.PipeModel;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.oe.ogtma.common.blockentity.pipe.QuarryPipeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class QuarryPipeRenderer implements IRenderer {

    protected PipeModel pipeModel;

    public QuarryPipeRenderer(PipeModel pipeModel) {
        this.pipeModel = pipeModel;
        if (GTCEu.isClientSide()) {
            registerEvent();
        }
    }

    @Override
    public boolean useAO() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                       @Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        if (level == null) {
            return pipeModel.bakeQuads(side, PipeModel.ITEM_CONNECTIONS, 0);
        } else if (level.getBlockEntity(pos) instanceof QuarryPipeBlockEntity pipeBlockEntity) {
            var connections = pipeBlockEntity.getConnections();
            return new LinkedList<>(pipeModel.bakeQuads(side, connections, 0));
        }
        return Collections.emptyList();
    }

    @NotNull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return pipeModel.getParticleTexture();
    }
}
