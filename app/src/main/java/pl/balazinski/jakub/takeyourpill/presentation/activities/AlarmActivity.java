package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.OuterPillDatabase;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.IntervalAlarmFragment;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.RepeatingAlarmFragment;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.SingleAlarmFragment;

public class AlarmActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    public enum State {
        NEW, EDIT
    }

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;

    @Bind(R.id.repeatable_checkbox)
    CheckBox repeatableCheckbox;
    @Bind(R.id.interval_checkbox)
    CheckBox intervalCheckbox;
    @Bind(R.id.single_checkbox)
    CheckBox singleCheckbox;

    @Bind(R.id.repeatable_relative)
    RelativeLayout repeatableRelative;
    @Bind(R.id.interval_relative)
    RelativeLayout intervalRelative;
    @Bind(R.id.single_relative)
    RelativeLayout singleRelative;

    @Bind(R.id.add_alarm)
    Button addAlarm;


    private State state;
    private IntervalAlarmFragment intervalFragment;
    private RepeatingAlarmFragment repeatableFragment;
    private SingleAlarmFragment singleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        OutputProvider outputProvider = new OutputProvider(this);

        /*
         * If extras are empty state is new otherwise
         * state is edit and edited pill must be loaded
         * from database.
         */
        repeatableFragment = new RepeatingAlarmFragment();
        intervalFragment = new IntervalAlarmFragment();
        singleFragment = new SingleAlarmFragment();
        if (extras == null) {
            state = AlarmActivity.State.NEW;
            intervalRelative.setVisibility(View.VISIBLE);
            repeatableRelative.setVisibility(View.VISIBLE);
            singleRelative.setVisibility(View.VISIBLE);
            repeatableCheckbox.setChecked(true);
            addAlarm.setText("Add alarm");
        } else {
            addAlarm.setText("Edit alarm");
            intervalRelative.setVisibility(View.INVISIBLE);
            repeatableRelative.setVisibility(View.INVISIBLE);
            singleRelative.setVisibility(View.INVISIBLE);
            state = AlarmActivity.State.EDIT;
            Long id = extras.getLong(Constants.EXTRA_LONG_ID);

            Alarm mAlarm = DatabaseRepository.getAlarmById(this, id);
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.EXTRA_LONG_ID, id);


            if (mAlarm == null)
                outputProvider.displayShortToast("Error loading pills");
            else {
                if (mAlarm.isRepeatable()) {
                    repeatableFragment.setArguments(bundle);
                    repeatableCheckbox.setChecked(true);
                } else if (mAlarm.isInterval()) {
                    intervalFragment.setArguments(bundle);
                    intervalCheckbox.setChecked(true);
                } else if (mAlarm.isSingle()) {
                    singleFragment.setArguments(bundle);
                    singleCheckbox.setChecked(true);
                }
            }

        }

         /*
         * Setting up notification bar color:
         * 1. Clear FLAG_TRANSLUCENT_STATUS flag
         * 2. Add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
         * 3. Change the color
         */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            addAlarm.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));

        } else {
            addAlarm.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
    }


    @OnCheckedChanged(R.id.repeatable_checkbox)
    public void onRepeatableChecked(boolean checked) {
        if (checked) {
            getSupportFragmentManager().popBackStack();
            intervalCheckbox.setChecked(false);
            singleCheckbox.setChecked(false);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, repeatableFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            intervalCheckbox.setChecked(true);
        }

    }

    @OnCheckedChanged(R.id.interval_checkbox)
    public void onIntervalChecked(boolean checked) {
        if (checked) {
            getSupportFragmentManager().popBackStack();
            repeatableCheckbox.setChecked(false);
            singleCheckbox.setChecked(false);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, intervalFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            singleCheckbox.setChecked(true);
        }
    }

    @OnCheckedChanged(R.id.single_checkbox)
    public void onSingleChecked(boolean checked) {
        if (checked) {
            getSupportFragmentManager().popBackStack();
            repeatableCheckbox.setChecked(false);
            intervalCheckbox.setChecked(false);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, singleFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            repeatableCheckbox.setChecked(true);
        }
    }


    @OnClick(R.id.add_alarm)
    public void addAlarmButton(View v) {
        boolean isOk = false;
        if (repeatableCheckbox.isChecked()) {
            isOk = repeatableFragment.addAlarm(state);
        } else if (intervalCheckbox.isChecked()) {
            isOk = intervalFragment.addAlarm(state);
        } else if (singleCheckbox.isChecked()) {
            isOk = singleFragment.addAlarm(state);
        }

        if (isOk)
            finish();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

}
