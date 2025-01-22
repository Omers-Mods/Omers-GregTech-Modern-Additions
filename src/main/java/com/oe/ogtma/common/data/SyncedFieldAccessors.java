package com.oe.ogtma.common.data;

import com.lowdragmc.lowdraglib.syncdata.IAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;

import com.oe.ogtma.syncdata.FluidChannelHandlerAccessor;

import static com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries.register;

public class SyncedFieldAccessors {

    public static final IAccessor FLUID_CHANNEL_HANDLER_ACCESSOR = new FluidChannelHandlerAccessor();

    public static void init() {
        register(FriendlyBufPayload.class, FriendlyBufPayload::new, FLUID_CHANNEL_HANDLER_ACCESSOR, 1000);
    }
}
