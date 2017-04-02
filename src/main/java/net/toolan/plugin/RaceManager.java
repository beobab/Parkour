package net.toolan.plugin;

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

    // Way to get waypoint to an entrant...
    private Map<UUID, RaceEntrant> _entrants = new HashMap<>();

    // PlayerWaypointKey -> RaceEntrant. Added when a user starts a race.
    private Map<String, RaceEntrant> _waypoints = new HashMap<>();


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

    // Player is starting a race!
    public void startRace(UUID playerKey, String wayPointKey) {
        Race race = _startpoints.get(wayPointKey);

        RaceEntrant entrant = new RaceEntrant(playerKey, race);
        for (String waypointKey: race.WayPointKeys()) {
            playerWayPointKey(playerKey, wayPointKey);
        }
    }

    public void endRace(UUID playerKey) {

    }

    // Is the waypoint one that this player can trigger as part of this race.
    public boolean isActiveRaceWaypoint(UUID playerKey, String waypointKey) {
        return _waypoints.containsKey(playerWayPointKey(playerKey, waypointKey));
    }


    public RaceEntrant getEntrant(UUID playerKey) {
        return _entrants.get(playerKey);
    }


    // Private methods.
    private String playerWayPointKey(UUID playerKey, String wayPointKey) {return playerKey.toString() + "|" + wayPointKey; }

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

}
