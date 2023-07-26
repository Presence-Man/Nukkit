package xxAROX.PresenceMan.NukkitX;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import xxAROX.PresenceMan.NukkitX.entity.ActivityType;
import xxAROX.PresenceMan.NukkitX.entity.ApiActivity;
import xxAROX.PresenceMan.NukkitX.entity.ApiRequest;
import xxAROX.PresenceMan.NukkitX.tasks.async.BackendRequest;
import xxAROX.PresenceMan.NukkitX.tasks.async.FetchGatewayInformationTask;

import java.util.HashMap;
import java.util.Map;

public class PresenceMan extends PluginBase {

    private static PresenceMan instance;

    private static String token = "undefined";
    private static String client_id = null;
    public static String server = "undefined";
    public static Boolean enable_default = true;

    public static Map<String, ApiActivity> presences = new HashMap<>();
    public static ApiActivity default_activity;

    @Override
    public void onLoad() {
        instance = this;
        this.saveResource("config.yml");
        Config config = this.getConfig();

        token = System.getenv("PRESENCE_MAN_TOKEN") != null && !System.getenv("PRESENCE_MAN_TOKEN").isEmpty() ?
                System.getenv("PRESENCE_MAN_TOKEN") : config.getString("token", token);

        client_id = System.getenv("PRESENCE_MAN_CLIENT_ID") != null && !System.getenv("PRESENCE_MAN_CLIENT_ID").isEmpty() ?
                System.getenv("PRESENCE_MAN_CLIENT_ID") : config.getString("client_id", client_id);

        server = System.getenv("PRESENCE_MAN_SERVER") != null && !System.getenv("PRESENCE_MAN_SERVER").isEmpty() ?
                System.getenv("PRESENCE_MAN_SERVER") : config.getString("server", server);

        enable_default = System.getenv("PRESENCE_MAN_DEFAULT_ENABLED") != null && !System.getenv("PRESENCE_MAN_DEFAULT_ENABLED").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_ENABLED").equalsIgnoreCase("true") :
                config.getBoolean("enable_default", enable_default);

        String DEFAULT_STATE = System.getenv("PRESENCE_MAN_DEFAULT_STATE") != null && !System.getenv("PRESENCE_MAN_DEFAULT_STATE").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_STATE") : config.getString("default_state", null);

        String DEFAULT_DETAILS = System.getenv("PRESENCE_MAN_DEFAULT_DETAILS") != null && !System.getenv("PRESENCE_MAN_DEFAULT_DETAILS").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_DETAILS") : config.getString("default_details", null);

        String DEFAULT_LARGE_IMAGE_KEY = System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_KEY") != null && !System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_KEY").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_KEY") : config.getString("default_large_image_key", null);

        String DEFAULT_LARGE_IMAGE_TEXT = System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_TEXT") != null && !System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_TEXT").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_TEXT") : config.getString("default_large_image_text", null);

        String DEFAULT_SMALL_IMAGE_KEY = System.getenv("PRESENCE_MAN_DEFAULT_SMALL_IMAGE_KEY") != null && !System.getenv("PRESENCE_MAN_DEFAULT_SMALL_IMAGE_KEY").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_SMALL_IMAGE_KEY") : config.getString("default_small_image_key", null);

        String DEFAULT_SMALL_IMAGE_TEXT = System.getenv("PRESENCE_MAN_DEFAULT_SMALL_IMAGE_TEXT") != null && !System.getenv("PRESENCE_MAN_DEFAULT_SMALL_IMAGE_TEXT").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_SMALL_IMAGE_TEXT") : config.getString("default_small_image_text", null);

        default_activity = new ApiActivity(
                ActivityType.PLAYING,
                DEFAULT_STATE,
                DEFAULT_DETAILS,
                null,
                DEFAULT_LARGE_IMAGE_KEY,
                DEFAULT_LARGE_IMAGE_TEXT,
                DEFAULT_SMALL_IMAGE_KEY,
                DEFAULT_SMALL_IMAGE_TEXT
        );
    }

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(new EventListener(), this);
        Server.getInstance().getScheduler().scheduleAsyncTask(this, new FetchGatewayInformationTask());
    }

    @Override
    public void onDisable() {
    }

    public static void setActivity(Player player, ApiActivity activity) {
        ApiRequest request = new ApiRequest(ApiRequest.URI_UPDATE_PRESENCE, Map.of(
                "ip", player.getAddress(),
                "xuid", player.getLoginChainData().getXUID(),
                "server", PresenceMan.server,
                "api_activity", activity
        ), true);

        request.header("Token", PresenceMan.token);

        PresenceMan.getInstance().getServer().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new BackendRequest(
                request.serialize(),
                response -> {
                    if (response.containsKey("code") && response.get("code").equals(200.0)) {
                        PresenceMan.presences.put(player.getLoginChainData().getXUID(), activity);
                    } else {
                        PresenceMan.getInstance().getLogger().error("Failed to update presence for " + player.getName() + ": " + response.getOrDefault("message", "n/a"));
                    }
                },
                error -> {
                    Server.getInstance().getLogger().error(error.toString());
                },
                10
        ));
    }

    public static void offline(Player player) {
        ApiRequest request = new ApiRequest(ApiRequest.URI_OFFLINE, Map.of(
                "ip", player.getAddress(),
                "xuid", player.getLoginChainData().getXUID()
        ), true);

        request.header("Token", PresenceMan.token);

        PresenceMan.getInstance().getServer().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new BackendRequest(
                request.serialize(),
                response -> {
                    if (response.containsKey("code") && response.get("code").equals(200)) {
                        PresenceMan.presences.remove(player.getLoginChainData().getXUID());
                    }
                },
                error -> {
                    Server.getInstance().getLogger().error(error.toString());
                },
                10
        ));
    }

    public static PresenceMan getInstance() {
        return instance;
    }
}
