package net.toolan.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by jonathan on 04/04/2017.
 * Generic storage of objects in a sqlite database.
 */
class Database<T> {

    // An empty example of the class you wish to store. To get round limitations of reflection.
    // Normally like this: Database db = new Database(f, l, new DatabaseStorageObject())
    private final T _empty;
    Database(File database, ILogger logger, T empty) {
        _empty = empty;
        _initialise(database, logger);
    }

    public interface ILogger {
        void log(Level level, String message);
        void log(Level level, String message, Exception ex);
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




    private String _databaseName;
    private File _database = null;
    private Connection _connection = null;
    private ILogger _logger = null;
    private Boolean _tableExists = null;
    private String _primaryKeyName = null;
    private List<String> _columnNames = new ArrayList<>();

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
        EnsureTablesExist(_empty.getClass());
    }

    private List<T> _retrieveAll() {
        EnsureTablesExist(_empty.getClass());
        return new ArrayList<T>();
    }

    private T _retrieve(String key) {
        EnsureTablesExist(_empty.getClass());
        return null;
    }

    private String _buildTableSql(Field f, String primaryKeyColumn) {
        SubTable t = f.getAnnotation(SubTable.class);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(t.name());

        sb.append("( ");
        sb.append(primaryKeyColumn);
        sb.append("`");
        sb.append(f.getName());
        sb.append("` ");
        sb.append(t.type());
        sb.append(t.nullable() ? " NULL ); " : " NOT NULL );");

        return sb.toString();
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
        sb.append("( ");

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
        if (_tableExists) return;

        final String sql = _buildTableSql(c);

        Query((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.execute();
            }
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

