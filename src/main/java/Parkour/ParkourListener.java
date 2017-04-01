package Parkour;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Created by jonathan on 01/04/2017.
 * Listens for teleport events to disqualify racers.
 * Listens for checkpoint events for racers.
 */
public class ParkourListener implements Listener {
    private final Parkour plugin;

    public ParkourListener(Parkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        plugin.getLogger().info(event.getCause().name());
        //new TeleportEvent(plugin, event.getPlayer(), event).raise();
    }
}
