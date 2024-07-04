/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
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
import java.net.InetAddress;
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

        try {
            if (!InetAddress.getByName("google.com").isReachable(5000)) {
                setResult(null);
                return;
            }
        } catch (IOException e) {
            setResult(null);
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = null;

        if (apiRequest.isPostMethod()) requestBody = RequestBody.create(PresenceMan.GSON.toJson(apiRequest.getBody()), mediaType);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url + apiRequest.getUri())
                .method(apiRequest.isPostMethod() ? "POST" : "GET", requestBody);

        for (Map.Entry<String, String> entry : headers.entrySet()) requestBuilder.header(entry.getKey(), entry.getValue());

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
            JsonObject body = results.get("body").getAsJsonObject();

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
