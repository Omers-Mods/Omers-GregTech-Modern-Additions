package com.oe.ogtma.common.data;

import com.oe.ogtma.client.renderer.block.MarkerRenderer;
import com.oe.ogtma.common.blockentity.MarkerBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.oe.ogtma.OGTMA.REGISTRATE;

public class OABlockEntities {

    public static final BlockEntityEntry<MarkerBlockEntity> MARKER = REGISTRATE
            .blockEntity("marker", MarkerBlockEntity::new)
            .renderer(() -> MarkerRenderer::new)
            .validBlocks(OABlocks.MARKER, OABlocks.WALL_MARKER)
            .register();

    public static void init() {}
}
