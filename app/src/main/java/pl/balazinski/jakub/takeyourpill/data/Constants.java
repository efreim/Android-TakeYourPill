package pl.balazinski.jakub.takeyourpill.data;

/**
 * Storage for statics constants
 */
public class Constants {

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;

    public static final String DATABASE_NAME = "takeyourpill.db";
    public static final int DATABASE_VERSION = 14;

    public static final String EXTRA_LONG_ID = "id";

    public static final String ALARM_FRAGMENT = "alaram";
    public static final String PILL_FRAGMENT = "pill";

    public static final Integer ALARM_FRAGMENT_VALUE = 0;
    public static final Integer PILL_FRAGMENT_VALUE = 1;

    public static final String TABLE_NAME = "drugtable";

    public static final String MAIN_PACKAGE_NAME = "pl.balazinski.jakub.takeyourpill";
    public static final String MAIN_ACTIVITY_NAME = "pl.balazinski.jakub.takeyourpill.presentation.activities.MainActivity";

    public static final String MAIN_FROM_ALARM_KEY = "RETURNING_KEY";
    public static final String MAIN_FROM_ALARM = "RETURNING";
}
