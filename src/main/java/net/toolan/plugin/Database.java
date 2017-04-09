package net.toolan.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by jonathan on 04/04/2017.
 * Generic storage of objects in a sqlite database.
 */
class Database<T> {

    // An empty example of the class you wish to store. To get round limitations of reflection.
    // Normally like this: Database db = new Database(f, l, new DatabaseStorageObject())
    private final IStorable<T> _empty;
    Database(File database, ILogger logger, IStorable<T> empty) {
        _empty = empty;
        if (database != null) {
            _initialise(database, logger);
        }
    }

    public interface ILogger {
        void log(Level level, String message);
        void log(Level level, String message, Exception ex);
    }

    public interface IStorable<T> {
        T empty();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Table {
        String name() default ""; // will use the name of the class (trims DatabaseStorage off the front by default).
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PrimaryKey {
        String type() default "varchar(32)";
        boolean nullable() default false;
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Column {
        String type() default "varchar(32)";
        boolean nullable() default false;
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubTable {
        String name() default ""; // will use the name of the class (trims DatabaseStorage off the front by default).
        String type() default "varchar(32)";
        boolean nullable() default false;
    }

    public void Store(T object) { _store(object); }
    public List<T> RetrieveAll() { return _retrieveAll(); }


    // If you want to see the sql that is sent to  sqlitedb on initial creation.
    public String BuildTableSql(Class c) { return _buildTableSql(c); }



    private String _databaseName;
    private File _database = null;
    private Connection _connection = null;
    private ILogger _logger = null;
    private Boolean _tableExists = false;
    private String _primaryKeyName = null;
    private List<String> _columnNames = new ArrayList<>();

    // In case _logger is null, we provide default behaviour.
    private void log(Level level, String message) {
        if (_logger != null) _logger.log(level, message);
    }
    private void log(Level level, String message, Exception ex) {
        if (_logger != null) _logger.log(level, message, ex);
    }

    private void _initialise(File database, ILogger logger) {
        _logger = logger;
        _database = database;
        _databaseName = _database.getName();
        if (!_database.exists()){
            try {
                _database.createNewFile();
            } catch (IOException e) {
                _logger.log(Level.SEVERE, "File write error: " + _databaseName);
            }
        }
        _connection = _getSqlConnection();

        Query((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement("select sqlite_version();")) {
                ps.executeQuery();
            }
        });

        EnsureTablesExist(_empty.getClass());
    }

    private Connection _getSqlConnection() {
        try {
            if(_connection != null && !_connection.isClosed()){
                return _connection;
            }
            Class.forName("org.sqlite.JDBC");
            _connection = DriverManager.getConnection("jdbc:sqlite:" + _database);
            return _connection;

        } catch (SQLException ex) {
            _logger.log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            _logger.log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    private void _store(T object) {
        classDetail cd = getClassDetail();

        List<String> columns = new ArrayList<>();

        for (Map.Entry<PrimaryKey, Field> entry : cd.pk.entrySet()) {
            Field f = entry.getValue();
            String name = f.getName();
            columns.add(name);
        }

        for (Map.Entry<Column, Field> entry : cd.cols.entrySet()) {
            Field f = entry.getValue();
            String name = f.getName();
            columns.add(name);
        }

        final String sql = "INSERT OR REPLACE INTO " + cd.tableName + " (" + _encase(columns) + ") VALUES (" + _enquestion(columns) + ");";

        Query((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                int i = 1;
                String primaryKeyName = "";
                String primaryKeyValue = "";

                for (Map.Entry<PrimaryKey, Field> entry : cd.pk.entrySet()) {
                    Field f = entry.getValue();
                    String name = f.getName();

                    String value = (String) f.get(object);
                    ps.setString(i++, value);

                    primaryKeyName = name;
                    primaryKeyValue = value;
                }

                for (Map.Entry<Column, Field> entry : cd.cols.entrySet()) {
                    Field f = entry.getValue();

                    String value = (String) f.get(object);
                    ps.setString(i++, value);
                }

                ps.execute();

                List<String> values = new ArrayList<>();
                for (Map.Entry<SubTable, Field> entry : cd.subtables.entrySet()) {
                    Field f = entry.getValue();
                    String fieldName = f.getName();
                    String tableName = entry.getKey().name();

                    String deleteSql = "DELETE FROM " + tableName + " WHERE " + primaryKeyName + " = ?";
                    try(PreparedStatement psT = conn.prepareStatement(deleteSql)) {
                        psT.setString(1, primaryKeyValue);
                        psT.execute();
                    }

                    Object obj = f.get(object);
                    // Check it's an ArrayList
                    if (obj instanceof ArrayList<?>) {
                        // Get the List.
                        ArrayList<?> al = (ArrayList<?>) obj;
                        if (al.size() > 0) {
                            // Iterate.
                            for (int j = 0; j < al.size(); j++) {
                                // Still not enough for a type.
                                Object o = al.get(j);
                                if (o instanceof String) {
                                    // Here we go!
                                    String v = (String) o;
                                    // use v.
                                    values.add(v);
                                }
                            }
                        }
                    }

                    final String sqlT = _buildListInsert(tableName, values, fieldName, primaryKeyValue, primaryKeyName);
                    try(PreparedStatement psT = conn.prepareStatement(sqlT)) {
                        psT.execute();
                    }
                }
            }
        });
    }

    // put a question mark instead of all the values, and comma separate them.
    private String _enquestion(List<String> lst) {
        StringBuilder sb = new StringBuilder();
        boolean prependComma = false;
        for (String item : lst) {
            if (prependComma) {
                sb.append(", ");
            } else {
                prependComma = true;
            }

            sb.append("?");
        }
        return sb.toString();
    }

    // put quotes round all the values, and comma separate them.
    private String _encase(List<String> lst) {
        StringBuilder sb = new StringBuilder();
        boolean prependComma = false;
        for (String item : lst) {
            if (prependComma) {
                sb.append(", ");
            } else {
                prependComma = true;
            }

            sb.append("`");
            sb.append(item);
            sb.append("`");
        }
        return sb.toString();
    }

    private List<T> _retrieveAll() {
        classDetail cd = getClassDetail();

        List<T> lst = new ArrayList<>();

        final String sql = "SELECT * FROM " + cd.tableName;

        Query((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        T item = _empty.empty();
                        String primaryKeyName = "";
                        String primaryKeyValue = "";

                        for (Map.Entry<PrimaryKey, Field> entry : cd.pk.entrySet()) {
                            Field f = entry.getValue();
                            String name = f.getName();
                            String value = rs.getString(name);

                            f.set(item, value);

                            primaryKeyName = name;
                            primaryKeyValue = value;
                        }

                        for (Map.Entry<Column, Field> entry : cd.cols.entrySet()) {
                            Field f = entry.getValue();
                            String name = f.getName();
                            f.set(item, rs.getString(name));
                        }

                        for (Map.Entry<SubTable, Field> entry : cd.subtables.entrySet()) {
                            List<String> subItems = new ArrayList<>();
                            String subTableName = entry.getKey().name();
                            Field f = entry.getValue();
                            String name = f.getName();

                            final String sqlT = "SELECT * FROM " + subTableName + " WHERE " + primaryKeyName + "  = ?;";
                            try(PreparedStatement psT = conn.prepareStatement(sqlT)) {
                                psT.setString(1, primaryKeyValue);

                                try (ResultSet rsT = psT.executeQuery()) {
                                    while (rsT.next()) {
                                        subItems.add(rsT.getString(name));
                                    }
                                }

                                f.set(item, subItems);
                            }
                        }

                        lst.add(item);
                    }
                }
            }
        });

        return lst;
    }

