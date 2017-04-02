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

    int WayPointCount() { return waypoints.size(); }

    List<String> WayPointKeys() {
        List<String> lst = new ArrayList<>();
        for (RaceWaypoint waypoint: waypoints) {
            lst.add(waypoint.WayPointKey());
        }
        return lst;
    }

    public RaceWaypoint addWayPoint(String waypointKey) {
        RaceWaypoint end = getEnd();
        RaceWaypoint newPoint = RaceWaypoint.FromKey(waypointKey);
        if (end == null) {
            if (waypoints.add(newPoint)) return newPoint;
        } else if (!end.equals(newPoint)) {
            if (waypoints.add(newPoint)) return newPoint;
        }
        return null;
    }

    public String toBrief() {
        RaceWaypoint start = getStart();
        return name + " starts at (" + Integer.toString(start.location.getBlockX()) + "," +
                Integer.toString(start.location.getBlockY()) + "," +
                Integer.toString(start.location.getBlockZ()) + ")";
    }

    public String toString() {
        int nrJumps = 0;
        double distance = 0.0;
        RaceWaypoint start = getStart();
        RaceWaypoint prev = start;
        for (RaceWaypoint point : waypoints) {
            if (point.isWorldJump(prev)) nrJumps++;
            distance += point.distanceTo(prev);
        }
        return "Race " + name +
                " starts at (" + Integer.toString(start.location.getBlockX()) + "," +
                                 Integer.toString(start.location.getBlockY()) + "," +
                                 Integer.toString(start.location.getBlockZ()) + ") and" +
                " covers " + Integer.toString((int) distance) + " steps" +
                " over " + Integer.toString(WayPointCount()) + " waypoints" +
                (nrJumps == 0 ? "" : " between " + Integer.toString(nrJumps + 1) + " worlds") +
                ".";
    }
}

