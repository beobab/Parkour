package net.toolan.plugin;

import java.util.List;

/**
 * Created by jonathan on 04/04/2017.
 * DTO class which can be populated from Database of the right class, and can store information back in the DB.
 * You need to write ToRace and FromRace - your actual models.
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

    public Race toRace() {
        Race race = new Race();
        race.name = this.name;
        for (String waypoint : this.waypoints) {
            race.addWayPoint(waypoint);
        }
        return race;
    }

    public static DatabaseStorageRaceV1 fromRace(Race race) {
        DatabaseStorageRaceV1 store = new DatabaseStorageRaceV1();
        store.name = race.name;
        store.worldname = race.getWaypoint(0).worldname;
        store.waypoints = race.WayPointKeys();
        return store;
    }
}
