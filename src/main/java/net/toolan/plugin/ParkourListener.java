package net.toolan.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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
    public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
        //plugin.getLogger().info(e.getCause().name());
        //new TeleportEvent(plugin, event.getPlayer(), event).raise();

        Player player = e.getPlayer();
        if (player == null) return;

        UUID playerKey = player.getUniqueId();
        RaceManager manager = plugin.getRaceManager();
        RaceEntrant entrant = manager.getEntrant(playerKey);

        if (entrant == null) return;

        if (entrant.canTeleportToNextWaypoint()) return;

        // Don't disqualify jumps back to last checkpoint.
        Location to = e.getTo();
        RaceWaypoint wayPoint = new RaceWaypoint(to.getWorld().getName(), to);
        if (entrant.isCurrentWayPoint(wayPoint)) return;

        player.sendMessage("Teleporting in a race is not allowed! You have been disqualified.");
        manager.endRace(playerKey);
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
            if (manager.isRaceStartLocation(wayPointKey)) {
                player.sendMessage(ChatColor.RED + "This is already a start location for another race.");
            } else {
                RaceWaypoint waypoint = manager.addWaypoint(playerKey, wayPointKey);
                if (waypoint != null) {
                    player.sendMessage("Added waypoint at " + wayPoint.DisplayWayPointKey());
                }
            }
            return;

        }

        if (manager.isPlayerRacing(playerKey)) {
            // Currently racing. Check against the active waypoints.
            if (manager.isActiveRaceWaypoint(playerKey, wayPointKey)) {
                RaceEntrant entrant = manager.getEntrant(playerKey);
                if (entrant.hitWayPoint(wayPoint)) {
                    if (entrant.hasWon()) {
                        manager.endRace(playerKey);
                        player.sendMessage("You finished the race in " + entrant.RaceTime() + " seconds.");
                    } else {
                        player.sendMessage("Waypoint " + ChatColor.AQUA + Integer.toString(entrant.currentWaypoint) + ChatColor.WHITE +
                                            " time: " + ChatColor.BLUE + entrant.WayPointTime() + ChatColor.WHITE + " seconds," +
                                            " Total: " + ChatColor.YELLOW + entrant.RaceTime() + ChatColor.WHITE + " seconds.");
                        if (!entrant.canTeleportToNextWaypoint()) {
                            Location nextWayPoint = entrant.nextWayPointLocation();
                            nextWayPoint.setWorld(player.getWorld());
                            player.setCompassTarget(nextWayPoint);
                        }
                    }
                }
            }
            return;
        }


        // Not currently racing, but might be about to step on a start waypoint.
        if (manager.isRaceStartLocation(wayPointKey)) {
            RaceEntrant entrant = manager.startRace(playerKey, wayPointKey);
            player.sendMessage("You started the race!");
            if (!entrant.canTeleportToNextWaypoint()) {
                Location nextWayPoint = entrant.nextWayPointLocation();
                nextWayPoint.setWorld(player.getWorld());
                player.setCompassTarget(nextWayPoint);
            }
        }

    }

}
