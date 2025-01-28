package com.oe.ogtma.common.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class OALangHandler {

    public static void init(RegistrateLangProvider provider) {
        OAMachineLang.init(provider);

        // QuarryMode tooltips
        provider.add("ogtma.option.quarry_mode.horizontal", "Horizontal");
        provider.add("ogtma.option.quarry_mode.vertical", "Vertical");
    }
}
