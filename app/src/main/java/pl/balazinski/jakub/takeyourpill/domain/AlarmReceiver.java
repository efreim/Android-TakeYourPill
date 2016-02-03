package pl.balazinski.jakub.takeyourpill.domain;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;

import pl.balazinski.jakub.takeyourpill.data.Constants;

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
        i.putExtra("id", bundle.getLong("id"));
        Log.i("RECIEVER ID", String.valueOf(bundle.getLong("id")));
        context.startActivity(i);

        //this will send a notification message
        /*ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);*/
    }

    public void setAlarm(Context context, Calendar calendar, Long id)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(id), intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public void cancelAlarm(Context context, Long id)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, longToInt(id), intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public static void stopRingtone() {
        mRingtone.stop();
    }

    private int longToInt(Long l){
        return (int)(long) l;
    }
}
