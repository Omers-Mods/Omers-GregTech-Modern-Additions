package com.oe.ogtma.common.data;

import net.minecraft.world.entity.MobCategory;

import com.oe.ogtma.client.renderer.entity.QuarryDrillRenderer;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;
import com.tterrag.registrate.util.entry.EntityEntry;

import static com.oe.ogtma.OGTMA.REGISTRATE;

public class OAEntities {

    public static final EntityEntry<QuarryDrillEntity> QUARRY_DRILL = REGISTRATE
            .entity("quarry_drill", QuarryDrillEntity::new, MobCategory.MISC)
            .lang("Quarry Drill")
            .renderer(() -> QuarryDrillRenderer::new)
            .properties(builder -> builder
                    .sized(2, 2)
                    .clientTrackingRange(10)
                    .updateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .fireImmune()
                    .noSave())
            .register();

    public static void init() {}
}
