package net.toolan.plugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by jonathan on 02/04/2017.
 * This maintains the list of races and the people racing in them.
 */
public class RaceManager {
    private final Parkour plugin;

    RaceManager(Parkour plugin) {
        this.plugin = plugin;
    }

    // Storage for all the races. Fill this in Enable, and save in Disable.
    private List<Race> _allRaces = new ArrayList<>();

    // Public methods.
    // All the races currently being set up. Normally only one at a time.
    private Map<UUID, Race> _setups = new HashMap<>();

    // WaypointKey -> Race Start positions.
    private Map<String, Race> _startpoints = new HashMap<>();

    // The people who are currently in a race.
    private Map<UUID, RaceEntrant> _entrants = new HashMap<>();

    // Player -> WaypointKey -> RaceEntrant. Added when a user starts a race.
    private Map<UUID, Map<String, RaceEntrant>> _waypoints = new HashMap<>();


    // ******************
    // Setting up a race.
    // ******************


    public Race setupRace(UUID playerKey, String name) {
        if (name == null || name.isEmpty())
            name = "race" + Integer.toString(_allRaces.size() + 1);

        Race race = findRaceNamed(name);
        if (race == null) {
            race = new Race();
            race.name = name;
            _allRaces.add(race);
        }

        _setups.put(playerKey, race);
        return race;
    }

    public boolean isSettingUpRace(UUID playerKey) {
        if (playerKey == null) return false;
        return _setups.containsKey(playerKey);
    }

    // Adding a waypoint during setup.
    public RaceWaypoint addWaypoint(UUID playerKey, String waypointKey) {
        Race race = _setups.get(playerKey);
        return race.addWayPoint(waypointKey);
    }

    public Race endSetupRace(UUID playerKey) {
        Race race = _setups.remove(playerKey);
        refreshRaceStarts();
        return race;
    }

    // Note that there is nothing stopping a race from completing even if it has been deleted.
    public Race deleteRace(String name) {
        Race race = findRaceNamed(name);
        if (race != null)
            _allRaces.remove(race);

        refreshRaceStarts();
        return race;
    }

    // ******************
    // Running in a race.
    // ******************

    // Is this a valid place to start a race?
    public boolean isRaceStartLocation(String wayPointKey) {
        return _startpoints.containsKey(wayPointKey);
    }

    // Is this player in an active race?
    public boolean isPlayerRacing(UUID playerKey) {
        return _entrants.containsKey(playerKey);
    }

    // Player is starting a race!
    public RaceEntrant startRace(UUID playerKey, String startWayPointKey) {
        Race race = _startpoints.get(startWayPointKey);
        if (race == null) return null;

        RaceEntrant entrant = new RaceEntrant(playerKey, race);
        if (entrant == null) return null;

        Map<String, RaceEntrant> waypointEntrant = new HashMap<>();
        for (String wayPointKey: race.WayPointKeys()) {
            waypointEntrant.put(wayPointKey, entrant);
        }

        _waypoints.put(playerKey, waypointEntrant);
        _entrants.put(playerKey, entrant);

        return entrant;
    }

    public void endRace(UUID playerKey) {
        _waypoints.remove(playerKey);
        _entrants.remove(playerKey);
    }

    // Is the waypoint one that this player can trigger as part of this race.
    public boolean isActiveRaceWaypoint(UUID playerKey, String waypointKey) {
        Map<String, RaceEntrant> wayPoints = _waypoints.get(playerKey);
        if (wayPoints == null) return false;

        return wayPoints.containsKey(waypointKey);
    }


    public RaceEntrant getEntrant(UUID playerKey) {
        return _entrants.get(playerKey);
    }


    // Private methods.
    private Race findRaceNamed(String name) {
        for (Race race : _allRaces) {
            if (race.name.equalsIgnoreCase(name)) return race;
        }
        return null;
    }

    private void refreshRaceStarts() {
        _startpoints.clear();
        for (Race race : _allRaces) {
            if (race.WayPointCount() > 1) {
                RaceWaypoint start = race.getStart();
                _startpoints.put(start.WayPointKey(), race);
            }
        }
    }

    public Race NearestRace(RaceWaypoint temporary) {
        if (temporary == null) return null;

        Race nearest = null;
        double distance = 0.0;
        for (Race race : _allRaces) {
            if (race.WayPointCount() > 1) {
                RaceWaypoint start = race.getStart();
                double toThisRace = temporary.distanceTo(start);
                if (nearest == null || distance > toThisRace) {
                    nearest = race;
                    distance = toThisRace;
                }
            }
        }
        return nearest;
    }
}
