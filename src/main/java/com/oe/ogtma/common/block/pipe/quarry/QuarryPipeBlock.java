package com.oe.ogtma.common.block.pipe.quarry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.PipeModel;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;

import com.oe.ogtma.client.model.pipe.QuarryPipeModel;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.oe.ogtma.client.renderer.block.QuarryPipeRenderer;
import com.oe.ogtma.common.blockentity.pipe.QuarryPipeBlockEntity;
import com.oe.ogtma.common.data.OABlockEntities;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryPipeBlock extends Block implements IBlockRendererProvider, EntityBlock {

    public final QuarryPipeModel model;
    public final QuarryPipeRenderer renderer;

    public QuarryPipeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.model = new QuarryPipeModel(.75f, () -> GTCEu.id("block/pipe/pipe_side"),
                () -> GTCEu.id("block/pipe/pipe_large_in"), null, null);
        this.renderer = new QuarryPipeRenderer(model);
    }

    @Nullable
    @Override
    public QuarryPipeRenderer getRenderer(BlockState blockState) {
        return renderer;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return OABlockEntities.QUARRY_PIPE.create(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof QuarryPipeBlockEntity qpbe) {
            return model.getShapes(qpbe.getConnections());
        }
        return super.getShape(state, level, pos, context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof QuarryPipeBlockEntity qpbe) {
            return model.getShapes(qpbe.getConnections());
        }
        return super.getCollisionShape(state, level, pos, context);
    }
}
