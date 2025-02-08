package com.oe.ogtma.common.data.lang;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.config.OAConfig;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import dev.toma.configuration.config.value.ConfigValue;
import dev.toma.configuration.config.value.ObjectValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OAConfigLang {

    public static void init(RegistrateLangProvider provider) {
        dfs(provider, new HashSet<>(),
                Configuration.registerConfig(OAConfig.class, ConfigFormats.yaml()).getValueMap());
    }

    private static void dfs(RegistrateLangProvider provider, Set<String> added, Map<String, ConfigValue<?>> map) {
        for (var entry : map.entrySet()) {
            var id = entry.getValue().getId();
            if (added.add(id)) {
                provider.add(String.format("config.%s.option.%s", OGTMA.MOD_ID, id), processId(id));
            }

            var value = entry.getValue();
            if (value instanceof ObjectValue objectValue) {
                dfs(provider, added, objectValue.get());
            }
        }
    }

    private static String processId(String id) {
        var builder = new StringBuilder();
        builder.append(Character.toUpperCase(id.charAt(0)));
        for (int i = 1; i < id.length(); i++) {
            var curr = id.charAt(i);
            if (Character.isUpperCase(curr)) {
                builder.append(" ");
            }
            builder.append(curr);
        }
        return builder.toString().strip();
    }
}
