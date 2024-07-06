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

package xxAROX.PresenceMan.Nukkit.tasks;

import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import xxAROX.PresenceMan.Nukkit.PresenceMan;
import xxAROX.PresenceMan.Nukkit.tasks.async.PerformUpdateTask;

public final class UpdateCheckerTask extends Task {
    public static boolean running = false;

    @Override
    public void onRun(int i) {
        if (running) return;
        running = true;
        Server.getInstance().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new PerformUpdateTask());
    }
}
