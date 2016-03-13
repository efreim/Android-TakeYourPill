package pl.balazinski.jakub.takeyourpill.utilities;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmReceiverActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.MainActivity;

/**
 * Created by Kuba on 2016-02-01.
 */
public class AlarmService extends IntentService {


    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        //sendNotification("Wake Up! Wake Up!");
        Bundle bundle = intent.getExtras();
        intent.putExtra("alarmID", bundle.getLong("alarmID"));
        Intent i = new Intent(this, AlarmReceiverActivity.class);
        i.putExtra("alarmID", bundle.getLong("alarmID"));
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
