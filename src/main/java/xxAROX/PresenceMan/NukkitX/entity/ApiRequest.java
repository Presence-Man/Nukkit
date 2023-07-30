package xxAROX.PresenceMan.NukkitX.entity;

import cn.nukkit.Server;
import com.google.gson.Gson;
import xxAROX.PresenceMan.NukkitX.PresenceMan;

import java.util.HashMap;
import java.util.Map;

public class ApiRequest {
    private Map<String, String> headers;
    private Map<String, Object> body;
    private boolean postMethod;
    private String uri;

    public static final String URI_UPDATE_PRESENCE = "/api/v1/servers/update_presence";
    public static final String URI_UPDATE_HEAD = "/api/v1/heads/update";
    public static final String URI_OFFLINE = "/api/v1/servers/offline";

    public ApiRequest(String uri, Map<String, Object> body, boolean postMethod) {
        this.uri = uri;
        this.body = body;
        this.postMethod = postMethod;
        this.headers = new HashMap<>();
        this.headers.put("Content-Type", "application/json");

        PresenceMan.getInstance().applyToken(this);
        this.header("Serversoftware", Server.getInstance().getCodename());
    }

    public String serialize() {
        Map<String, Object> arr = new HashMap<>();
        arr.put("uri", this.uri);
        arr.put("headers", this.headers);
        arr.put("body", this.body);
        arr.put("post_method", this.postMethod);
        return new Gson().toJson(arr);
    }

    public static ApiRequest deserialize(String str) {
        Gson gson = new Gson();
        Map<String, Object> json = gson.fromJson(str, Map.class);
        ApiRequest self = new ApiRequest(
                (String) json.get("uri"),
                (Map<String, Object>) json.getOrDefault("body", new HashMap<>()),
                (boolean) json.getOrDefault("post_method", false)
        );
        self.headers = (Map<String, String>) json.getOrDefault("headers", new HashMap<>());
        return self;
    }

    public String getUri() {
        return this.uri;
    }

    public ApiRequest header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public ApiRequest body(String key, String value) {
        this.body.put(key, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public Map<String, Object> getBody() {
        return this.body;
    }

    public boolean isPostMethod() {
        return this.postMethod;
    }
}