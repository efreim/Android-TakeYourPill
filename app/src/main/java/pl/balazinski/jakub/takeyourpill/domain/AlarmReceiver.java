package pl.balazinski.jakub.takeyourpill.domain;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Created by Kuba on 2016-02-01.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    private static Ringtone mRingtone = null;
    private OutputProvider outputProvider;

    @Override
    public void onReceive(final Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        //this will sound the alarm tone
        //this will sound the alarm once, if you wish to
        //raise alarm in loop continuously then use MediaPlayer and setLooping(true)
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        mRingtone = RingtoneManager.getRingtone(context, alarmUri);
        mRingtone.play();

        Intent i = new Intent();
        i.setClassName(Constants.MAIN_PACKAGE_NAME, Constants.MAIN_ACTIVITY_NAME);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //i.putExtra(Constants.MAIN_FROM_ALARM_KEY, Constants.MAIN_FROM_ALARM);
        i.putExtra("alarmID", bundle.getLong("alarmID"));
        context.startActivity(i);

        //this will send a notification message
        /*ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);*/
    }

    public void setRepeatingAlarm(Context context, Long alarmID) {
        outputProvider = new OutputProvider(context);

        boolean isSingleAlarm = true;
        List<Long> daysList = new ArrayList<>();
        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            alarm.setIsActive(true);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
        }

        String daysOfWeek = alarm.getDaysRepeating();
        char[] daysOfWeekArray = daysOfWeek.toCharArray();
        for (int i = 0; i < daysOfWeekArray.length; i++)
            if (daysOfWeekArray[i] == '1')
                isSingleAlarm = false;

        Calendar now = Calendar.getInstance();
        outputProvider.displayLog(TAG, "NOW day = " + now.get(Calendar.DAY_OF_WEEK) + "; timeinMillis = " + now.getTimeInMillis() + ";  date: " + now.getTime());


        long alarmTimeInMillis = 0;

        if (isSingleAlarm) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());


            if (calendar.getTimeInMillis() <= now.getTimeInMillis())
                alarmTimeInMillis = calendar.getTimeInMillis() + (AlarmManager.INTERVAL_DAY + 1);
            else
                alarmTimeInMillis = calendar.getTimeInMillis();

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarmID", alarmID);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
            outputProvider.displayLog(TAG, "SINGLE ALARM day = " + calendar.get(Calendar.DAY_OF_WEEK) + "; timeinMillis = " + calendar.getTimeInMillis() + ";  date: " + calendar.getTime());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = formatter.format(new Date(alarmTimeInMillis));
            Toast.makeText(context, "Alarm will fire in: " + dateString, Toast.LENGTH_LONG).show();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            outputProvider.displayLog(TAG,"alarm.getHour = " + alarm.getHour() + ";  alarm.getMinute = " + alarm.getMinute());
            outputProvider.displayLog(TAG,"calendar HOUR = " + now.get(Calendar.HOUR_OF_DAY) + ";  calendar MINUTE = " + now.get(Calendar.MINUTE));
            for (int i = 0; i < daysOfWeekArray.length; i++) {
                if (daysOfWeekArray[i] == '1') {
                    if (i < 6) {
                        calendar.set(Calendar.DAY_OF_WEEK, i + 2);
                        if(i+2 == now.get(Calendar.DAY_OF_WEEK)) {
                            if (alarm.getHour() == now.get(Calendar.HOUR_OF_DAY)) {
                                if (alarm.getMinute() <= now.get(Calendar.MINUTE)) {
                                    weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
                                    calendar.set(Calendar.WEEK_OF_MONTH, ++weekOfMonth);
                                }
                            } else if (alarm.getHour() < now.get(Calendar.HOUR)) {
                                weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
                                calendar.set(Calendar.WEEK_OF_MONTH, ++weekOfMonth);
                            }
                        }else
                            --weekOfMonth;


                        if (calendar.get(Calendar.WEEK_OF_MONTH) < now.get(Calendar.WEEK_OF_MONTH))
                            calendar.set(Calendar.WEEK_OF_MONTH, ++weekOfMonth);
                        if (calendar.get(Calendar.MONTH) < now.get(Calendar.MONTH))
                            calendar.set(Calendar.MONTH, ++month);
                        if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR))
                            calendar.set(Calendar.YEAR, ++year);

                        daysList.add(calendar.getTimeInMillis());

                        if(i+2 == now.get(Calendar.DAY_OF_WEEK))
                            --weekOfMonth;
                            outputProvider.displayLog(TAG, "i = " + i + ";  day = " + (i + 2) + "; timeinMillis = " + calendar.getTimeInMillis() +
                                ";  date: " + calendar.getTime());

                    } else if (i == 6) {
                        calendar.set(Calendar.DAY_OF_WEEK, 1);
                        if (calendar.get(Calendar.WEEK_OF_MONTH) < now.get(Calendar.WEEK_OF_MONTH))
                            calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth++);
                        if (calendar.get(Calendar.MONTH) < now.get(Calendar.MONTH))
                            calendar.set(Calendar.MONTH, month++);
                        if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR))
                            calendar.set(Calendar.YEAR, year++);

                        daysList.add(calendar.getTimeInMillis());
                        outputProvider.displayLog(TAG, "i = " + i + ";  day = " + 1 + "; timeinMillis = " + calendar.getTimeInMillis() +
                                ";  date: " + calendar.getTime().toString());

                    }
                }
            }
            outputProvider.displayLog(TAG, "collections min = " + Collections.min(daysList));


           /* AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarmID", alarmID);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, Collections.min(daysList), pendingIntent);
            Log.i("setRepeatingAlarm", "alarmID == " + String.valueOf(alarmID));
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = formatter.format(new Date(Collections.min(daysList)));
            Toast.makeText(context, "Alarm will fire in: " + dateString, Toast.LENGTH_LONG).show();*/

        }
    }

    public void setIntervalAlarm(Context context, Long alarmID) {

        int interval = 1, day, month, year;
        Calendar calendar = Calendar.getInstance();
        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            alarm.setIsActive(true);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
            interval = alarm.getInterval();
            day = alarm.getDay();
            month = alarm.getMonth();
            year = alarm.getYear();


            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
        }


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmID", alarmID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * interval, pendingIntent);
        Log.i("setIntervalAlarm", "alarmID == " + String.valueOf(alarmID));
    }

    public void cancelAlarm(Context context, Long id) {
        Alarm alarm = DatabaseRepository.getAlarmById(context, id);
        if (alarm != null) {
            alarm.setIsActive(false);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
        }
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, longToInt(id), intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.i("cancelAlarm", "id == " + String.valueOf(id));

    }

    public static void stopRingtone() {
        mRingtone.stop();
    }

    private int longToInt(Long l) {
        return (int) (long) l;
    }
}
