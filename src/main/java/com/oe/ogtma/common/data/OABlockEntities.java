package com.oe.ogtma.common.data;

import com.oe.ogtma.client.renderer.block.MarkerRenderer;
import com.oe.ogtma.common.blockentity.marker.MarkerBlockEntity;
import com.oe.ogtma.common.blockentity.pipe.QuarryPipeBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.oe.ogtma.OGTMA.REGISTRATE;

public class OABlockEntities {

    public static final BlockEntityEntry<MarkerBlockEntity> MARKER = REGISTRATE
            .blockEntity("marker", MarkerBlockEntity::new)
            .renderer(() -> MarkerRenderer::new)
            .validBlocks(OABlocks.MARKER, OABlocks.WALL_MARKER)
            .register();

    public static final BlockEntityEntry<QuarryPipeBlockEntity> QUARRY_PIPE = REGISTRATE
            .blockEntity("quarry_pipe", QuarryPipeBlockEntity::new)
            .validBlock(OABlocks.QUARRY_PIPE_BLOCK)
            .register();

    public static void init() {}
}
