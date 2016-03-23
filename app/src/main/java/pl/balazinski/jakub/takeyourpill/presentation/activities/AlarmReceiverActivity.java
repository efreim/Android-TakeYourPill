package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Window;
import android.view.WindowManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.utilities.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.utilities.WakeLocker;


public class AlarmReceiverActivity extends Activity {

    private final String TAG = getClass().getSimpleName();

    private OutputProvider mOutputProvider;
    private Context mContext;
    private AlarmReceiver mAlarmReceiver;
    private Alarm mAlarm;
    private List<Long> mPillIdList;
    private Long mAlarmId;
    private String mAlertMessage;
    private double mPillRemainingPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        Bundle extras = getIntent().getExtras();
        mOutputProvider = new OutputProvider(mContext);
        WakeLocker.acquire(mContext);

        //TODO this be get from shared preferences set in preferences activity
        mPillRemainingPercentage = 0.1;

        setupContent(extras);
        setupView();
    }

    private void setupContent(Bundle extras) {
        if (extras != null) {
            mAlarmId = extras.getLong(Constants.EXTRA_LONG_ALARM_ID);
            mOutputProvider.displayLog(TAG, "alarmID == " + String.valueOf(mAlarmId));
            if (mAlarmId != null) {
                mAlertMessage = setupAlarmAndPill(mAlarmId);
            }
        }
    }

    private void setupView() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(AlarmReceiverActivity.this);
        alertDialogBuilder.setTitle(getString(R.string.dialog_title));
        alertDialogBuilder.setMessage(mAlertMessage)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        takePillClick();
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    /**
     * @param alarmId id of mAlarm that fired.
     * @return Built string for alert dialog window text
     */
    private String setupAlarmAndPill(Long alarmId) {
        mAlarmReceiver = new AlarmReceiver(getApplicationContext());
        mAlarm = DatabaseRepository.getAlarmById(getApplicationContext(), alarmId);
        mPillIdList = new ArrayList<>();
        if (alarmId != null) {
            mPillIdList = DatabaseRepository.getPillsByAlarm(this, alarmId);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.dialog_message));
        for (Long pillId : mPillIdList) {
            Pill pill = DatabaseRepository.getPillByID(getApplicationContext(), pillId);
            if (pill != null) {
                stringBuilder.append(pill.getName());
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * Used when user clicks button on notification when mAlarm is fired
     */
    public void takePillClick() {
        mAlarmReceiver.stopRingtone();

        //Checking pills for count and pill dosage to update pills remaining and send notification
        if (!mPillIdList.isEmpty()) {
            for (Long pillId : mPillIdList) {
                //Getting pill for each pill attached to mAlarm
                Pill pill = DatabaseRepository.getPillByID(getApplicationContext(), pillId);
                int pillRemaining, pillDosage, pillCount;

                if (pill != null) {
                    //get pill info
                    pillRemaining = pill.getPillsRemaining();
                    pillDosage = pill.getDosage();
                    pillCount = pill.getPillsCount();
                    //if pill dosage was set in pill
                    if (pillRemaining != -1 && pillDosage != -1) {
                        //count how many pills are left
                        int remaining = pillRemaining - pillDosage;

                        //set remaining pills
                        if (remaining < 0) {
                            //number of pills remaining cannot be negative number
                            remaining = 0;
                            pill.setPillsRemaining(remaining);
                            DatabaseHelper.getInstance(getApplicationContext()).getPillDao().update(pill);
                        } else {
                            pill.setPillsRemaining(remaining);
                            DatabaseHelper.getInstance(getApplicationContext()).getPillDao().update(pill);
                        }
                        //count percentage of pills left to send notification
                        if (remaining <= (pillCount * mPillRemainingPercentage)) {
                            sendNotification(longToInt(pill.getId()), pill.getName());
                        }
                        mOutputProvider.displayLog(TAG, "pill taken. id = " + pill.getId() + "  name: " + pill.getName() + "  pills left: " + pill.getPillsRemaining());
                    }
                }

            }
        }

        //Checking number of usage set in mAlarm
        if (mAlarm != null) {
            int usageNumber = mAlarm.getUsageNumber();
            if (usageNumber != -1) {
                //if usage number IS NOT -1 mAlarm is repeating until usage number is 0 (this is done in repeating and interval alarms)
                usageNumber--;

                mAlarm.setUsageNumber(usageNumber);
                DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(mAlarm);

                if (usageNumber > 0) {
                    //usage number is greater than 0 so next mAlarm can be set (interval alarms are set automatically)
                    if (mAlarm.isRepeatable()) {
                        mAlarmReceiver.cancelAlarm(getApplicationContext(), mAlarmId);
                        mAlarmReceiver.setRepeatingAlarm(getApplicationContext(), mAlarmId);
                    }

                } else if (usageNumber == 0) {
                    //usage number is 0 so repeating and interval alarms are canceled and set to false
                    mOutputProvider.displayShortToast("Alarm usage used");
                    mAlarmReceiver.cancelAlarm(getApplicationContext(), mAlarmId);
                    mAlarm.setIsActive(false);
                    DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(mAlarm);
                }

            } else {
                //usage number == -1
                if (mAlarm.isSingle()) {
                    //cancelling and deleting single mAlarm after one and only usage
                    mAlarmReceiver.cancelAlarm(mContext, mAlarmId);
                    DatabaseRepository.deleteAlarm(mContext, mAlarm);
                }
                if (mAlarm.isRepeatable()) {
                    //if usage number is -1 mAlarm is repeating infinitely (works the same for interval alarms)
                    mAlarmReceiver.cancelAlarm(mContext, mAlarmId);
                    mAlarmReceiver.setRepeatingAlarm(mContext, mAlarmId);
                }
            }
        }

        //Starting main activity and releasing locked screen
        startActivity(new Intent(mContext, MainActivity.class));
        WakeLocker.release();
    }


    /**
     * Sends notification that opens in-app map with nearby pharmacies.
     * Used only when remaining pill count is below given percentage of full pill count
     *
     * @param id  pill id
     * @param msg pill name
     */
    private void sendNotification(int id, String msg) {
        Pill pill = DatabaseRepository.getPillByID(getApplicationContext(), (long) id);
        if (pill != null) {
            NotificationManager alarmNotificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent mapsIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MapsActivity.class), 0);

            Intent intent = new Intent(this, PillDetailActivity.class);
            intent.putExtra(Constants.EXTRA_LONG_ID, pill.getId());
            PendingIntent refillIntent = PendingIntent.getActivity(this, 0, intent, 0);

            PendingIntent mainIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
                    this).setContentTitle(getString(R.string.find_nearby_pharmacy)).setSmallIcon(R.drawable.pill)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.low_on) + msg))
                    .setContentText(getString(R.string.low_on) + msg)
                    .addAction(R.drawable.ic_room_black_36dp, "Find pharmacy", mapsIntent)
                    .addAction(R.drawable.ic_autorenew_black_36dp, "Refill me", refillIntent);

            alarmNotificationBuilder.setContentIntent(mainIntent);
            alarmNotificationManager.notify(id, alarmNotificationBuilder.build());


            mOutputProvider.displayLog(TAG, "Notification sent.");
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
     * Method used to get through locked screen
     */
    public void onAttachedToWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

}
