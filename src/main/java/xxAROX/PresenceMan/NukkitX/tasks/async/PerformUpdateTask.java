/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.NukkitX.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xxAROX.PresenceMan.NukkitX.PresenceMan;
import xxAROX.PresenceMan.NukkitX.entity.Gateway;
import xxAROX.PresenceMan.NukkitX.tasks.UpdateCheckerTask;
import xxAROX.PresenceMan.NukkitX.utils.Utils;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PerformUpdateTask extends AsyncTask {
    private static final String LATEST_VERSION_URL = "https://raw.githubusercontent.com/Presence-Man/Nukkit/main/version-nukkit.txt";

    private final String currentVersion;
    private boolean notified = false;

    public PerformUpdateTask() {
        currentVersion = PresenceMan.getInstance().getDescription().getVersion();
    }

    @Override
    public void onRun() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        ;
        Request.Builder requestBuilder = new Request.Builder()
                .url(LATEST_VERSION_URL)
                .get()
        ;
        try {
            Response response = client.newCall(requestBuilder.build()).execute();
            String responseBody = response.body() != null ? Objects.requireNonNull(response.body()).string() : null;
            int responseCode = response.code();
            if (responseCode != 200 || responseBody == null || responseBody.isEmpty()) setResult(null);
            else {
                Pattern versionPattern = Pattern.compile("\\d+(\\.\\d+)*");
                Matcher matcher = versionPattern.matcher(responseBody);

                if (matcher.find()) {
                    String latestVersionString = matcher.group();
                    Utils.VersionComparison.Version latest = Utils.VersionComparison.parse(latestVersionString);
                    Utils.VersionComparison.Version current = Utils.VersionComparison.parse(currentVersion);

                    setResult(latest.compareTo(current) < 0 ? latestVersionString : null);
                } else setResult(null);
            }
            response.close();
        } catch (IOException e) {
            PresenceMan.getInstance().getLogger().error("" + e);
            setResult(null);
        }
    }

    @Override
    public void onCompletion(Server server) {
        String latest = (String) getResult();
        if (latest != null) {
            if (!notified) {
                PresenceMan.getInstance().getLogger().warning("Your version of Presence-Man is out of date. To avoid issues please update it to the latest version!");
                PresenceMan.getInstance().getLogger().warning("Download: " + Gateway.getUrl() + "/downloads/nukkit");
                notified = true;
            }
        }
        UpdateCheckerTask.running = false;
    }
}
