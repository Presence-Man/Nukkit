package xxAROX.PresenceMan.NukkitX.tasks;

import cn.nukkit.scheduler.Task;
import cn.nukkit.scheduler.TaskHandler;
import xxAROX.PresenceMan.NukkitX.PresenceMan;
import xxAROX.PresenceMan.NukkitX.tasks.async.FetchGatewayInformationTask;

public class ReconnectingTask extends Task {
    public static boolean active = false;
    private static TaskHandler task = null;

    public static void activate() {
        if (active) return;
        active = true;
        task = PresenceMan.getInstance().getServer().getScheduler().scheduleRepeatingTask(new ReconnectingTask(), 20 * 5);
    }

    public static void deactivate() {
        if (!active) return;
        task.cancel();
        task = null;
        active = false;
    }

    @Override
    public void onRun(int currentTick) {
        FetchGatewayInformationTask.ping_backend(success -> {
            if (success) {
                PresenceMan.getInstance().getLogger().debug("Reconnected!");
                ReconnectingTask.deactivate();
            }
        });
    }
}
