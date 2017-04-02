package net.toolan.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jonathan on 02/04/2017.
 * This maintains the list of races and the people racing in them.
 */
public class RaceManager {
    private final Parkour plugin;

    RaceManager(Parkour plugin) {
        this.plugin = plugin;
    }


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

    public boolean isSettingUpRace(UUID playerKey) {
        if (playerKey == null) return false;
        return _setups.containsKey(playerKey);
    }

    // Adding a waypoint during setup.
    public void addWaypoint(UUID playerKey, String waypointKey) {
        Race race = _setups.get(playerKey);
        race.addWayPoint(waypointKey);
    }


    // ******************
    // Running in a race.
    // ******************

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

    public boolean isActiveRaceWaypoint(UUID playerKey, String waypointKey) {
        return _waypoints.containsKey(playerWayPointKey(playerKey, waypointKey));
    }

    public boolean isRaceStartLocation(String wayPointKey) {
        return _startpoints.containsKey(wayPointKey);
    }

    public RaceEntrant getEntrant(UUID playerKey) {
        return _entrants.get(playerKey);
    }


    // Private methods.
    private String playerWayPointKey(UUID playerKey, String wayPointKey) {return playerKey.toString() + "|" + wayPointKey; }

}
