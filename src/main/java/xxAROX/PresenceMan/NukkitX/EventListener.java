package xxAROX.PresenceMan.NukkitX;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;

public final class EventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerLoginEvent(PlayerLoginEvent event){
        if (!PresenceMan.enable_default) return;
        if (event.getPlayer().getSkin() != null && PresenceMan.update_skin) PresenceMan.save_skin(event.getPlayer(), event.getPlayer().getSkin());
        PresenceMan.setActivity(event.getPlayer(), PresenceMan.default_activity);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerChangeSkinEvent(PlayerChangeSkinEvent event){
        if (!event.isCancelled()) PresenceMan.save_skin(event.getPlayer(), event.getSkin());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerQuitEvent(PlayerQuitEvent event){
        PresenceMan.presences.remove(event.getPlayer().getLoginChainData().getXUID());
        PresenceMan.offline(event.getPlayer());
    }
}
