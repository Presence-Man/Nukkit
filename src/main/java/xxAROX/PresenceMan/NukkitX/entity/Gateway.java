package xxAROX.PresenceMan.NukkitX.entity;

public class Gateway {
    public static String protocol = "http://";
    public static String address = "127.0.0.1";
    public static Integer port = null;
    public static boolean broken = false;

    public static String getUrl() {
        if (broken) throw new Error("Presence-Man Backend server is not reachable");
        return protocol + address + (port != null ? ":" + port : "");
    }
}
