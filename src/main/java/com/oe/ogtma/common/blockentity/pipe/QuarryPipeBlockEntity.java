package com.oe.ogtma.common.blockentity.pipe;

import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class QuarryPipeBlockEntity extends BlockEntity {

    public QuarryPipeBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public int getConnections() {
        var connections = Node.ALL_CLOSED;
        for (var side : GTUtil.DIRECTIONS) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof QuarryPipeBlockEntity) {
                connections |= 1 << side.ordinal();
            }
        }
        return connections;
    }
}
