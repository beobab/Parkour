package net.toolan.plugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jonathan on 01/04/2017.
 * Listens for teleport events to disqualify racers.
 * Listens for checkpoint events for racers.
 */
public class ParkourListener implements Listener {
    private final Parkour plugin;

    ParkourListener(Parkour plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        plugin.getLogger().info(event.getCause().name());
        //new TeleportEvent(plugin, event.getPlayer(), event).raise();
    }

    @EventHandler
    public void onButtonClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player == null) return;

        Block b = e.getClickedBlock();
        if (b == null) return;

        Action a = e.getAction();
        if (a == null) return;

        // Must switch a lever/button or step on a pressure plate.
        if (a != Action.RIGHT_CLICK_BLOCK && a != Action.PHYSICAL) return;

        Material clicker = b.getType();
        if (clicker == null) return;

        boolean isCorrectMaterial = (
// Single click items act like toggles.
               clicker == Material.STONE_BUTTON
            || clicker == Material.WOOD_BUTTON
            || clicker == Material.STONE_PLATE
            || clicker == Material.WOOD_PLATE
// --  Need work to work out how pressure plates will work.
            || clicker == Material.IRON_PLATE
            || clicker == Material.GOLD_PLATE

// The lever is a real toggle. Should probably get it's state and open/close specifically.
            || clicker == Material.LEVER
        );
        // Short circuit out asap if it's not interesting.
        if (!isCorrectMaterial) return;



        UUID playerKey = player.getUniqueId();

        RaceWaypoint wayPoint = new RaceWaypoint(b.getWorld().getName(), b.getLocation());
        String wayPointKey = wayPoint.WayPointKey();

        RaceManager manager = plugin.getRaceManager();

        if (manager.isSettingUpRace(playerKey)) {
            // They went over another checkpoint.
            manager.addWaypoint(playerKey, wayPointKey);
            return;
        }

        if (manager.isActiveRaceWaypoint(playerKey, wayPointKey)) {
            RaceEntrant entrant = manager.getEntrant(playerKey);
            if (entrant.hitWayPoint(wayPoint)) {
                if (entrant.hasWon()) {
                    player.sendMessage("You finished the race!");
                } else {
                    player.sendMessage("Achieved waypoint " + Integer.toString(entrant.currentWaypoint));
                }
            }
            return;
        }

        if (manager.isRaceStartLocation(wayPointKey)) {
            manager.startRace(playerKey, wayPointKey);
            player.sendMessage("You started the race!");
        }
    }




}
