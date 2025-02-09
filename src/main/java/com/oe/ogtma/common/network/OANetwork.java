package com.oe.ogtma.common.network;

import com.lowdragmc.lowdraglib.networking.INetworking;
import com.lowdragmc.lowdraglib.networking.forge.LDLNetworkingImpl;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.network.packets.dev.ChunkLoadedPacket;

public class OANetwork {

    public static final INetworking NETWORK = LDLNetworkingImpl.createNetworking(OGTMA.id("network"), "0.0.1");

    public static void init() {
        NETWORK.registerBoth(ChunkLoadedPacket.class);
    }
}
