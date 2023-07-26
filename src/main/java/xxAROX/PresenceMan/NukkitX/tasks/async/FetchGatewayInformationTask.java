package xxAROX.PresenceMan.NukkitX.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import xxAROX.PresenceMan.NukkitX.PresenceMan;
import xxAROX.PresenceMan.NukkitX.entity.Gateway;
import xxAROX.PresenceMan.NukkitX.tasks.ReconnectingTask;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class FetchGatewayInformationTask extends AsyncTask {
    public static final String URL = "https://raw.githubusercontent.com/Presence-Man/releases/main/gateway.json";

    @Override
    public void onRun() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        String responseBody = response.body().string();
                        Map<String, Object> result = parseJsonResponse(responseBody);
                        if (result != null) {
                            Double port = Double.parseDouble(result.get("port").toString());
                            Gateway.protocol = (String) result.get("protocol");
                            Gateway.address = (String) result.get("address");
                            Gateway.port = port.intValue();
                        }

                        ping_backend(success -> {
                            if (!success) {
                                PresenceMan.getInstance().getLogger().error("Error while connecting to backend-server!");
                            } else {
                                PresenceMan.getInstance().getLogger().error("Succesfully connected to backend-server!");
                            }
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

    private Map<String, Object> parseJsonResponse(String responseBody) {
        Gson gson = new Gson();
        return gson.fromJson(responseBody, Map.class);
    }

    public static void ping_backend(Consumer<Boolean> callback) {
        if (ReconnectingTask.active) return;

        PresenceMan.getInstance().getServer().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new AsyncTask() {
            private Consumer<Boolean> callback = null;

            @Override
            public void onRun() {
                this.callback = new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {

                    }
                };
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(Gateway.getUrl()).build();
                    Response response = client.newCall(request).execute();

                    this.setResult(response.isSuccessful());
                    response.close();
                } catch (IOException e) {
                    this.setResult(false);
                }
            }

            @Override
            public void onCompletion(Server server) {
                boolean success = (boolean) this.getResult();
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
