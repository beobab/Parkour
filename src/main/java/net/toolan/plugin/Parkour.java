package net.toolan.plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by jonathan on 01/04/2017.
 * race start name
 *   First plate you go over will mark the start of the race.
 *   Subsequent plates will act as checkpoints.
 * race disqualify below
 *   This will disqualify players who fall below a certain point on the map.
 * race end
 *   Next plate you go over will mark the END of the race. The finish line.
 *
 **/
public class Parkour extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new ParkourListener(this), this);
        this.getCommand("basic").setExecutor(new ParkourCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
    }
}
