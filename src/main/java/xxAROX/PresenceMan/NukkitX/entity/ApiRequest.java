/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.NukkitX.entity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class ApiRequest {
    private final Map<String, String> headers = new HashMap<String, String>(){{
        put("Content-Type", "application/json");
    }};
    private JsonObject body;
    private boolean postMethod;
    private String uri;

    public static final String URI_UPDATE_PRESENCE = "/api/v1/servers/update_presence";
    public static final String URI_OFFLINE = "/api/v1/servers/offline";
    public static final String URI_UPDATE_SKIN = "/api/v1/images/skins/update";

    public ApiRequest(String uri, JsonObject body, boolean postMethod) {
        this.uri = uri;
        this.body = body;
        this.postMethod = postMethod;
    }

    public String serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("uri", uri);
        JsonObject headers = new JsonObject();
        this.headers.forEach(headers::addProperty);
        json.add("headers", headers);
        json.add("body", body);
        json.addProperty("post_method", postMethod);
        return json.toString();
    }

    public static ApiRequest deserialize(String str) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(str, JsonObject.class);
        ApiRequest self = new ApiRequest(json.get("uri").getAsString(), json.get("body").getAsJsonObject(), json.get("post_method").getAsBoolean());
        json.get("body").getAsJsonObject().asMap().forEach((key, value) -> self.headers.put(key, value.getAsString()));
        return self;
    }

    public String getUri() {
        return uri;
    }

    public ApiRequest header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public ApiRequest body(String key, String value) {
        this.body.addProperty(key, value);
        return this;
    }

    public boolean isPostMethod() {
        return this.postMethod;
    }
}