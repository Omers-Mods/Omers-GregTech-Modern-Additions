package com.oe.ogtma.common.network.packets.dev;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ForcedChunksSavedData;

import com.oe.ogtma.client.cache.LoadedChunksCache;
import com.oe.ogtma.common.network.OANetwork;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChunkLoadedPacket implements IPacket {

    protected long chunkPos;
    protected boolean loaded;

    public ChunkLoadedPacket(long chunkPos) {
        this(chunkPos, false);
    }

    public ChunkLoadedPacket(long chunkPos, boolean loaded) {
        this.chunkPos = chunkPos;
        this.loaded = loaded;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(chunkPos);
        buf.writeBoolean(loaded);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.chunkPos = buf.readLong();
        this.loaded = buf.readBoolean();
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.isClient()) {
            LoadedChunksCache.LOADED.clear();
            if (loaded) {
                LoadedChunksCache.LOADED.add(chunkPos);
            }
        } else {
            var data = ((ServerLevel) handler.getLevel()).getDataStorage()
                    .get(ForcedChunksSavedData::load, "chunks");
            this.loaded = data.getBlockForcedChunks().getTickingChunks().values().stream()
                    .anyMatch(set -> set.contains(chunkPos));
            OANetwork.NETWORK.sendToPlayer(this, handler.getPlayer());
        }
    }
}
