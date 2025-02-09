package com.oe.ogtma.common.data;

import net.minecraft.world.level.ChunkPos;

import com.oe.ogtma.api.area.QuarryArea;
import com.oe.ogtma.syncdata.AreaChunkIteratorPayload;
import com.oe.ogtma.syncdata.ChunkBlockIteratorPayload;
import com.oe.ogtma.syncdata.ChunkPosPayload;
import com.oe.ogtma.syncdata.QuarryAreaPayload;

import static com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries.registerSimple;

public class OASyncedFieldAccessors {

    public static void init() {
        registerSimple(ChunkPosPayload.class, ChunkPosPayload::new, ChunkPos.class, 100);
        registerSimple(QuarryAreaPayload.class, QuarryAreaPayload::new, QuarryArea.class, 100);
        registerSimple(ChunkBlockIteratorPayload.class, ChunkBlockIteratorPayload::new,
                QuarryArea.ChunkBlockIterator.class, 100);
        registerSimple(AreaChunkIteratorPayload.class, AreaChunkIteratorPayload::new,
                QuarryArea.AreaChunkIterator.class, 100);
    }
}