    private T _retrieve(String key) {
//        classDetail cd = getClassDetail();
//
//        Field field = _empty.getClass().getDeclaredField(fieldName);
//        field.setAccessible(true);
//        Object value = field.get(object);
        return null;
    }

    private String _buildListInsert(String tableName, List<String> values, String columnName, String masterColumn, String masterCoumnName) {
        // masterColumn should be: "'door32' as 'name'"
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO '" + tableName + "'\n");
        boolean isFirst = true;
        for (String item : values) {
            if (isFirst) {
                sb.append("          SELECT '");
                sb.append(masterColumn);
                sb.append("' as '");
                sb.append(masterCoumnName);
                sb.append("', '");
                sb.append(item);
                sb.append("' AS '");
                sb.append(columnName);
                sb.append("'\n");

                isFirst = false;

            } else {
                sb.append("UNION ALL SELECT '");
                sb.append(masterColumn);
                sb.append("', '");
                sb.append(item);
                sb.append("'\n");
            }
        }
        sb.append(";");

        return sb.toString();
    }

    private String _buildTableSql(Field f, String primaryKeyColumn) {
        SubTable t = f.getAnnotation(SubTable.class);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(t.name());

        sb.append(" ( ");
        sb.append(primaryKeyColumn);
        sb.append("`");
        sb.append(f.getName());
        sb.append("` ");
        sb.append(t.type());
        sb.append(t.nullable() ? " NULL ); " : " NOT NULL );");

        return sb.toString();
    }

