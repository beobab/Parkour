package net.toolan.plugin;

import java.util.List;

/**
 * Created by jonathan on 04/04/2017.
 * DTO class which can be populated from Database of the right class, and can store information back in the DB.
 */
@Database.Table(name = "race")
public class DatabaseStorageRaceV1 implements Database.IStorable {
    @Database.PrimaryKey
    public String name;
    @Database.Column(nullable = true)
    public String description;
    @Database.Column
    public String worldname;

    @Database.SubTable(name = "waypoint")
    public List<String> waypoints;

    public DatabaseStorageRaceV1 empty() { return new DatabaseStorageRaceV1();}
}
