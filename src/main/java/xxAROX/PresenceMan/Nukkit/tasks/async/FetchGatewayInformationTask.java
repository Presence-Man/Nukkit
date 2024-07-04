/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.Nukkit.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import xxAROX.PresenceMan.Nukkit.PresenceMan;
import xxAROX.PresenceMan.Nukkit.entity.Gateway;
import xxAROX.PresenceMan.Nukkit.tasks.ReconnectingTask;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public final class FetchGatewayInformationTask extends AsyncTask {
    public static final String URL = "https://raw.githubusercontent.com/Presence-Man/Gateway/main/gateway.json";

    @Override
    public void onRun() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL)
                .header("Cache-Control", "no-cache, no-store")
                .build()
        ;

        client.newCall(request).enqueue(new Callback() {
            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    if (response.code() != 200) PresenceMan.getInstance().getLogger().warning("Couldn't fetch gateway data");
                    else {
                        JsonObject json = null;
                        try {json = PresenceMan.GSON.fromJson(Objects.requireNonNull(response.body()).string(), JsonObject.class);} catch (IOException ignore) {}
                        if (json != null) {
                            Gateway.protocol = json.get("protocol").getAsString();
                            Gateway.address = json.get("address").getAsString();
                            Gateway.port = json.has("port") && !json.get("port").isJsonNull() ? json.get("port").getAsInt() : null;
                            ping_backend(success -> {
                                if (!success) PresenceMan.getInstance().getLogger().error("Error while connecting to backend-server!");
                            });
                        }
                    }
                } catch (JsonParseException e) {
                    PresenceMan.getInstance().getLogger().error("Error while fetching gateway information: " + e.getMessage());
                }
            }
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                PresenceMan.getInstance().getLogger().critical("Presence-Man backend-gateway config is not reachable, disabling..");
                PresenceMan.getInstance().setEnabled(false);
            }
        });
    }

    public static void ping_backend(Consumer<Boolean> callback) {
        if (ReconnectingTask.active) return;

        PresenceMan.getInstance().getServer().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(Gateway.getUrl()).build();
                    Response response = client.newCall(request).execute();
                    setResult(response.code());
                    response.close();
                } catch (IOException e) {
                    setResult(404);
                }
            }

            @Override
            public void onCompletion(Server server) {
                int code = (int) getResult();
                if (code != 200) {
                    Gateway.broken = true;
                    ReconnectingTask.activate();
                } else {
                    ReconnectingTask.deactivate();
                    PresenceMan.getInstance().getLogger().notice("This server will be displayed as '" + PresenceMan.server + "' in presences!");
                }
                callback.accept(code == 200);
            }
        });
    }

    public static void unga_bunga() {
        Server.getInstance().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new FetchGatewayInformationTask());
    }
}
