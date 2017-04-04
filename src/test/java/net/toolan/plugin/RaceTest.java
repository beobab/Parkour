package net.toolan.plugin;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 02/04/2017.
 */
public class RaceTest {
    @Test
    public void getStart() throws Exception {
    }

    @Test
    public void getEnd() throws Exception {
    }

    @Test
    public void getWaypoint() throws Exception {
        Race race = new Race();

        assertNull("Empty race should return null waypoints", race.getWaypoint(0));

        race.addWayPoint("world|10|10|10");

        RaceWaypoint actual = race.getWaypoint(0);
        assertNotNull("First waypoint should not be null", actual);

        assertNull("Asking for waypoitn above bounds should return null waypoint.", race.getWaypoint(500));

        assertNull("Asking for waypoitn above bounds should return null waypoint.", race.getWaypoint(-500));

    }

    @Test
    public void wayPointCount() throws Exception {
    }

    @Test
    public void wayPointKeys() throws Exception {
    }

    @Test
    public void addWayPoint() throws Exception {
        // Arrange
        Race race = new Race();

        // Act
        race.addWayPoint("world|10|10|10");

        // Assert
        assertEquals("One waypoint exists", 1, race.WayPointCount());
    }
    @Test
    public void addSecondWayPoint() throws Exception {
        // Arrange
        Race race = new Race();
        race.addWayPoint("world|10|10|10");

        // Act
        race.addWayPoint("world|11|10|11");

        // Assert
        assertEquals("Two waypoints exist", 2, race.WayPointCount());
    }

    @Test
    public void toBrief() throws Exception {
    }

    @Test
    public void testNoWayPointsToString() throws Exception {
        Race race = new Race();
        String actual = race.toString();
        assertEquals("Should equal a string", "Race null has no waypoints.", actual);
    }

    @Test
    public void testToString() throws Exception {
        Race race = new Race();

        race.addWayPoint("world|10|10|10");
        race.addWayPoint("world|20|10|20");
        race.addWayPoint("world_nether|2044|1044|2044");

        String actual = race.toString();

        assertEquals("Should equal a string", "Race bob starts at (10,10,10) and covers 14 steps over 3 waypoints between 2 worlds.", actual);
    }


}