package pl.balazinski.jakub.takeyourpill.data.database;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.data.Pill;


public class PillRepository {

    public static List<Pill> getAllPills(Context context) {
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getDao();
//        QueryBuilder<Pill, Integer> qb = dao.queryBuilder();
//        qb.orderBy("name", true);
        return dao.queryForAll();
    }

    public static void addPill(Context context, Pill pill) {
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getDao();
        dao.create(pill);
//        QueryBuilder<Pill, Integer> qb = dao.queryBuilder();
//        qb.orderBy("name", true);
    }

    public static void deleteDatabase(Context context){
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getDao();
        try {
            TableUtils.clearTable(dao.getConnectionSource(), Pill.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
