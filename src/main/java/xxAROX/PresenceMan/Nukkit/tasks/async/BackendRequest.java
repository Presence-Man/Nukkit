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

package xxAROX.PresenceMan.Nukkit.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.JsonObject;
import okhttp3.*;
import xxAROX.PresenceMan.Nukkit.PresenceMan;
import xxAROX.PresenceMan.Nukkit.entity.ApiRequest;
import xxAROX.PresenceMan.Nukkit.entity.Gateway;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class BackendRequest extends AsyncTask {
    private final String request;
    private final Consumer<JsonObject> onResponse;
    private final Consumer<JsonObject> onError;
    private final int timeout;
    private final String url;

    public BackendRequest(String request, Consumer<JsonObject> onResponse, Consumer<JsonObject> onError, int timeout) throws RuntimeException{
        this.url = Gateway.getUrl();
        this.request = request;
        this.onResponse = onResponse;
        this.onError = onError;
        this.timeout = timeout;
    }

    @Override
    public void onRun() {
        ApiRequest apiRequest = ApiRequest.deserialize(request);
        Map<String, String> headers = apiRequest.getHeaders();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = null;

        if (apiRequest.isPostMethod()) requestBody = RequestBody.create(apiRequest.getBody().toString(), mediaType);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url + apiRequest.getUri())
                .method(apiRequest.isPostMethod() ? "POST" : "GET", requestBody);

        headers.forEach(requestBuilder::header);

        try {
            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body() != null ? Objects.requireNonNull(response.body()).string() : null;
            int responseCode = response.code();
            response.close();

            JsonObject json = new JsonObject();
            json.addProperty("body", responseBody);
            json.addProperty("status", responseCode);
            setResult(json.toString());
        } catch (IOException e) {
            PresenceMan.getInstance().getLogger().error("" + e);
            setResult(null);
        }
    }

    @Override
    public void onCompletion(Server server) {
        ApiRequest request = ApiRequest.deserialize(this.request);
        JsonObject results = PresenceMan.GSON.fromJson((String) getResult(), JsonObject.class);

        if (results != null) {
            int code = results.get("status").getAsInt();
            JsonObject body = PresenceMan.GSON.fromJson(results.get("body").getAsString(), JsonObject.class);

            if (code >= 100 && code <= 399) { // Good
                if (onResponse != null) onResponse.accept(body);
            } else if (code >= 400 && code <= 499) { // Client-Errors
                PresenceMan.getInstance().getLogger().error("[CLIENT-ERROR] [" + request.getUri() + "]: " + body.toString());
                if (onError != null) onError.accept(body);
            } else if (code >= 500 && code <= 599) { // Server-Errors
                if (!body.toString().contains("<html>")) PresenceMan.getInstance().getLogger().error("[API-ERROR] [" + request.getUri() + "]: " + body.toString());
                if (onError != null) onError.accept(body);
            }
        }
    }
}
