package xxAROX.PresenceMan.NukkitX.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import xxAROX.PresenceMan.NukkitX.PresenceMan;
import xxAROX.PresenceMan.NukkitX.entity.Gateway;
import xxAROX.PresenceMan.NukkitX.tasks.ReconnectingTask;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class FetchGatewayInformationTask extends AsyncTask {
    public static final String URL = "https://raw.githubusercontent.com/Presence-Man/releases/main/gateway.json";

    @Override
    public void onRun() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL)
                .header("Cache-Control", "no-cache, no-store")
                .build()
        ;

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() == 200) {
                    try {
                        String responseBody = Objects.requireNonNull(response.body()).string();
                        JsonObject result = PresenceMan.GSON.fromJson(responseBody, JsonObject.class);
                        if (result != null && !result.isEmpty()) {
                            Integer port = result.has("port") && !result.get("port").isJsonNull() ? result.get("port").getAsInt() : null;
                            Gateway.protocol = result.get("protocol").getAsString();
                            Gateway.address = result.get("address").getAsString();
                            Gateway.port = port;
                        }

                        ping_backend(success -> {
                            if (!success) PresenceMan.getInstance().getLogger().error("Error while connecting to backend-server!");
                        });
                    } catch (Exception e) {
                        PresenceMan.getInstance().getLogger().error("Error while parsing gateway information: " + e.getMessage());
                    }
                } else {
                    PresenceMan.getInstance().getLogger().critical("Presence-Man backend-gateway config is not reachable, disabling..");
                    PresenceMan.getInstance().getServer().getPluginManager().disablePlugin(PresenceMan.getInstance());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                PresenceMan.getInstance().getLogger().critical("Presence-Man backend-gateway config is not reachable, disabling..");
                PresenceMan.getInstance().getServer().getPluginManager().disablePlugin(PresenceMan.getInstance());
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
                    setResult(response.isSuccessful());
                    response.close();
                } catch (IOException e) {
                    setResult(false);
                }
            }

            @Override
            public void onCompletion(Server server) {
                boolean success = (boolean) getResult();
                if (!success) {
                    Gateway.broken = true;
                    ReconnectingTask.activate();
                } else {
                    ReconnectingTask.deactivate();
                    PresenceMan.getInstance().getLogger().notice("This server will be displayed as '" + PresenceMan.server + "' in presences!");
                }
                callback.accept(success);
            }
        });
    }
}
