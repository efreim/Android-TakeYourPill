package pl.balazinski.jakub.takeyourpill.utilities;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

public class AlarmService extends IntentService {

    private final String TAG = getClass().getSimpleName();

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        OutputProvider mOutputProvider = new OutputProvider(context);
        MyNotificationManager myNotificationManager = new MyNotificationManager(context);

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Long id = bundle.getLong(Constants.EXTRA_LONG_ALARM_ID);
            KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            //If screen is turned off (locked) send notification and display dialog.
            if (keyguardManager.inKeyguardRestrictedInputMode()) {
                //mOutputProvider.displayDebugLog(TAG, "IS LOCKED");
                intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, id);
                Intent i = myNotificationManager.setupIntent(id, -1);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                myNotificationManager.sendAlarmNotification(id);
            } else {
                //if screen is on display heads up notification
                //mOutputProvider.displayDebugLog(TAG, "NOT LOCKED");
                myNotificationManager.sendAlarmHeadsUpNotification(id);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
