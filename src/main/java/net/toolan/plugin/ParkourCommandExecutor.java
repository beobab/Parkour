package net.toolan.plugin;

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

            if (args[0].equalsIgnoreCase("setup")) {
                if (manager.isSettingUpRace(playerKey)) {
                    sender.sendMessage("You are already setting up a race. Finish that one first.");
                    return true;
                }

                String name = ArgAt(args, 1, "");
                manager.setupRace(playerKey, name);
                sender.sendMessage("Activate levers, pressure plates and buttons to mark waypoints.");
                sender.sendMessage("Type &6/race finish&F after you have activated the last one.");

            } else if (args[0].equalsIgnoreCase("finish")) {
                Race race = manager.endSetupRace(playerKey);
                if (race.WayPointCount() > 1)
                    sender.sendMessage("Race " + race.name + " created with " + race.WayPointCount() + " waypoints.");
                else
                    sender.sendMessage("Race " + race.name + " is not a valid race. You will need to create more waypoints.");

            } else if (args[0].equalsIgnoreCase("delete")) {
                Race race = manager.endSetupRace(playerKey);
                if (race.WayPointCount() > 1)
                    sender.sendMessage("Race " + race.name + " created with " + race.WayPointCount() + " waypoints.");
                else
                    sender.sendMessage("Race " + race.name + " is not a valid race. You will need to create more waypoints.");

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
