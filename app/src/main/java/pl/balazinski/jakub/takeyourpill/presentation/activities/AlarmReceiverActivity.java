package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

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
import pl.balazinski.jakub.takeyourpill.utilities.MyNotificationManager;
import pl.balazinski.jakub.takeyourpill.utilities.WakeLocker;


public class AlarmReceiverActivity extends Activity {

    private final String TAG = getClass().getSimpleName();

    private OutputProvider mOutputProvider;
    private MyNotificationManager myNotificationManager;
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
        myNotificationManager = new MyNotificationManager(mContext);
        mOutputProvider.displayLog(TAG, "ON CREATE");
        WakeLocker.acquire(mContext);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPillRemainingPercentage = Integer.parseInt(preferences.getString("percentage", "10"));

        if(mPillRemainingPercentage>= 1 && mPillRemainingPercentage<=99)
            mPillRemainingPercentage = mPillRemainingPercentage/100;
        else mPillRemainingPercentage = 0.1;

        setupContent(extras);

    }


    private void setupContent(Bundle extras) {
        if (extras != null) {
            mAlarmReceiver = new AlarmReceiver(getApplicationContext());
            mAlarmId = extras.getLong(Constants.EXTRA_LONG_ALARM_ID);
            int sneeze = extras.getInt(Constants.RECEIVER_NOTIFICATION_KEY);
            mOutputProvider.displayLog(TAG, "alarmID == " + String.valueOf(mAlarmId));
            mOutputProvider.displayLog(TAG, "sneeze == " + String.valueOf(extras.getInt(Constants.RECEIVER_NOTIFICATION_KEY)));
            if (mAlarmId != null) {
                if (sneeze == 0) {
                    sneezeClick();
                } else if (sneeze == 1) {
                    setupAlarmAndPill(mAlarmId);
                    takePillClick();
                } else if (sneeze == -1) {
                    clearNotification(mAlarmId);
                    mAlertMessage = setupAlarmAndPill(mAlarmId);
                    mAlarmReceiver.startRingtone(mContext);
                    setupView();
                }
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
                })
                .setNegativeButton(getString(R.string.snooze), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sneezeClick();
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
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
     * Stops ringtone and vibration and sets snooze alarm with
     * provided alarm id for snooze time which is set in preferences
     */
    private void sneezeClick() {
        if (mAlarmReceiver != null) {
            mOutputProvider.displayLog(TAG, "Sneeze clicked!");
            clearNotification(mAlarmId);
            mAlarmReceiver.stopRingtone();
            mAlarmReceiver.setSnoozeAlarm(mContext, mAlarmId);
            finish();
        }
    }

    /**
     * Used when user clicks button on notification when mAlarm is fired
     */
    private void takePillClick() {
        mOutputProvider.displayLog(TAG, "Take pill clicked!");
        if (mAlarmReceiver != null)
            mAlarmReceiver.stopRingtone();

        clearNotification(mAlarmId);
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
                            myNotificationManager.sendPillNotification(pill.getId(), pill.getName());
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
                    mOutputProvider.displayShortToast(getString(R.string.toast_alarm_usage_used));
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

    private void clearNotification(Long id) {
        String notificationService = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(notificationService);
        notificationManager.cancel(longToInt(id));
    }

}
