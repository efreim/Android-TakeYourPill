package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.utilities.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.utilities.WakeLocker;


public class AlarmReceiverActivity extends Activity {

    private final String TAG = getClass().getSimpleName();

    private OutputProvider outputProvider;
    private Context context;
    private AlarmReceiver alarmReceiver;
    private Alarm alarm;
    private List<Long> pillIds;
    private Long alarmId = null;
    private NotificationManager alarmNotificationManager;
    private double pillLeftPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        WakeLocker.acquire(context);
        outputProvider = new OutputProvider(context);
        pillLeftPercentage = 0.1;

        String s = " ";
        Intent intent = getIntent();
        if (intent != null) {
            alarmId = intent.getLongExtra("alarmID", -1);
            outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmId));
            if (alarmId != -1) {
                s = setupAlarmAndPill(alarmId);
            }
        }

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(AlarmReceiverActivity.this);
        alertDialogBuilder.setTitle("Take your pill");
        alertDialogBuilder.setMessage(s)
                .setCancelable(false)
                .setNeutralButton("Cancel alarm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelAlarm(alarmId);
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelPillClick();
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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

    public void onAttachedToWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private String setupAlarmAndPill(Long alarmId) {

        alarmReceiver = new AlarmReceiver(getApplicationContext());
        alarm = DatabaseRepository.getAlarmById(getApplicationContext(), alarmId);
        pillIds = new ArrayList<>();
        if (alarmId != null) {
            pillIds = DatabaseRepository.getPillsByAlarm(this, alarmId);
        }


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Did you take your pills?\n");
        for (Long pillId : pillIds) {
            Pill pill = DatabaseRepository.getPillByID(getApplicationContext(), pillId);
            if (pill != null) {
                stringBuilder.append(pill.getName());
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public void takePillClick() {
        AlarmReceiver.stopRingtone();
        StringBuilder stringBuilder = new StringBuilder();
        boolean wasTaken = false;
        if (!pillIds.isEmpty()) {
            for (Long pillId : pillIds) {
                Pill pill = DatabaseRepository.getPillByID(getApplicationContext(), pillId);

                int pillRemaining = -1, pillDosage = -1, pillCount = -1;
                if (pill != null) {
                    pillRemaining = pill.getPillsRemaining();
                    pillDosage = pill.getDosage();
                    pillCount = pill.getPillsCount();
                    if (pillRemaining != -1 && pillDosage != -1) {
                        int remaining = pillRemaining - pillDosage;
                        if(remaining<0) {
                            remaining = 0;
                            pill.setPillsRemaining(remaining);
                            DatabaseHelper.getInstance(getApplicationContext()).getPillDao().update(pill);
                        }
                        if(pillRemaining <= (pillCount*pillLeftPercentage))
                            stringBuilder.append(pill.getName() + " ");
                        outputProvider.displayLog(TAG, "pill taken. id = " + pill.getId() + "  name: " + pill.getName() + "  pills left: " + pill.getPillsRemaining());
                        wasTaken = true;
                    }
                }

            }
            if (alarm != null) {
                int usageNumber = alarm.getUsageNumber();
                if (usageNumber != -1) {
                    usageNumber--;
                    alarm.setUsageNumber(usageNumber);
                    DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(alarm);

                    if (usageNumber > 0) {

                        if (alarm.isRepeatable()) {
                            alarmReceiver.cancelAlarm(getApplicationContext(), alarmId);
                            alarmReceiver.setRepeatingAlarm(getApplicationContext(), alarmId);
                        }
                    } else if (usageNumber == 0) {
                        outputProvider.displayShortToast("Alarm usage used");
                        alarmReceiver.cancelAlarm(getApplicationContext(), alarmId);
                        alarm.setIsActive(false);
                        DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(alarm);
                    }

                } else {
                    if (alarm.isSingle()) {
                        alarm.setIsActive(false);
                        DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(alarm);
                    }
                    if (alarm.isRepeatable()) {
                        alarmReceiver.cancelAlarm(getApplicationContext(), alarmId);
                        alarmReceiver.setRepeatingAlarm(getApplicationContext(), alarmId);
                    }
                }
            }
        }
        if(wasTaken)
            sendNotification(stringBuilder.toString());
        startActivity(new Intent(context, MainActivity.class));
        WakeLocker.release();
    }

    public void cancelPillClick() {
        AlarmReceiver.stopRingtone();

        if (alarm != null) {
            int usageNumber = alarm.getUsageNumber();
            if (usageNumber != -1) {
                usageNumber--;
                alarm.setUsageNumber(usageNumber);
                DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(alarm);

                if (usageNumber > 0) {

                    if (alarm.isRepeatable()) {
                        alarmReceiver.cancelAlarm(getApplicationContext(), alarmId);
                        alarmReceiver.setRepeatingAlarm(getApplicationContext(), alarmId);
                    }
                } else if (usageNumber == 0) {
                    outputProvider.displayShortToast("Alarm usage used");
                    alarmReceiver.cancelAlarm(getApplicationContext(), alarmId);
                    alarm.setIsActive(false);
                    DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(alarm);
                }

            } else {
                if (alarm.isSingle()) {
                    alarm.setIsActive(false);
                    DatabaseHelper.getInstance(getApplicationContext()).getAlarmDao().update(alarm);
                }
                if (alarm.isRepeatable()) {
                    alarmReceiver.cancelAlarm(getApplicationContext(), alarmId);
                    alarmReceiver.setRepeatingAlarm(getApplicationContext(), alarmId);
                }
            }
        }
        // cancel the alert box and put a Toast to the user
        outputProvider.displayShortToast("You didn't take your pill :(");

    //    sendNotification("JUST A TEST");
        startActivity(new Intent(context, MainActivity.class));
        WakeLocker.release();
    }

    private void cancelAlarm(Long alarmId){
        AlarmReceiver.stopRingtone();
        alarmReceiver.cancelAlarm(context, alarmId);
        startActivity(new Intent(context, MainActivity.class));
    }


    //TODO Create notification class for creating,updating and deleting notification
    private void sendNotification(String msg) {
        alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MapsActivity.class), 0);

        NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
                this).setContentTitle("Find nearby pharmacy!").setSmallIcon(R.drawable.pill)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("You are low on: " + msg))
                .setContentText("You are low on: " + msg);


        alarmNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(1, alarmNotificationBuilder.build());
        Log.d("AlarmReceiverActivity", "Notification sent.");
    }

}
