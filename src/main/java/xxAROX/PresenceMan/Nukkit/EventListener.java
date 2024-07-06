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

package xxAROX.PresenceMan.Nukkit;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import xxAROX.PresenceMan.Nukkit.utils.Utils;

public final class EventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerLoginEvent(PlayerLoginEvent event){
        Utils.savePlayerData(event.getPlayer());
        if (Utils.isFromSameHost(Utils.retrievePlayerData_ip(event.getPlayer()))) return;
        if (!PresenceMan.enable_default) return;
        if (event.getPlayer().getSkin() != null && PresenceMan.update_skin) PresenceMan.save_skin(event.getPlayer(), event.getPlayer().getSkin());
        PresenceMan.setActivity(event.getPlayer(), PresenceMan.default_activity);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerChangeSkinEvent(PlayerChangeSkinEvent event){
        if (Utils.isFromSameHost(Utils.retrievePlayerData_ip(event.getPlayer()))) return;
        if (!event.isCancelled()) PresenceMan.save_skin(event.getPlayer(), event.getSkin());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerQuitEvent(PlayerQuitEvent event){
        if (Utils.isFromSameHost(Utils.retrievePlayerData_ip(event.getPlayer()))) return;
        PresenceMan.presences.remove(event.getPlayer().getLoginChainData().getXUID());
        PresenceMan.offline(event.getPlayer());
    }
}
