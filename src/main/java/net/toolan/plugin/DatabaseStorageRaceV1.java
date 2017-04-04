package net.toolan.plugin;

import java.util.List;

/**
 * Created by jonathan on 04/04/2017.
 * DTO class which can be populated from Database of the right class, and can store information back in the DB.
 */
@Database.Table(name = "race")
public class DatabaseStorageRaceV1 {
    @Database.PrimaryKey
    String name;
    @Database.Column(nullable = true)
    String description;
    @Database.Column
    String worldname;

    @Database.SubTable(name = "waypoint")
    List<String> waypoints;
}
