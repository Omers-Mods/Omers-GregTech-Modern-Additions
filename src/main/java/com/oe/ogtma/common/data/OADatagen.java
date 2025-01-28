package com.oe.ogtma.common.data;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.data.lang.OALangHandler;
import com.tterrag.registrate.providers.ProviderType;

public class OADatagen {

    public static void init() {
        OGTMA.REGISTRATE.addDataGenerator(ProviderType.LANG, OALangHandler::init);
    }
}
