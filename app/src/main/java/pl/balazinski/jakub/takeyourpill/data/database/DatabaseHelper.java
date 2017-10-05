package pl.balazinski.jakub.takeyourpill.data.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import pl.balazinski.jakub.takeyourpill.utilities.Constants;
import pl.balazinski.jakub.takeyourpill.data.model.Alarm;
import pl.balazinski.jakub.takeyourpill.data.model.Pill;
import pl.balazinski.jakub.takeyourpill.data.model.PillToAlarm;

/**
 * Class that stores pill objects in database
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static DatabaseHelper instance;
    private RuntimeExceptionDao<Pill, Integer> pillDao;
    private RuntimeExceptionDao<Alarm, Integer> alarmDao;
    private RuntimeExceptionDao<PillToAlarm, Integer> pillToAlarmDao;


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
        try {
            TableUtils.createTableIfNotExists(connectionSource, Pill.class);
            TableUtils.createTableIfNotExists(connectionSource, Alarm.class);
            TableUtils.createTableIfNotExists(connectionSource, PillToAlarm.class);

        }catch (SQLException e){
            Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        recreateDatabase(connectionSource);
    }

    private void recreateDatabase(ConnectionSource connectionSource) {
        try {
            TableUtils.dropTable(connectionSource, Pill.class, true);
            TableUtils.createTableIfNotExists(connectionSource, Pill.class);
            TableUtils.dropTable(connectionSource, Alarm.class, true);
            TableUtils.createTableIfNotExists(connectionSource, Alarm.class);
            TableUtils.dropTable(connectionSource, PillToAlarm.class, true);
            TableUtils.createTableIfNotExists(connectionSource, PillToAlarm.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Pill database access object
     * @return Pill database access object
     */
    public RuntimeExceptionDao<Pill, Integer> getPillDao() {
        if (pillDao == null) {
            pillDao = getRuntimeExceptionDao(Pill.class);
        }
        return pillDao;
    }

    /**
     * Get Alarm database access object
     * @return Alarm database access object
     */
    public RuntimeExceptionDao<Alarm, Integer> getAlarmDao() {
        if (alarmDao == null) {
            alarmDao = getRuntimeExceptionDao(Alarm.class);
        }
        return alarmDao;
    }

    /**
     * Get PillToAlarm database access object
     * @return PillToAlarm database access object
     */
    public RuntimeExceptionDao<PillToAlarm, Integer> getPillToAlarmDao() {
        if (pillToAlarmDao == null) {
            pillToAlarmDao = getRuntimeExceptionDao(PillToAlarm.class);
        }
        return pillToAlarmDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        pillDao = null;
        alarmDao = null;
        pillToAlarmDao = null;
    }
}
