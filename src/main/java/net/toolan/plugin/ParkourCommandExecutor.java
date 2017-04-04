package net.toolan.plugin;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by jonathan on 01/04/2017.
 * Race command recognition.
 */
public class ParkourCommandExecutor implements CommandExecutor {
    private final Parkour plugin;
    ParkourCommandExecutor(Parkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("race")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
                return true;
            }

            Player player = (Player) sender;
            UUID playerKey = player.getUniqueId();

            RaceManager manager = plugin.getRaceManager();

            // Player just typed "/race"
            if (args.length == 0) {
                if (manager.isPlayerRacing(playerKey)) {
                    RaceEntrant entrant = manager.getEntrant(playerKey);
                    sender.sendMessage("Racing for " + entrant.RaceTime() + " seconds, " + entrant.WayPointTime() + " seconds since last waypoint");

                } else {
                    sender.sendMessage("You are not currently racing.");
                    RaceWaypoint tempWaypoint = new RaceWaypoint(player.getWorld().getName(), player.getLocation());
                    Race race = manager.NearestRace(tempWaypoint);
                    if (race == null) {
                        sender.sendMessage("There are no races set up on this world.");
                    } else {
                        sender.sendMessage("Nearest race is " + race.name + ". Setting compass to nearest race start.");
                        Location nextWayPoint = race.getStart().getLocation();
                        nextWayPoint.setWorld(player.getWorld());
                        player.setCompassTarget(nextWayPoint);
                    }
                }


            } else if (args[0].equalsIgnoreCase("start")) {
                if (manager.isSettingUpRace(playerKey)) {
                    sender.sendMessage("You are already setting up a race. Finish that one first.");
                } else {
                    String name = ArgAt(args, 1, "");
                    manager.setupRace(playerKey, name);
                    sender.sendMessage("Activate levers, pressure plates and buttons to mark waypoints.");
                    sender.sendMessage("Type $6/race finish$F after you have activated the last one.");
                }

            } else if (args[0].equalsIgnoreCase("end")) {
                Race race = manager.endSetupRace(playerKey);
                if (race.WayPointCount() > 1) {
                    sender.sendMessage("Race " + race.name + " created with " + race.WayPointCount() + " waypoints.");
                } else {
                    sender.sendMessage("Race " + race.name + " is not a valid race. You will need to create more waypoints.");
                }

            } else if (args[0].equalsIgnoreCase("delete")) {
                String name = ArgAt(args, 1, "");
                Race race = manager.deleteRace(name);
                if (race == null) {
                    sender.sendMessage("Could not find that race.");
                } else {
                    sender.sendMessage("Deleted race " + race.name + ".");
                }
            }

            return true;
        }
        return false;
    }

    String ArgAt(String[] args, int index, String defaultValue) {
        if (index >= args.length) return defaultValue;
        return args[index];
    }
}
