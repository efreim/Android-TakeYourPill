package pl.balazinski.jakub.takeyourpill.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmReceiverActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.MainActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.MapsActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillDetailActivity;

/**
 * Created by Kuba on 2016-03-25.
 */
public class MyNotificationManager {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private OutputProvider mOutputProvider;

    public MyNotificationManager(Context context) {
        this.mContext = context;
        mOutputProvider = new OutputProvider(mContext);
    }

    /**
     * @param id  alarm id
     * @param msg alarm name
     */
    public void sendAlarmHeadsUpNotification(long id) {
        mOutputProvider.displayLog(TAG, "Heads up notification opened");
        Alarm alarm = DatabaseRepository.getAlarmById(mContext, id);
        if (alarm != null) {
            android.app.NotificationManager alarmNotificationManager = (android.app.NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            SharedPreferences getAlarms = PreferenceManager.getDefaultSharedPreferences(mContext);
            String ringtone = getAlarms.getString("ringtone", "content://settings/system/alarm_alert");
            boolean isVibration = getAlarms.getBoolean("vibration", false);


            PendingIntent sneezePendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), setupIntent(alarm.getId(), 0), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent takePillPendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), setupIntent(alarm.getId(), 1), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent mainIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), setupIntent(alarm.getId(), -1), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
                    mContext).setContentTitle("Take you pill!").setSmallIcon(R.drawable.pill)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(""))
                    .setContentText(setupAlarmAndPill(id))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setSound(Uri.parse(ringtone))
                    .setAutoCancel(false)
                    .addAction(R.drawable.ic_snooze_black_36dp, mContext.getString(R.string.snooze), sneezePendingIntent)
                    .addAction(R.drawable.ic_check_black_36dp, mContext.getString(R.string.take_pill), takePillPendingIntent);

            long[] pattern;
            if (isVibration)
                pattern = new long[]{0, 200, 2000};
            else
                pattern = new long[]{0};

            alarmNotificationBuilder.setVibrate(pattern);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmNotificationBuilder.setCategory(Notification.CATEGORY_REMINDER);
            }

            alarmNotificationBuilder.setContentIntent(mainIntent);
            Notification notification = alarmNotificationBuilder.build();
            notification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_ONGOING_EVENT;
            alarmNotificationManager.notify(longToInt(id), notification);

            mOutputProvider.displayLog(TAG, "Heads up notification sent.");
        }
    }

    /**
     * Sends notification that opens in-app map with nearby pharmacies.
     * Used only when remaining pill count is below given percentage of full pill count
     *
     * @param id pill id
     */
    public void sendAlarmNotification(long id) {
        Alarm alarm = DatabaseRepository.getAlarmById(mContext, id);
        if (alarm != null) {
            android.app.NotificationManager alarmNotificationManager = (android.app.NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent sneezePendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), setupIntent(alarm.getId(), 0), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent takePillPendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), setupIntent(alarm.getId(), 1), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent mainIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), setupIntent(alarm.getId(), -1), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
                    mContext).setContentTitle("Take you pill!").setSmallIcon(R.drawable.pill)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(""))
                    .setContentText(setupAlarmAndPill(id))
                    .setAutoCancel(false)
                    .addAction(R.drawable.ic_snooze_black_36dp, mContext.getString(R.string.snooze), sneezePendingIntent)
                    .addAction(R.drawable.ic_check_black_36dp, mContext.getString(R.string.take_pill), takePillPendingIntent);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmNotificationBuilder.setCategory(Notification.CATEGORY_REMINDER);
            }

            alarmNotificationBuilder.setContentIntent(mainIntent);
            Notification notification = alarmNotificationBuilder.build();
            notification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_ONGOING_EVENT;
            alarmNotificationManager.notify(longToInt(id), notification);

            mOutputProvider.displayLog(TAG, "Notification sent.");
        }
    }

    /**
     * Sends notification that opens in-app map with nearby pharmacies.
     * Used only when remaining pill count is below given percentage of full pill count
     *
     * @param id  pill id
     * @param msg pill name
     */
    public void sendPillNotification(Long id, String msg) {
        Pill pill = DatabaseRepository.getPillByID(mContext, id);
        if (pill != null) {
            NotificationManager alarmNotificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent mapsIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(),
                    new Intent(mContext, MapsActivity.class), 0);

            Intent intent = new Intent(mContext, PillDetailActivity.class);
            intent.putExtra(Constants.EXTRA_LONG_ID, pill.getId());

            PendingIntent refillIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent, 0);

            PendingIntent mainIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), new Intent(mContext, MainActivity.class), 0);
            NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
                    mContext).setContentTitle(mContext.getString(R.string.find_nearby_pharmacy)).setSmallIcon(R.drawable.pill)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(mContext.getString(R.string.low_on) + msg))
                    .setContentText(mContext.getString(R.string.low_on) + msg)
                    .addAction(R.drawable.ic_room_black_36dp, mContext.getString(R.string.find_pharmacy), mapsIntent)
                    .addAction(R.drawable.ic_autorenew_black_36dp, mContext.getString(R.string.refill_pill), refillIntent);

            alarmNotificationBuilder.setContentIntent(mainIntent);
            alarmNotificationManager.notify(longToInt(id), alarmNotificationBuilder.build());


            mOutputProvider.displayLog(TAG, "Pill notification sent.");
        }
    }

    /**
     * Converts Long value to int value.
     *
     * @param l Long value we want to transform.
     * @return Transformed int value.
     */
    private int longToInt(Long l) {
        return (int) (long) l;
    }

    /**
     * Setup intent for pending intent
     *
     * @param alarmId  id of alarm
     * @param extraInt 0 is sneeze, 1 is take pill
     * @return intent with given extras
     */
    public Intent setupIntent(Long alarmId, int extraInt) {
        mOutputProvider.displayLog(TAG, "setupIntent, extraInt = " + String.valueOf(extraInt));
        Intent intent = new Intent(mContext, AlarmReceiverActivity.class);
        intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmId);
        intent.putExtra(Constants.RECEIVER_NOTIFICATION_KEY, extraInt);
        return intent;
    }


    /**
     * @param alarmId id of mAlarm that fired.
     * @return Built string for alert dialog window text
     */
    private String setupAlarmAndPill(Long alarmId) {
        List<Long> mPillIdList = new ArrayList<>();
        if (alarmId != null) {
            mPillIdList = DatabaseRepository.getPillsByAlarm(mContext, alarmId);
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (!mPillIdList.isEmpty()) {
            for (Long pillId : mPillIdList) {
                Pill pill = DatabaseRepository.getPillByID(mContext, pillId);
                if (pill != null) {
                    stringBuilder.append(pill.getName());
                    if (!Objects.equals(pillId, mPillIdList.get(mPillIdList.size() - 1)))
                        stringBuilder.append(", ");
                }
            }
        } else
            stringBuilder.append(mContext.getString(R.string.no_pill_attached_to_alarm));

        return stringBuilder.toString();
    }
}
