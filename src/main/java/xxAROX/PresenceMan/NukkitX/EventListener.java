package xxAROX.PresenceMan.NukkitX;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    @EventHandler
    public void PlayerLoginEvent(PlayerLoginEvent event){
        if (!PresenceMan.enable_default) return;
        if (event.getPlayer().getSkin() != null && PresenceMan.update_skin) PresenceMan.save_head(event.getPlayer(), event.getPlayer().getSkin());
        PresenceMan.setActivity(event.getPlayer(), PresenceMan.default_activity);
    }

    @EventHandler
    public void PlayerChangeSkinEvent(PlayerChangeSkinEvent event){
        if (!event.isCancelled()) PresenceMan.save_head(event.getPlayer(), event.getSkin());
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent event){
        PresenceMan.presences.remove(event.getPlayer().getLoginChainData().getXUID());
        PresenceMan.offline(event.getPlayer());
    }
}
