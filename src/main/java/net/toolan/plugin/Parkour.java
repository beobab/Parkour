package net.toolan.plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

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
public class Parkour
        extends JavaPlugin
        implements Database.ILogger {

    private RaceManager _allRaces = null;
    RaceManager getRaceManager() { return _allRaces; }

    private Database<DatabaseStorageRaceV1> _db = null;

    //region ILogger interface ...

    @Override
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    @Override
    public void log(Level level, String message, Exception ex) {
        getLogger().log(level, message, ex);
    }

    //endregion

    private void setupDB() {
        String sqlitedb = getConfig().getString("SQLite.filename", "races.db");
        _db = new Database<>(new File(this.getDataFolder(), sqlitedb), this, new DatabaseStorageRaceV1());

        _allRaces = new RaceManager(this);
        _db.RetrieveAll().stream()
                .map(DatabaseStorageRaceV1::toRace)
                .forEach((race) -> _allRaces.storageAddRaceFromStorage(race));
        _allRaces.storageDoneAddingRaces();
    }

    @Override
    public void onEnable() {
        // This should create the config directories.
        saveDefaultConfig();

        setupDB();

        Bukkit.getPluginManager().registerEvents(new ParkourListener(this), this);
        this.getCommand("race").setExecutor(new ParkourCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        _allRaces.getAllRaces().stream()
                .map(DatabaseStorageRaceV1::fromRace)
                .forEach((dsr) -> _db.Store(dsr));
    }
}
