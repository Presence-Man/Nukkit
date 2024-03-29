/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.NukkitX.tasks;

import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import xxAROX.PresenceMan.NukkitX.PresenceMan;
import xxAROX.PresenceMan.NukkitX.tasks.async.PerformUpdateTask;

public final class UpdateCheckerTask extends Task {
    public static boolean running = false;

    @Override
    public void onRun(int i) {
        if (running) return;
        running = true;
        Server.getInstance().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new PerformUpdateTask());
    }
}
