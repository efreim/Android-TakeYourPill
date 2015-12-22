package pl.balazinski.jakub.takeyourpill.data.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;

/**
 * Class that stores pill objects in database
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static DatabaseHelper instance;
    private RuntimeExceptionDao<Pill, Integer> pillDao;

    public DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        recreateDatabase(connectionSource);
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        recreateDatabase(connectionSource);
    }

    private void recreateDatabase(ConnectionSource connectionSource) {
        try {
            TableUtils.dropTable(connectionSource, Pill.class, true);
            TableUtils.createTableIfNotExists(connectionSource, Pill.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RuntimeExceptionDao<Pill, Integer> getDao() {
        if (pillDao == null) {
            pillDao = getRuntimeExceptionDao(Pill.class);
        }
        return pillDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        pillDao = null;
    }
}
