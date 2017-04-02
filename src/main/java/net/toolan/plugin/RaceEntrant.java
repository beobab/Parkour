package net.toolan.plugin;

import org.bukkit.entity.Player;

import java.util.UUID;

public class RaceEntrant {
    Race race;
    UUID playerKey;

    RaceEntrant(UUID playerKey, Race race) {
        this.playerKey = playerKey;
        this.race = race;
        this.currentWaypoint = 0;
    }

    int currentWaypoint = 0;

    public boolean canTeleportToNextWaypoint() {
        RaceWaypoint currentWaypoint = race.getWaypoint(this.currentWaypoint);
        RaceWaypoint nextWaypoint = race.getWaypoint(this.currentWaypoint + 1);

        if (currentWaypoint == null || nextWaypoint == null) return false;

        // You can only teleport if the world name is different.
        return (!currentWaypoint.worldname.equalsIgnoreCase(nextWaypoint.worldname));
    }

    public boolean hitWayPoint(RaceWaypoint hit) {
        RaceWaypoint currentWaypoint = race.getWaypoint(this.currentWaypoint);
        if (currentWaypoint == null) {
            // Not in a race. Exit as soon as possible.
            return false;
        }

        if (currentWaypoint.equals(hit)) {
            // Do nothing. It's the one you are already on. Maybe a plate is repeating, or the player
            // went back to the last checkpoint.
            return false;
        }

        RaceWaypoint nextWaypoint = race.getWaypoint(this.currentWaypoint + 1);
        if (nextWaypoint == null) {
            // Not sure how this would come about, but the player has already finished the race.
            return false;
        }

        if (!nextWaypoint.equals(hit)) {
            // Not the correct waypoint. They may have lost their way.
            return false;
        }

        // The successful case!
        this.currentWaypoint++;
        return true;
    }

    public boolean hasWon() {
        RaceWaypoint currentWaypoint = race.getWaypoint(this.currentWaypoint);
        if (currentWaypoint == null) return false;
        return (currentWaypoint.equals(race.getEnd()));
    }

    public boolean isCurrentWayPoint(RaceWaypoint hit) {
        RaceWaypoint currentWaypoint = race.getWaypoint(this.currentWaypoint);
        if (currentWaypoint == null) {
            // Not in a race. Exit as soon as possible.
            return false;
        }

        return currentWaypoint.equals(hit);
    }
}
