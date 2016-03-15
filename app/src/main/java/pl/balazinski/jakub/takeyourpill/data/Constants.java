package pl.balazinski.jakub.takeyourpill.data;

import android.os.Build;

/**
 * Storage for statics constants
 */
public class Constants {

    //CAMERA
    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;
    public static final String SCAN_BARCODE_RESULT = "result";
    public static final String ADD_BARCODE_MANUALLY = "addBarcodeManually";
    //IDs
    public static final String EXTRA_LONG_ID = "id";
    public static final String EXTRA_LONG_ALARM_ID = "alarmID";

    //FRAGMENTS
    public static final String ALARM_FRAGMENT = "alarm";
    public static final String PILL_FRAGMENT = "pill";
    public static final Integer ALARM_FRAGMENT_VALUE = 0;
    public static final Integer PILL_FRAGMENT_VALUE = 1;


    public static final String MAIN_PACKAGE_NAME = "pl.balazinski.jakub.takeyourpill";
    public static final String MAIN_ACTIVITY_NAME = "pl.balazinski.jakub.takeyourpill.presentation.activities.MainActivity";
    public static final String RECEIVER_ACTIVITY_NAME = "pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmReceiverActivity";

    public static final String MAIN_FROM_ALARM_KEY = "RETURNING_KEY";
    public static final String MAIN_FROM_ALARM = "RETURNING";

    //DATABASE
    public static final String OUTER_TABLE_NAME = "drugtable";
    public static final String DATABASE_NAME = "takeyourpill4.db";
    public static final int DATABASE_VERSION = 1;
    public static final String OUTER_DATABASE_NAME = "drugs.db";
    public static final int OUTER_DATABASE_VERSION = 1;

    //SYSTEM
    public static final int VERSION = Build.VERSION.SDK_INT;

    //SYSTEM
    public static final String DRAWABLE_PATH = "android.resource://pl.balazinski.jakub.takeyourpill/";

    //INTERNET
    public static final String SEARCH_PILL_WEBSITE = "http://www.bazalekow.mp.pl/leki/szukaj.html?item_name=";

    //DAYS
    public static final String MONDAY = "Mon";
    public static final String TUESDAY = "Tue";
    public static final String WEDNESDAY = "Wed";
    public static final String THURSDAY = "Thu";
    public static final String FRIDAY = "Fri";
    public static final String SATURDAY = "Sat";
    public static final String SUNDAY = "Sun";

}
