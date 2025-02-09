package com.oe.ogtma.common.data.lang;

import com.oe.ogtma.OGTMA;
import com.tterrag.registrate.providers.RegistrateLangProvider;

public class OAIntegrationLang {

    public static void init(RegistrateLangProvider provider) {
        addJade(provider, "quarry", "Quarry");
    }

    protected static void addJade(RegistrateLangProvider provider, String name, String translation) {
        provider.add("config.jade.plugin_ogtma." + name, "[" + OGTMA.MOD_ID.toUpperCase() + "] " + translation);
    }
}
