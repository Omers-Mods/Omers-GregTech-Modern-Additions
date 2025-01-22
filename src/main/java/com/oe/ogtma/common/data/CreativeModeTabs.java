package com.oe.ogtma.common.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.RegistrateDisplayItemsGenerator;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.oe.ogtma.OGTMA;
import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.oe.ogtma.OGTMA.*;

public class CreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> GENERAL = REGISTRATE.defaultCreativeTab(MOD_ID,
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator(MOD_ID, REGISTRATE))
                    .icon(() -> new ItemStack(Items.DIAMOND_BLOCK))
                    .title(REGISTRATE.addLang("tab", OGTMA.id("tab"), NAME))
                    .build())
            .register();

    public static void init() {}
}
