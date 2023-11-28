/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.NukkitX.utils;

import cn.nukkit.utils.Config;

import java.util.Locale;

public final class Utils {
    public static Object getconfigvalue(Config config, String key, String env, Object defaultValue){
        if (env.isEmpty()) env = key.toUpperCase(Locale.ROOT);
        if (!env.startsWith("PRESENCE_MAN_")) env = "PRESENCE_MAN_" + env;
        String val = System.getenv(env);
        if (val == null || val.isEmpty()) return config.get(key, defaultValue);
        else return val;
    }
    public static Object getconfigvalue(Config config, String key, String env){
        return getconfigvalue(config, key, env, null);
    }
    public static Object getconfigvalue(Config config, String key){
        return getconfigvalue(config, key, "", null);
    }
}
