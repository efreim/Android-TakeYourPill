package pl.balazinski.jakub.takeyourpill.data.database;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.data.Alarm;
import pl.balazinski.jakub.takeyourpill.data.Pill;


public class DatabaseRepository {

    public static List<Pill> getAllPills(Context context) {
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getPillDao();
        QueryBuilder<Pill, Integer> qb = dao.queryBuilder();
        qb.orderByRaw("name COLLATE NOCASE");
        try {
            return qb.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Pill getPillByID(Context context, Long id) {
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getPillDao();
        List<Pill> pills = dao.queryForAll();
        for(Pill p : pills){
            if(id.equals(p.getId()))
                return p;
        }
        return null;
    }

    public static List<Alarm> getAllAlarms(Context context) {
        RuntimeExceptionDao<Alarm, Integer> dao = DatabaseHelper.getInstance(context).getAlarmDao();
        QueryBuilder<Alarm, Integer> qb = dao.queryBuilder();
        qb.orderBy("active", false);
        qb.orderBy("hour", true);
        qb.orderBy("minute", true);
        try {
            return qb.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Alarm getAlarmById(Context context, Long id) {
        RuntimeExceptionDao<Alarm, Integer> dao = DatabaseHelper.getInstance(context).getAlarmDao();

        List<Alarm> alarms = dao.queryForAll();
        for(Alarm a : alarms){
            if(id.equals(a.getId()))
                return a;
        }
        return null;
    }

    public static void addPill(Context context, Pill pill) {
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getPillDao();
        dao.create(pill);
    }

    public static void addAlarm(Context context, Alarm alarm) {
        RuntimeExceptionDao<Alarm, Integer> dao = DatabaseHelper.getInstance(context).getAlarmDao();
        dao.create(alarm);
    }

    public static void deletePillDatabase(Context context){
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getPillDao();
        try {
            TableUtils.clearTable(dao.getConnectionSource(), Pill.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAlarmDatabase(Context context){
        RuntimeExceptionDao<Alarm, Integer> dao = DatabaseHelper.getInstance(context).getAlarmDao();
        try {
            TableUtils.clearTable(dao.getConnectionSource(), Alarm.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteWholeDatabase(Context context){
        RuntimeExceptionDao<Pill, Integer> pillDao = DatabaseHelper.getInstance(context).getPillDao();
        RuntimeExceptionDao<Alarm, Integer> alarmDao = DatabaseHelper.getInstance(context).getAlarmDao();
        try {
            TableUtils.clearTable(pillDao.getConnectionSource(), Pill.class);
            TableUtils.clearTable(alarmDao.getConnectionSource(), Alarm.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAlarm(Context context, Alarm alarm){
        RuntimeExceptionDao<Alarm, Integer> alarmDao = DatabaseHelper.getInstance(context).getAlarmDao();
        DeleteBuilder<Alarm,Integer> deleteBuilder = alarmDao.deleteBuilder();
        try {
            deleteBuilder.where().eq("id", alarm.getId());
            deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