    private classDetail _classDetail;
    private classDetail getClassDetail() {
        if (_classDetail == null) _initialiseClassDetail(_empty.getClass());
        return _classDetail;
    }
    private class classDetail {
        public String tableName = "";
        public Map<PrimaryKey, Field> pk = new HashMap<>();
        public Map<Column, Field> cols = new HashMap<>();
        public Map<SubTable, Field> subtables = new HashMap<>();
    }

    private classDetail _initialiseClassDetail(Class c) {
        _classDetail = new classDetail();

        Table table = ((Table) c.getAnnotation(Table.class));
        String tableName = (table == null ? "" : table.name());
        if (tableName.isEmpty()) {
            tableName = c.getName().replaceFirst("DatabaseStorage", "");
        }
        _classDetail.tableName = tableName;

        for (Field f : c.getDeclaredFields()) {
            PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
            if (pk != null) {
                _classDetail.pk.put(pk, f);
            }
            Column col = f.getAnnotation(Column.class);
            if (col != null) {
                _classDetail.cols.put(col, f);
            }
            SubTable t = f.getAnnotation(SubTable.class);
            if (t != null) {
                _classDetail.subtables.put(t, f);
            }
        }
        return _classDetail;
    }

    private String _buildTableSql(Class c) {
        Table table = ((Table) c.getAnnotation(Table.class));
        String tableName = (table == null ? "" : table.name());
        if (tableName.isEmpty()) {
            tableName = c.getName().replaceFirst("DatabaseStorage", "");
        }

        String primaryKeyColumn = "`id` integer NOT NULL,";
        String primaryKeyLine = "PRIMARY KEY (`id`)";
        List<String> otherColumns = new ArrayList<>();
        List<Field> otherTables = new ArrayList<>();


        for (Field f : c.getDeclaredFields()) {
            PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
            if (pk != null) {
                primaryKeyColumn = "`" + f.getName() + "` " + pk.type() + " " + (pk.nullable() ? " NULL, " : "NOT NULL, ");
                primaryKeyLine = "PRIMARY KEY (`" + f.getName() + "`)";
            }
            Column col = f.getAnnotation(Column.class);
            if (col != null) {
                otherColumns.add("`" + f.getName() + "` " + col.type() + " " + (col.nullable() ? " NULL, " : "NOT NULL, "));
            }
            SubTable t = f.getAnnotation(SubTable.class);
            if (t != null) {
                otherTables.add(f);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(tableName);
        sb.append(" ( ");

        sb.append(primaryKeyColumn);
        for (String s : otherColumns) {
            sb.append(s);
        }
        sb.append(primaryKeyLine);
        sb.append(");");

        for (Field f : otherTables) {
            sb.append(_buildTableSql(f, primaryKeyColumn));
        }

//        for (Field f : getClass().getDeclaredFields()) {
//            sb.append(f.getName());
//            sb.append("=");
//            sb.append(f.get(this));
//            sb.append(", ");
//        }
        return sb.toString();
    }


    private void EnsureTablesExist(Class c) {
        // Does table exist?
        // If we already ran this, then yes. It's created. Otherwise check.
        if (_tableExists == true) return;

        final String sql = _buildTableSql(c);
        final String[] sqlItems = sql.split(";");

        Query((Connection conn) -> {
            for(String sqlItem : sqlItems) {
                try (PreparedStatement ps = conn.prepareStatement(sqlItem)) {
                    ps.execute();
                }
            };
        });

        // Yes.
        // Loop through each column, adding it.
        // Primary key should already exist.


        // No.
        // send complete create statement off to sqlite database.
        // Primary key is annotated.

        _tableExists = true;
    }

    interface ISqlCommand {
        void execute(Connection conn) throws Exception;
    }

    public void Query(ISqlCommand sql) {
        if (sql == null) return;
        Connection conn = null;
        try {
            conn = _getSqlConnection();
            sql.execute(conn);

        } catch (SQLException ex) {
            _logger.log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "Some other exception, maybe parameter related.", ex);
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                _logger.log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
    }

}

