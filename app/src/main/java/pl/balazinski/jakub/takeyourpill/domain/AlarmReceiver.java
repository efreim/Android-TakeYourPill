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

import java.util.Calendar;

import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;

/**
 * Created by Kuba on 2016-02-01.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static Ringtone mRingtone  = null;

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

    public void setAlarm(Context context, Calendar calendar, Long alarmID)
    {
        Calendar now = Calendar.getInstance();
        long alarmTimeInMillis = 0;

        if (calendar.getTimeInMillis() <= now.getTimeInMillis())
            alarmTimeInMillis = calendar.getTimeInMillis() + (AlarmManager.INTERVAL_DAY + 1);
        else
            alarmTimeInMillis = calendar.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmID", alarmID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
        Log.i("setAlarm", "alarmID == " + String.valueOf(alarmID));
    }

    public void cancelAlarm(Context context, Long id)
    {
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

    private int longToInt(Long l){
        return (int)(long) l;
    }
}
