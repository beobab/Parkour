package net.toolan.plugin;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 03/04/2017.
 */
public class RaceManagerTest {
    @Test
    public void setupRace() throws Exception {
        RaceManager manager = new RaceManager(null);
        UUID playerKey = UUID.randomUUID();
        Race race = manager.setupRace(playerKey, "bob");
        race.addWayPoint("world|10|10|10");
        race.addWayPoint("world|20|10|10");
        race.addWayPoint("world|30|10|10");
        manager.endSetupRace(playerKey);

        RaceEntrant entrant = manager.startRace(UUID.randomUUID(), "world|10|10|10");

        assertNotNull("Added race should not return a null race entrant", entrant);

        assertEquals("Race object of entrant should be race with same name", "bob", entrant.race.name);
    }

}