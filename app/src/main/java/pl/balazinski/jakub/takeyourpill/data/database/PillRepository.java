package pl.balazinski.jakub.takeyourpill.data.database;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.List;

import pl.balazinski.jakub.takeyourpill.data.Pill;


public class PillRepository {

    public static List<Pill> getAllPills(Context context) {
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getDao();
        return dao.queryForAll();
    }

    public static void addPill(Context context, Pill pill) {
        RuntimeExceptionDao<Pill, Integer> dao = DatabaseHelper.getInstance(context).getDao();
        dao.create(pill);
    }
}
