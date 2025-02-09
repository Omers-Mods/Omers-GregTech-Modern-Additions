package com.oe.ogtma.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.cover.multiblock.ModeSwitcherCover;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.oe.ogtma.OGTMA.REGISTRATE;

public class OACovers {

    public static final CoverDefinition MODE_SWITCHER;
    static {
        MODE_SWITCHER = GTCovers.register(
                "mode_switcher", ModeSwitcherCover::new,
                new SimpleCoverRenderer(OGTMA.id("block/cover/mode_switcher")));
    }
    public static final ItemEntry<ComponentItem> COVER_MODE_SWITCHER;
    static {
        COVER_MODE_SWITCHER = REGISTRATE
                .item("mode_switcher_cover", ComponentItem::create)
                .lang("Mode Switcher Cover")
                .onRegister(attach(new CoverPlaceBehavior(OACovers.MODE_SWITCHER)))
                .register();
    }

    public static void init() {}
}
