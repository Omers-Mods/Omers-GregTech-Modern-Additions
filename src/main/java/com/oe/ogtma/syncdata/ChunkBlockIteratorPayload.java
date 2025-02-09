package com.oe.ogtma.syncdata;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.oe.ogtma.api.area.QuarryArea;
import org.jetbrains.annotations.Nullable;

public class ChunkBlockIteratorPayload extends ObjectTypedPayload<QuarryArea.ChunkBlockIterator> {

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        return payload.serializeNBT();
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (payload != null) {
            payload.deserializeNBT((CompoundTag) tag);
        }
    }
}
