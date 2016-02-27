package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.PillToAlarm;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.views.HorizontalScrollViewItem;

public class AlarmActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private enum State {
        NEW, EDIT
    }

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;

    @Bind(R.id.timePicker)
    TimePicker timePicker;

    @Bind(R.id.inside_horizontal)
    GridLayout linearInsideHorizontal;

    @Bind(R.id.add_alarm)
    Button addAlarm;

    private List<Pill> pills;
    private List<HorizontalScrollViewItem> viewList;
    private OutputProvider outputProvider;
    private State state;
    Long mId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        outputProvider = new OutputProvider(this);

        /*
         * If extras are empty state is new otherwise
         * state is edit and edited pill must be loaded
         * from database.
         */

        if (extras == null) {
            state = State.NEW;
            setView(state);
        } else {
            state = State.EDIT;
            mId = extras.getLong(Constants.EXTRA_LONG_ID);
            setView(state);
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

    }


    private void setView(State state) {
        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        timePicker.setIs24HourView(true);

        viewList = new ArrayList<>();
        pills = DatabaseRepository.getAllPills(this);

        if (pills != null) {
            for (Pill p : pills) {
                HorizontalScrollViewItem item = new HorizontalScrollViewItem(getApplicationContext(), p.getPhoto(), p.getName(), p.getId());
                linearInsideHorizontal.addView(item);
                viewList.add(item);
            }
        } else
            outputProvider.displayShortToast("Error loading pills");

        if (state == State.EDIT) {
            addAlarm.setText("Update alarm");
            Alarm alarm = null;
            int i = 0;
            if (mId != null)
                alarm = DatabaseRepository.getAlarmById(this, mId);
            if (alarm != null) {
                List<Long> pillIds = DatabaseRepository.getPillsbyAlarm(getApplicationContext(), alarm.getId());
                for(Long id : pillIds){
                    getViewItem(id);
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    timePicker.setHour(alarm.getHour());
                    timePicker.setMinute(alarm.getMinute());
                } else {
                    timePicker.setCurrentHour(alarm.getHour());
                    timePicker.setCurrentMinute(alarm.getMinute());
                }
            } else {
                outputProvider.displayShortToast("Error loading alarm!");
            }
        } else {
            addAlarm.setText("Add alarm");
            Calendar c = Calendar.getInstance();
            if (Build.VERSION.SDK_INT >= 23) {
                timePicker.setHour(c.get(Calendar.HOUR_OF_DAY));
                timePicker.setMinute(c.get(Calendar.MINUTE));
            } else {
                timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
            }


        }

    }

    @OnClick(R.id.add_alarm)
    public void addAlarm(View v) {
        int hour = 0;
        int minute = 0;
        if (Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);



        Alarm alarm = new Alarm(hour, minute, true);
        DatabaseRepository.addAlarm(this, alarm);

        if(state == State.EDIT){
            DatabaseRepository.deleteAlarmToPill(getApplicationContext(), alarm.getId());
        }

        for (HorizontalScrollViewItem item : viewList) {
            if (item.isChecked()) {
                DatabaseRepository.addPillToAlarm(getApplicationContext(), new PillToAlarm(alarm.getId(), item.getPillId()));
            }
        }

        AlarmReceiver alarmReceiver = new AlarmReceiver();
        alarmReceiver.setAlarm(getApplicationContext(), calendar, alarm.getId());

        finish();
    }

    private void getViewItem(Long id){
        for(HorizontalScrollViewItem item : viewList){
            if(item.getPillId().equals(id))
                item.setClick();
        }
    }

}
