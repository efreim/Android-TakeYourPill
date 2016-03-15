package pl.balazinski.jakub.takeyourpill.utilities;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmReceiverActivity;

public class AlarmService extends IntentService {


    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, bundle.getLong(Constants.EXTRA_LONG_ALARM_ID));
        Intent i = new Intent(this, AlarmReceiverActivity.class);
        i.putExtra(Constants.EXTRA_LONG_ALARM_ID, bundle.getLong(Constants.EXTRA_LONG_ALARM_ID));
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
