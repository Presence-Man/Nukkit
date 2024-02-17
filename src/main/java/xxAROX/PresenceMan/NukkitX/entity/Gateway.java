/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.NukkitX.entity;

public final class Gateway {
    public static String protocol = "http://";
    public static String address = "127.0.0.1";
    public static Integer port = null;
    public static boolean broken = false;

    public static String getUrl() throws RuntimeException{
        if (broken) throw new RuntimeException("Presence-Man Backend server is not reachable");
        return protocol + address + (port != null ? ":" + port : "");
    }
}
