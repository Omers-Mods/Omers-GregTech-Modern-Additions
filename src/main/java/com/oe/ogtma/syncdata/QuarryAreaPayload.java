package com.oe.ogtma.syncdata;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.oe.ogtma.api.area.QuarryArea;
import org.jetbrains.annotations.Nullable;

public class QuarryAreaPayload extends ObjectTypedPayload<QuarryArea> {

    @Nullable
    @Override
    public Tag serializeNBT() {
        return payload.serializeNBT();
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag cTag) {
            payload = new QuarryArea();
            payload.deserializeNBT(cTag);
        }
    }
}
