/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.NukkitX.utils;

import cn.nukkit.utils.Config;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    public static boolean isFromSameHost(String ip) {
        try {
            InetAddress address = InetAddress.getByName(InetAddress.getByName(ip).getHostAddress());
            return address.isSiteLocalAddress() || address.isLoopbackAddress() || address.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static class VersionComparison {
        public static Version parse(String versionString) {
            String[] parts = versionString.split("\\.");
            int[] versionNumbers = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                versionNumbers[i] = Integer.parseInt(parts[i]);
            }
            return new Version(versionNumbers);
        }
        public static class Version implements Comparable<Version> {
            private final int[] versionNumbers;
            public Version(int[] versionNumbers) {
                this.versionNumbers = versionNumbers;
            }
            @Override
            public int compareTo(Version other) {
                for (int i = 0; i < Math.min(versionNumbers.length, other.versionNumbers.length); i++) {
                    int result = Integer.compare(versionNumbers[i], other.versionNumbers[i]);
                    if (result != 0) return result;
                }
                return Integer.compare(versionNumbers.length, other.versionNumbers.length);
            }
        }
    }
}
