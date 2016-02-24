package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Alarm;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

public class AlarmActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String TAG = getClass().getSimpleName();

    private enum State {
        NEW, EDIT
    }

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;

    @Bind(R.id.timePicker)
    TimePicker timePicker;

    @Bind(R.id.spinner)
    Spinner spinner;

    private Pill pill = null;
    private List<Pill> pills;
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



        setView(state);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        for (Pill p : pills) {
            if (p.getName().equals(item))
                pill = p;
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private void setView(State state){
        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        timePicker.setIs24HourView(true);


        spinner.setOnItemSelectedListener(this);

        List<String> names = new ArrayList<>();
        pills = DatabaseRepository.getAllPills(this);

        if(pills!=null) {
            for (Pill p : pills)
                names.add(p.getName());

            if(names.size()<1) {
                spinner.setEnabled(false);
                names.add("No pills detected");
            }else {
                names.add(0,"No pill chosen");
                spinner.setEnabled(true);
            }
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter);
            spinner.setPrompt("Select Pill");
        }else
            outputProvider.displayShortToast("Error loading pills");

        if(state == State.EDIT){
            Alarm alarm = null;
            int i = 0;
            if(mId!=null)
                alarm = DatabaseRepository.getAlarmById(this, mId);
            if(alarm!=null){
                for (Pill p : pills) {
                    i++;
                    if (alarm.getPillId() == p.getId())
                        spinner.setSelection(i);
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    timePicker.setHour(alarm.getHour());
                    timePicker.setMinute(alarm.getMinute());
                }else{
                    timePicker.setCurrentHour(alarm.getHour());
                    timePicker.setCurrentMinute(alarm.getMinute());
                }
            }else
                outputProvider.displayShortToast("Error loading alarm!");
        }else{
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
        Long pillId = null;

        if(pill!=null)
            pillId = pill.getId();

        Alarm alarm = new Alarm(hour, minute, pillId, true);
        DatabaseRepository.addAlarm(this, alarm);

        AlarmReceiver alarmReceiver = new AlarmReceiver();
        alarmReceiver.setAlarm(getApplicationContext(), calendar, pillId, alarm.getId());

        finish();
    }

}
