package com.oe.ogtma.common.block.marker;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.oe.ogtma.common.blockentity.marker.MarkerBlockEntity;
import com.oe.ogtma.common.data.OABlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public class MarkerBlock extends TorchBlock implements EntityBlock {

    private long counter;

    public MarkerBlock(Properties pProperties) {
        super(pProperties, null);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide || !hand.equals(InteractionHand.MAIN_HAND)) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof MarkerBlockEntity marker) {
            marker.interact();
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return OABlockEntities.MARKER.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || counter++ % 10 != 0) {
            return null;
        }
        return (l, p, s, be) -> ((MarkerBlockEntity) be).updateConnections();
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {}
}
