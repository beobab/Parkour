package net.toolan.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
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
            boolean handled = false;

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
                        sender.sendMessage("There are no races set up on this server.");
                        sender.sendMessage("Type " + ChatColor.BLUE + "/race start" + ChatColor.WHITE + " to set one up");
                    } else {
                        sender.sendMessage("Nearest race is " + race.name + ". Setting compass to nearest race start.");
                        Location nextWayPoint = race.getStart().getLocation();
                        player.setCompassTarget(nextWayPoint);
                    }
                }
                handled = true;

            } else if (args[0].equalsIgnoreCase("back")) {
                if (manager.isPlayerRacing(playerKey)) {
                    RaceEntrant entrant = manager.getEntrant(playerKey);
                    Location current = player.getLocation();
                    float pitch = current.getPitch();
                    float yaw = current.getYaw();
                    Location lastWaypoint =entrant.currentWayPointLocation();
                    lastWaypoint.setPitch(pitch);
                    lastWaypoint.setYaw(yaw);
                    player.teleport(lastWaypoint);
                    sender.sendMessage("Returned to last waypoint.");
                } else {
                    sender.sendMessage("You are not in a race. There's no waypoint to go back to.");
                }
                handled = true;

            } else if (args[0].equalsIgnoreCase("start")) {
                if (manager.isSettingUpRace(playerKey)) {
                    sender.sendMessage("You are already setting up a race. Finish that one first.");
                } else {
                    String name = ArgAt(args, 1, "");
                    manager.setupRace(playerKey, name);
                    sender.sendMessage("Activate levers, pressure plates and buttons to mark waypoints.");
                    sender.sendMessage("Type " + ChatColor.BLUE + "/race end" + ChatColor.WHITE + " after you have activated the last one.");
                }
                handled = true;

            } else if (args[0].equalsIgnoreCase("end")) {
                if (manager.isSettingUpRace(playerKey)) {
                    Race race = manager.endSetupRace(playerKey);
                    if (race.WayPointCount() > 1) {
                        sender.sendMessage("Race " + race.name + " created with " + race.WayPointCount() + " waypoints.");
                    } else {
                        sender.sendMessage("Race " + race.name + " is not a valid race. You will need to create more waypoints.");
                    }
                } else {
                    sender.sendMessage("You are not setting up a race.");
                }
                handled = true;

            } else if (args[0].equalsIgnoreCase("delete")) {
                String name = ArgAt(args, 1, "");
                Race race = manager.deleteRace(name);
                if (race == null) {
                    sender.sendMessage("Could not find that race.");
                } else {
                    sender.sendMessage("Deleted race " + race.name + ".");
                }
                handled = true;

            } else if (args[0].equalsIgnoreCase("list")) {
                int PAGE_SIZE = 5;
                int page = IntegerArgAt(args, 1, 1);
                List<Race> races = manager.getAllRaces();

                long nrRaces = races.size();
                long nrPages = (long) Math.ceil((double) nrRaces / PAGE_SIZE);

                sender.sendMessage("----==== Races [Page " + page + "/" + nrPages + "] ====----");
                if (nrRaces == 0) {
                    sender.sendMessage("There are no races set up on this server.");
                    sender.sendMessage("Type " + ChatColor.BLUE + "/race start" + ChatColor.WHITE + " to set one up");
                } else {
                    races.stream()
                         .skip((page - 1) * PAGE_SIZE)
                         .limit(PAGE_SIZE)
                         .forEach((Race r) -> { sender.sendMessage(r.toBrief()); });
                }
                handled = true;

            } else if (args[0].equalsIgnoreCase("info")) {
                String name = ArgAt(args, 1, "");
                Race race = manager.getRace(name);
                if (race == null) {
                    sender.sendMessage("Could not find that race.");
                } else {
                    sender.sendMessage(race.toString());
                }
                handled = true;

            }

            return handled;
        }
        return false;
    }

    String ArgAt(String[] args, int index, String defaultValue) {
        if (index >= args.length) return defaultValue;
        return args[index];
    }

    int IntegerArgAt(String[] args, int index, int defaultValue) {
        if (index >= args.length) return defaultValue;
        return Integer.parseInt(args[index], 10);
    }
}
