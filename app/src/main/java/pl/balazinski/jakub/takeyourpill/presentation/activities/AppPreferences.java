package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;
import pl.balazinski.jakub.takeyourpill.R;

/**
 * Created by Kuba on 13.03.2016.
 */
public class AppPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ButterKnife.bind(this);

        //TODO http://stackoverflow.com/questions/26564400/creating-a-preference-screen-with-support-v21-toolbar

        //Setting up notification bar color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));


    }

}