package com.oe.ogtma.common.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class OAMachineLang {

    protected static void init(RegistrateLangProvider provider) {
        // quarry
        provider.add("ogtma.machine.quarry.tooltip", "§7Mines a marked area");
        provider.add("ogtma.machine.quarry.working_area_max", "§bThe max area is limited only by the marker range");
    }
}
