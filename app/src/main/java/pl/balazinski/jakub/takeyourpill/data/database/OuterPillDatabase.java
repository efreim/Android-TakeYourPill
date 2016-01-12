package pl.balazinski.jakub.takeyourpill.data.database;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Kuba on 12.01.2016.
 */
public class OuterPillDatabase extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "drugs.db";
    private static final int DATABASE_VERSION = 1;

    public OuterPillDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
