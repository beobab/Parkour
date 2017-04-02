package net.toolan.plugin;

import org.bukkit.Location;

public class RaceWaypoint {
    public String worldname;
    public Location location;

    RaceWaypoint (String worldname, Location location) {
        this.worldname = worldname;
        this.location = location;
    }

    public boolean equals(RaceWaypoint other) {
        if (other == null) return false;
        if (worldname != other.worldname) return false;
        if (location.getBlockX() != other.location.getBlockX()) return false;
        if (location.getBlockY() != other.location.getBlockY()) return false;
        if (location.getBlockZ() != other.location.getBlockZ()) return false;
        return true;
    }

    public static RaceWaypoint FromKey(String wayPointKey) {
        String worldName = null;
        int x = 0;
        int y = 0;
        int z = 0;

        String[] data = wayPointKey.split("\\|");
        if (data.length > 0) worldName = data[0];
        if (data.length > 1) x = Integer.getInteger(data[1]);
        if (data.length > 2) y = Integer.getInteger(data[2]);
        if (data.length > 3) z = Integer.getInteger(data[3]);

        return new RaceWaypoint(worldName, new Location(null, x, y, z));
    }

    public static String WayPointKey(String worldName, Location location) {
        return worldName + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ();
    }

    public String WayPointKey() {
        return WayPointKey(worldname, location);
    }
}
