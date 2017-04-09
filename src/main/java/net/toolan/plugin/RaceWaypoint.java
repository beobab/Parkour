package net.toolan.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class RaceWaypoint {
    public String worldname;
    public Location location;

    RaceWaypoint (String worldname, Location location) {
        this.worldname = worldname;
        this.location = location;
    }

    public boolean equals(RaceWaypoint other) {
        if (other == null) return false;
        if (isWorldJump(other)) return false;
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
        if (data.length > 1) x = Integer.parseInt(data[1]);
        if (data.length > 2) y = Integer.parseInt(data[2]);
        if (data.length > 3) z = Integer.parseInt(data[3]);

        return new RaceWaypoint(worldName, new Location(null, x, y, z));
    }

    public static String WayPointKey(String worldName, Location location) {
        return worldName + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ();
    }

    public String WayPointKey() {
        return WayPointKey(worldname, location);
    }

    public static String DisplayWayPointKey(String worldName, Location location) {
        return "(" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + ") in " + worldName + ".";
    }

    public String DisplayWayPointKey() {
        return DisplayWayPointKey(worldname, location);
    }

    public boolean isWorldJump(RaceWaypoint other) {
        return !worldname.equalsIgnoreCase(other.worldname);
    }

    public double distanceTo(RaceWaypoint other) {
        if (isWorldJump(other)) return 0;
        double dX = location.getBlockX() - other.location.getBlockX();
        double dY = location.getBlockY() - other.location.getBlockY();
        double dZ = location.getBlockZ() - other.location.getBlockZ();
        return Math.sqrt(dX*dX + dY*dY + dZ*dZ);
    }

    // Used to set the compass.
    public Location getLocation() {
        World world = Bukkit.getWorld(worldname);
        return new Location(world, location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
    }
}
