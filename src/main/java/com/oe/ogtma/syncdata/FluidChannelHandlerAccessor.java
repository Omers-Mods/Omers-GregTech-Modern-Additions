package com.oe.ogtma.syncdata;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraft.nbt.CompoundTag;

import com.oe.ogtma.common.cache.fluid.FluidChannelHandler;

public class FluidChannelHandlerAccessor extends CustomObjectAccessor<FluidChannelHandler> {

    public FluidChannelHandlerAccessor() {
        super(FluidChannelHandler.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp accessorOp, FluidChannelHandler fluidChannelHandler) {
        return NbtTagPayload.of(fluidChannelHandler.serializeNBT());
    }

    @Override
    public FluidChannelHandler deserialize(AccessorOp accessorOp, ITypedPayload<?> payload) {
        if (payload instanceof NbtTagPayload nbtTagPayload && nbtTagPayload.getPayload() instanceof CompoundTag tag) {
            var handler = new FluidChannelHandler();
            handler.deserializeNBT(tag);
            return handler;
        }
        return null;
    }
}
