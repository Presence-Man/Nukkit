/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.Nukkit.entity;

import cn.nukkit.Server;
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
    public static final String URI_UPDATE_SKIN = "/api/v1/images/skins/update";
    public static final String URI_UPDATE_OFFLINE = "/api/v1/servers/offline";
    public static final String URI_GET_SKIN = "/api/v1/images/skins/";
    public static final String URI_GET_HEAD = "/api/v1/images/heads/";

    public ApiRequest(String uri, JsonObject body, boolean postMethod) {
        this.uri = uri;
        this.body = body;
        this.postMethod = postMethod;
        header("Serversoftware", Server.getInstance().getName());
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
        json.get("headers").getAsJsonObject().asMap().forEach((key, value) -> self.headers.put(key, value.getAsString()));
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