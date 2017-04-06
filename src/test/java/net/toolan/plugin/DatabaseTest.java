package net.toolan.plugin;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 06/04/2017.
 */
public class DatabaseTest {
    @Test
    public void BuildTableSql() throws Exception {

        DatabaseStorageRaceV1 empty = new DatabaseStorageRaceV1();
        Database<DatabaseStorageRaceV1> db = new Database<>(null, null, empty);
        String actual = db.BuildTableSql(empty.getClass());

        String expected = "CREATE TABLE IF NOT EXISTS race ( `name` varchar(32) NOT NULL, `description` varchar(32)  NULL, `worldname` varchar(32) NOT NULL, PRIMARY KEY (`name`));CREATE TABLE IF NOT EXISTS waypoint ( `name` varchar(32) NOT NULL, `waypoints` varchar(32) NOT NULL );";

        assertEquals("Create SQL is two tables.", expected, actual);
    }

    @Test
    public void query() throws Exception {
    }

}