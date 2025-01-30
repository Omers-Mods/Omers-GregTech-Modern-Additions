package com.oe.ogtma.common.blockentity.pipe;

import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class QuarryPipeBlockEntity extends BlockEntity
                                   implements IEnhancedManaged, IAsyncAutoSyncBlockEntity, IAutoPersistBlockEntity {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuarryPipeBlockEntity.class);
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

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

    @Override
    public void scheduleRenderUpdate() {
        if (level != null) {
            var pos = getBlockPos();
            var state = level.getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return syncStorage;
    }

    @Override
    public void onChanged() {
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(this::setChanged);
        }
    }

    @Override
    public IManagedStorage getRootStorage() {
        return syncStorage;
    }
}
