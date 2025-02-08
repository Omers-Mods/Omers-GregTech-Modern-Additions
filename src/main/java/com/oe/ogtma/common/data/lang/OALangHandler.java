package com.oe.ogtma.common.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class OALangHandler {

    public static void init(RegistrateLangProvider provider) {
        OAMachineLang.init(provider);
        OAConfigLang.init(provider);

        // QuarryMode tooltips
        provider.add("ogtma.option.quarry_mode", "Quarry Mode");
        provider.add("ogtma.option.quarry_mode.horizontal", "Horizontal");
        provider.add("ogtma.option.quarry_mode.vertical", "Vertical");

        // QuarryFluidMode tooltips
        provider.add("ogtma.option.quarry_fluid_mode", "Fluid Mode");
        provider.add("ogtma.option.quarry_fluid_mode.collect", "Collect");
        provider.add("ogtma.option.quarry_fluid_mode.stop", "Stop");
        provider.add("ogtma.option.quarry_fluid_mode.void", "Void Excess");
    }
}
