package net.toolan.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
            if (args[0].equalsIgnoreCase("setup")) {

            } else if (args[0].equalsIgnoreCase("finish")) {

            }
            sender.sendMessage("Starting race.");

            return true;
        }
        return false;
    }
}
