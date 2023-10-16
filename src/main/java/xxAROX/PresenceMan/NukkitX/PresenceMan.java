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
import xxAROX.PresenceMan.NukkitX.utils.Utils;

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
        saveResource("README.md");
        saveResource("config.yml");

        Config config = this.getConfig();
        token = (String) Utils.getconfigvalue(config, "token");
        client_id = (String) Utils.getconfigvalue(config, "client_id", "", client_id);
        server = (String) Utils.getconfigvalue(config, "server", "", server);
        update_skin = (Boolean) Utils.getconfigvalue(config, "update_skin", "", update_skin);

        enable_default = (Boolean) Utils.getconfigvalue(config, "default_presence.enabled", "DEFAULT_ENABLED", enable_default);
        String DEFAULT_STATE = (String) Utils.getconfigvalue(config, "default_presence.state", "DEFAULT_STATE", "Playing {server} on {network}");
        String DEFAULT_DETAILS = (String) Utils.getconfigvalue(config, "default_presence.details", "DEFAULT_DETAILS", "");
        String DEFAULT_LARGE_IMAGE_KEY = (String) Utils.getconfigvalue(config, "default_presence.large_image_key", "DEFAULT_LARGE_IMAGE_KEY", "");
        String DEFAULT_LARGE_IMAGE_TEXT = (String) Utils.getconfigvalue(config, "default_presence.large_image_text", "DEFAULT_LARGE_IMAGE_TEXT", "{App.name} - v{App.version}");

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
