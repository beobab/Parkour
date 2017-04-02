package net.toolan.plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

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

    private RaceManager _allRaces = null;
    RaceManager getRaceManager() { return _allRaces; }

    @Override
    public void onEnable() {
        _allRaces = new RaceManager(this);

        Bukkit.getPluginManager().registerEvents(new ParkourListener(this), this);
        this.getCommand("race").setExecutor(new ParkourCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
    }
}
