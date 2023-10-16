package xxAROX.PresenceMan.NukkitX;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xxAROX.PresenceMan.NukkitX.entity.ActivityType;
import xxAROX.PresenceMan.NukkitX.entity.ApiActivity;
import xxAROX.PresenceMan.NukkitX.entity.ApiRequest;
import xxAROX.PresenceMan.NukkitX.entity.Gateway;
import xxAROX.PresenceMan.NukkitX.tasks.UpdateCheckerTask;
import xxAROX.PresenceMan.NukkitX.tasks.async.BackendRequest;
import xxAROX.PresenceMan.NukkitX.tasks.async.FetchGatewayInformationTask;
import xxAROX.PresenceMan.NukkitX.utils.SkinUtils;

import java.util.HashMap;
import java.util.Map;

public class PresenceMan extends PluginBase {
    public static final Gson GSON = new Gson();
    private static PresenceMan instance;

    private static String token = "undefined";
    public static String client_id = null;
    public static String server = "undefined";
    public static Boolean enable_default = false;
    public static Boolean update_skin = false;

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

        update_skin = System.getenv("PRESENCE_MAN_UPDATE_SKIN") != null && !System.getenv("PRESENCE_MAN_UPDATE_SKIN").isEmpty() ?
                System.getenv("PRESENCE_MAN_UPDATE_SKIN").equalsIgnoreCase("true") :
                config.getBoolean("enable_default", enable_default);

        String DEFAULT_STATE = System.getenv("PRESENCE_MAN_DEFAULT_STATE") != null && !System.getenv("PRESENCE_MAN_DEFAULT_STATE").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_STATE") : config.getString("default_state", null);

        String DEFAULT_DETAILS = System.getenv("PRESENCE_MAN_DEFAULT_DETAILS") != null && !System.getenv("PRESENCE_MAN_DEFAULT_DETAILS").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_DETAILS") : config.getString("default_details", null);

        String DEFAULT_LARGE_IMAGE_KEY = System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_KEY") != null && !System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_KEY").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_KEY") : config.getString("default_large_image_key", "bedrock");

        String DEFAULT_LARGE_IMAGE_TEXT = System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_TEXT") != null && !System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_TEXT").isEmpty() ?
                System.getenv("PRESENCE_MAN_DEFAULT_LARGE_IMAGE_TEXT") : config.getString("default_large_image_text", "Minecraft: Bedrock Edition");

        default_activity = new ApiActivity(
                ActivityType.PLAYING,
                DEFAULT_STATE,
                DEFAULT_DETAILS,
                null,
                DEFAULT_LARGE_IMAGE_KEY,
                DEFAULT_LARGE_IMAGE_TEXT
        );
    }

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(new EventListener(), this);
        Server.getInstance().getScheduler().scheduleRepeatingTask(this, new UpdateCheckerTask(), 20 *60 *60); // NOTE: 60 minutes
        Server.getInstance().getScheduler().scheduleAsyncTask(this, new FetchGatewayInformationTask());
    }

    @Override
    public void onDisable() {
        for (Player player : Server.getInstance().getOnlinePlayers().values()) offline(player);
    }

    public static void setActivity(Player player, ApiActivity activity) {
        if (!Server.getInstance().isRunning()) return;
        if (player.getLoginChainData().getXUID().isEmpty()) return;

        JsonObject body = new JsonObject();
        Map.of(
                "ip", player.getAddress(),
                "xuid", player.getLoginChainData().getXUID(),
                "server", PresenceMan.server
        ).forEach(body::addProperty);
        body.add("api_activity", activity.serialize());

        ApiRequest request = new ApiRequest(
                ApiRequest.URI_UPDATE_PRESENCE,
                body,
                true
        );
        request.header("Token", token);

        PresenceMan.getInstance().getServer().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new BackendRequest(
                request.serialize(),
                response -> {
                    if (response.has("status") && response.get("status").getAsInt() == 200) PresenceMan.presences.put(player.getLoginChainData().getXUID(), activity);
                    else PresenceMan.getInstance().getLogger().error("Failed to update presence for " + player.getName() + ": " + response.get("message").getAsString());
                },
                error -> {},
                10
        ));
    }

    public static String getHeadUrl(String xuid){
        return Gateway.getUrl() + "/api/v1/heads/" + xuid;
    }

    public static void offline(Player player) {
        JsonObject body = new JsonObject();
        Map.of(
                "ip", player.getAddress(),
                "xuid", player.getLoginChainData().getXUID()
        ).forEach(body::addProperty);

        ApiRequest request = new ApiRequest(ApiRequest.URI_OFFLINE, body, true);
        request.header("Token", token);

        BackendRequest task = new BackendRequest(
                request.serialize(),
                response -> PresenceMan.presences.remove(player.getLoginChainData().getXUID()),
                error -> {},
                10
        );
        if (!Server.getInstance().isRunning()) task.run();
        else Server.getInstance().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), task);
    }

    public static void save_head(Player player, Skin skin) {
        if (!Server.getInstance().isRunning()) return;
        if (!player.isConnected()) return;
        if (player.getLoginChainData().getXUID().isEmpty()) return;

        String head = SkinUtils.getFrontFace(player, skin);

        if (head != null) {
            JsonObject body = new JsonObject();
            Map.of(
                    "ip", player.getAddress(),
                    "xuid", player.getLoginChainData().getXUID(),
                    "head", head
            ).forEach(body::addProperty);
            ApiRequest request = new ApiRequest(ApiRequest.URI_UPDATE_HEAD, body, true);
            request.header("Token", token);

            BackendRequest task = new BackendRequest(request.serialize(), response -> {}, error -> {}, 10);
            if (!Server.getInstance().isRunning()) task.run();
            else Server.getInstance().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), task);
        }
    }

    public static PresenceMan getInstance() {
        return instance;
    }
}
