package net.toolan.plugin;

import org.bukkit.Location;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 02/04/2017.
 * To test waypointKeys.
 */
public class RaceWaypointTest {
    @Test
    public void wayPointKey() throws Exception {
        //Arrange
        Location l = new Location(null, 3,4,5);
        // Act
        String key = RaceWaypoint.WayPointKey("world", l);
        //Assert
        Assert.assertSame("WaypointKey matches", "world|3|4|5", key);
    }

}