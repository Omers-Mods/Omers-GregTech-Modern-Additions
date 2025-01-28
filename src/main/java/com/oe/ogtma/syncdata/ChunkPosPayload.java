package com.oe.ogtma.syncdata;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import org.jetbrains.annotations.Nullable;

public class ChunkPosPayload extends ObjectTypedPayload<ChunkPos> {

    @Nullable
    @Override
    public Tag serializeNBT() {
        return new IntArrayTag(new int[] { payload.x, payload.z });
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof IntArrayTag arrTag) {
            var xz = arrTag.getAsIntArray();
            if (xz.length >= 2) {
                payload = new ChunkPos(xz[0], xz[1]);
            }
        } else {
            payload = new ChunkPos(ChunkPos.INVALID_CHUNK_POS);
        }
    }

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        buf.writeChunkPos(payload);
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        payload = buf.readChunkPos();
    }
}
