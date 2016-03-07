package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.PillToAlarm;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmActivity;
import pl.balazinski.jakub.takeyourpill.presentation.views.DayOfWeekView;
import pl.balazinski.jakub.takeyourpill.presentation.views.HorizontalScrollViewItem;

/**
 * Created by Kuba on 02.03.2016.
 */
public class RepeatingAlarmFragment extends Fragment {

    private final String TAG = "REPEATING_ALARM_FRAGMENT";
    @Bind(R.id.inside_horizontal)
    GridLayout linearInsideHorizontal;
    @Bind(R.id.day_of_week_grid)
    GridLayout dayOfWeekGrid;
    @Bind(R.id.change_time_button)
    Button changeTimeButton;
    @Bind(R.id.number_of_usage)
    EditText numberOfUsageEditText;
    private ScrollView scrollView;
    private List<HorizontalScrollViewItem> pillViewList;
    private List<DayOfWeekView> weekViewListList;
    private Alarm mAlarm;
    private List<Pill> pills;
    private AlarmActivity.State state;
    private OutputProvider outputProvider;
    private Context context;
    private int mMinute = 0, mHour = 0, mNumberOfAlarms = 0;
    private Long alarmId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scrollView = (ScrollView) inflater.inflate(R.layout.fragment_repetable_alarm, container, false);
        context = getContext();
        outputProvider = new OutputProvider(context);
        ButterKnife.bind(this, scrollView);

        Bundle bundle = getArguments();
        if (bundle == null) {
            state = AlarmActivity.State.NEW;
            setupView(state);
        } else {
            state = AlarmActivity.State.EDIT;
            alarmId = bundle.getLong(Constants.EXTRA_LONG_ID);

            mAlarm = DatabaseRepository.getAlarmById(context, alarmId);
            if (mAlarm == null)
                outputProvider.displayShortToast("Error loading pills");
            else {
                setupView(state);
            }
        }

        return scrollView;
    }

    private void setupView(AlarmActivity.State state) {
        pillViewList = new ArrayList<>();
        weekViewListList = new ArrayList<>();
        pills = DatabaseRepository.getAllPills(context);
        numberOfUsageEditText.setText("100");

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            DayOfWeekView dayOfWeekView = new DayOfWeekView(context, dayOfWeek.getId(), dayOfWeek.getDay());
            dayOfWeekGrid.addView(dayOfWeekView);
            weekViewListList.add(dayOfWeekView);
        }

        if (pills != null) {
            for (Pill p : pills) {
                HorizontalScrollViewItem item = new HorizontalScrollViewItem(context, p.getPhoto(), p.getName(), p.getId());
                linearInsideHorizontal.addView(item);
                pillViewList.add(item);
            }
        } else
            outputProvider.displayShortToast("Error loading pills");


        if (state == AlarmActivity.State.NEW) {
            Calendar calendar = Calendar.getInstance();
            mMinute = calendar.get(Calendar.MINUTE);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            changeTimeButton.setText(buildString(mMinute,mHour));
        } else {
            //STATE EDIT
            mMinute = mAlarm.getMinute();
            mHour = mAlarm.getHour();
            changeTimeButton.setText(buildString(mMinute, mHour));

            List<Long> pillIds = DatabaseRepository.getPillsByAlarm(context, mAlarm.getId());
            for (Long id : pillIds) {
                getViewItem(id);
            }
            String daysOfWeek = mAlarm.getDaysRepeating();
            outputProvider.displayLog(TAG,"days of week:  " + daysOfWeek);
            char[] daysArray = daysOfWeek.toCharArray();

            for (int i = 0; i < daysArray.length; i++) {
                outputProvider.displayLog(TAG, "daysArray["+i+"] = " + daysArray[i]);
                if (daysArray[i] == '1')
                    weekViewListList.get(i).setClick();
            }

        }

    }

    @OnClick(R.id.change_time_button)
    public void onChangeTimeClick(View v) {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour;
        int minute;
        if (mAlarm != null) {
            hour = mAlarm.getHour();
            minute = mAlarm.getMinute();
        } else {
            hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
            minute = mCurrentTime.get(Calendar.MINUTE);
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                changeTimeButton.setText(buildString(selectedMinute, selectedHour));
                mMinute = selectedMinute;
                mHour = selectedHour;
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();

    }


    public void addAlarm(AlarmActivity.State state) {
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        String numberOfUsage = numberOfUsageEditText.getText().toString();
        StringBuilder stringBuilder = new StringBuilder();
        for (DayOfWeekView dayOfWeekView : weekViewListList) {
            if (dayOfWeekView.isChecked())
                stringBuilder.append("1");
            else
                stringBuilder.append("0");
        }
        outputProvider.displayLog(TAG, "addAlarm days list " + stringBuilder.toString());
        if (state == AlarmActivity.State.NEW) {

            if (!numberOfUsage.equals("")) {
                int nou = Integer.parseInt(numberOfUsage);
                mAlarm = new Alarm(mHour, mMinute, -1, nou, -1, -1, -1, true, true, stringBuilder.toString());

                DatabaseRepository.addAlarm(context, mAlarm);
                alarmReceiver.setRepeatingAlarm(context, mAlarm.getId());
            }
        } else {
            if (!numberOfUsage.equals(""))
                mAlarm.setUsageNumber(Integer.parseInt(numberOfUsage));

            mAlarm.setMinute(mMinute);
            mAlarm.setHour(mHour);
            mAlarm.setDaysRepeating(stringBuilder.toString());
            DatabaseHelper.getInstance(context).getAlarmDao().update(mAlarm);
            DatabaseRepository.deleteAlarmToPill(context, mAlarm.getId());
            if (mAlarm.isActive())
               alarmReceiver.setRepeatingAlarm(context, mAlarm.getId());
            else
                alarmReceiver.cancelAlarm(context, mAlarm.getId());
        }

        for (HorizontalScrollViewItem item : pillViewList) {
            if (item.isChecked()) {
                DatabaseRepository.addPillToAlarm(getContext(), new PillToAlarm(mAlarm.getId(), item.getPillId()));
            }
        }

    }

    private void getViewItem(Long id) {
        for (HorizontalScrollViewItem item : pillViewList) {
            if (item.getPillId().equals(id))
                item.setClick();
        }
    }


    /**
     * Builds string to be display in list item
     *
     * @param minute alarm minute
     * @param hour   alarm hour
     * @return returns built string
     */
    private String buildString(int minute, int hour) {
        StringBuilder stringBuilder = new StringBuilder();
        if (hour < 10)
            stringBuilder.append("0");
        stringBuilder.append(String.valueOf(hour));
        String s = " : ";
        stringBuilder.append(s);
        if (minute < 10)
            stringBuilder.append(String.valueOf(0));
        stringBuilder.append(String.valueOf(minute));
        return stringBuilder.toString();
    }

    private enum DayOfWeek {
        MON(0, "Mon"),
        TUE(1, "Tue"),
        WED(2, "Wed"),
        THU(3, "Thur"),
        FRI(4, "Fri"),
        SAT(5, "Sat"),
        SUN(6, "Sun");

        private int id;
        private String day;

        DayOfWeek(int id, String day) {
            this.id = id;
            this.day = day;
        }

        public int getId() {
            return id;
        }

        public String getDay() {
            return day;
        }
    }

}
