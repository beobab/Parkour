package net.toolan.plugin;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 03/04/2017.
 */
public class RaceEntrantTest {
    @Test
    public void canTeleportToNextWaypoint() throws Exception {
        Race race = new Race();
        race.addWayPoint("world|10|10|10");
        race.addWayPoint("world|11|10|11");
        race.addWayPoint("world|12|10|12");
        race.addWayPoint("world_nether|13|10|13");

        RaceEntrant entrant = new RaceEntrant(UUID.randomUUID(), race);
        entrant.currentWaypoint = 0;
        assertFalse("Entrant cannot teleport if world is the same.", entrant.canTeleportToNextWaypoint());

        entrant.currentWaypoint = 2;
        assertTrue("Entrant can teleport if world differs.", entrant.canTeleportToNextWaypoint());
    }

    @Test
    public void hitWayPoint() throws Exception {
    }

    @Test
    public void hasWon() throws Exception {
    }

    @Test
    public void isCurrentWayPoint() throws Exception {
    }

    @Test
    public void nextWayPointLocation() throws Exception {
    }

}