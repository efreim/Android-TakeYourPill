package pl.balazinski.jakub.takeyourpill.data.database;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import pl.balazinski.jakub.takeyourpill.utilities.Constants;

/**
 * Load medicines database from file.
 */
public class OuterPillDatabase extends SQLiteAssetHelper {

    public OuterPillDatabase(Context context) {
        super(context, Constants.OUTER_DATABASE_NAME, null, Constants.OUTER_DATABASE_VERSION);
    }
}
