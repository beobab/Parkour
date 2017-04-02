package net.toolan.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jonathan on 01/04/2017.
 * A race is a collection of waypoints. It has a start and an end.
 */
public class Race {
    Race() {
        waypoints = new ArrayList<>();
    }

    String name;
    RaceWaypoint getStart() { if (waypoints.isEmpty()) return null; return waypoints.get(0); }
    private List<RaceWaypoint> waypoints;
    RaceWaypoint getEnd() { if (waypoints.isEmpty()) return null; return waypoints.get(waypoints.size() - 1); }

    RaceWaypoint getWaypoint(int index) {
        if (waypoints.size() >= index) return waypoints.get(index);
        return null;
    }

    List<String> WayPointKeys() {
        List<String> lst = new ArrayList<>();
        for (RaceWaypoint waypoint: waypoints) {
            lst.add(waypoint.WayPointKey());
        }
        return lst;
    }

    public void addWayPoint(String waypointKey) {
        RaceWaypoint end = getEnd();
        RaceWaypoint newPoint = RaceWaypoint.FromKey(waypointKey);
        if (end == null)
            waypoints.add(newPoint);
        else if (!end.equals(newPoint)) {
            waypoints.add(newPoint);
        }
    }
}

