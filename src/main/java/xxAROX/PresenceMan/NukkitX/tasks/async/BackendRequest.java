package xxAROX.PresenceMan.NukkitX.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xxAROX.PresenceMan.NukkitX.PresenceMan;
import xxAROX.PresenceMan.NukkitX.entity.ApiRequest;
import xxAROX.PresenceMan.NukkitX.entity.Gateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BackendRequest extends AsyncTask {
    private final String request;
    private final Callback onResponse;
    private final Callback onError;
    private final int timeout;
    private final String url;

    public BackendRequest(String request, Callback onResponse, Callback onError, int timeout) {
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

        if (apiRequest.isPostMethod()) {
            String requestBodyJson = GSON.toJson(apiRequest.getBody());
            requestBody = RequestBody.create(requestBodyJson, mediaType);
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url + apiRequest.getUri())
                .method(apiRequest.isPostMethod() ? "POST" : "GET", requestBody);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }

        try {
            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body() != null ? response.body().string() : null;
            int responseCode = response.code();

            Map<String, Object> result = new HashMap<>();
            result.put("body", responseBody);
            result.put("code", responseCode);

            this.setResult(GSON.toJson(result));
            response.close();
        } catch (IOException e) {
            PresenceMan.getInstance().getLogger().error("" + e);
        }
    }

    @Override
    public void onCompletion(Server server) {
        Object result = this.getResult();
        if (result != null) {
            try {
                Map<String, Object> resultMap = GSON.fromJson((String) result, new TypeToken<Map<String, Object>>(){}.getType());
                Double originalCode = Double.valueOf(resultMap.get("code").toString());
                int code = originalCode.intValue();

                if (code >= 100 && code <= 399) {
                    if (onResponse != null) onResponse.onResponse(resultMap);
                } else if (code >= 400 && code <= 499) {
                    // Client error
                    PresenceMan.getInstance().getLogger().error("[CLIENT-ERROR] [" + url + "]: " + resultMap.get("body"));
                    if (onError != null) onError.onResponse(resultMap);
                } else if (code >= 500 && code <= 599) {
                    // Server error
                    PresenceMan.getInstance().getLogger().error("[API-ERROR] [" + url + "]: " + resultMap.get("body"));
                    if (onError != null) onError.onResponse(resultMap);
                }
            } catch (JsonSyntaxException e) {
                PresenceMan.getInstance().getLogger().error(url);
                PresenceMan.getInstance().getLogger().error("" + e);
            }
        } else {
            PresenceMan.getInstance().getLogger().error("[JUST-IN-CASE-ERROR] [" + url + "]: got null, that's not good");
        }
    }

    public interface Callback {
        void onResponse(Map<String, Object> response);
    }

    private static final Gson GSON = new Gson();
}