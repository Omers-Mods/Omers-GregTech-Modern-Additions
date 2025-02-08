package com.oe.ogtma.config;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.config.category.FeatureConfigs;
import com.oe.ogtma.config.category.QuarryConfigs;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = OGTMA.MOD_ID)
public class OAConfig {

    public static OAConfig INSTANCE;
    private static final Object LOCK = new Object();

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Configuration.registerConfig(OAConfig.class, ConfigFormats.yaml()).getConfigInstance();
            }
        }
    }

    @Configurable
    public FeatureConfigs features = new FeatureConfigs();
    @Configurable
    public QuarryConfigs quarry = new QuarryConfigs();
}
