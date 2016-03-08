package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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
import pl.balazinski.jakub.takeyourpill.presentation.views.HorizontalScrollViewItem;

/**
 * Created by Kuba on 07.03.2016.
 */
public class SingleAlarmFragment extends Fragment {

    private final String TAG = "INTERVAL_ALARM_FRAGMENT";
    @Bind(R.id.inside_horizontal)
    GridLayout linearInsideHorizontal;

    @Bind(R.id.change_time_button)
    Button changeTimeButton;

    @Bind(R.id.change_day_button)
    Button changeDayButton;


    private List<HorizontalScrollViewItem> pillViewList;
    private Alarm mAlarm;
    private OutputProvider outputProvider;
    private Context context;
    private int mMinute = 0, mHour = 0, mNumberOfAlarms = 0, mDay = 0, mMonth = 0, mYear = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fragment_single_alarm, container, false);
        context = getContext();
        outputProvider = new OutputProvider(context);
        ButterKnife.bind(this, scrollView);

        Bundle bundle = getArguments();
        AlarmActivity.State state;
        if (bundle == null) {
            state = AlarmActivity.State.NEW;
            setupView(state);
        } else {
            state = AlarmActivity.State.EDIT;
            Long alarmId = bundle.getLong(Constants.EXTRA_LONG_ID);

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
        List<Pill> pills = DatabaseRepository.getAllPills(context);

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
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mMonth = calendar.get(Calendar.MONTH);
            mYear = calendar.get(Calendar.YEAR);
            changeTimeButton.setText(buildString(mMinute, mHour));
            changeDayButton.setText(buildString(mDay, mMonth, mYear));
        } else {
            //STATE EDIT
            mMinute = mAlarm.getMinute();
            mHour = mAlarm.getHour();
            mDay = mAlarm.getDay();
            mMonth = mAlarm.getMonth();
            mYear = mAlarm.getYear();
            changeTimeButton.setText(buildString(mMinute, mHour));
            changeDayButton.setText(buildString(mDay, mMonth, mYear));

            List<Long> pillIds = DatabaseRepository.getPillsByAlarm(context, mAlarm.getId());
            for (Long id : pillIds) {
                getViewItem(id);
            }
        }
    }

    @OnClick(R.id.change_time_button)
    public void onChangeTimeClick(View v) {
        Calendar mCurrentTime = Calendar.getInstance();
        //int hour, minute;
        if (mAlarm != null) {
            mHour = mAlarm.getHour();
            mMinute = mAlarm.getMinute();
        } else {
            mHour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
            mMinute = mCurrentTime.get(Calendar.MINUTE);
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                changeTimeButton.setText(buildString(selectedMinute, selectedHour));
                mMinute = selectedMinute;
                mHour = selectedHour;
            }
        }, mHour, mMinute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();

    }

    @OnClick(R.id.change_day_button)
    public void onDayTimeClick(View v) {
        Calendar mCurrentTime = Calendar.getInstance();
        // int day, month, year;
        if (mAlarm != null) {

            mDay = mAlarm.getDay();
            mMonth = mAlarm.getMonth();
            mYear = mAlarm.getYear();
        } else {
            mDay = mCurrentTime.get(Calendar.DAY_OF_MONTH);
            mMonth = mCurrentTime.get(Calendar.MONTH);
            mYear = mCurrentTime.get(Calendar.YEAR);
        }
        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                changeDayButton.setText(buildString(dayOfMonth, monthOfYear, year));
                mDay = dayOfMonth;
                mMonth = monthOfYear;
                mYear = year;
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Select starting date");
        mDatePicker.show();
    }


    public void addAlarm(AlarmActivity.State state) {
        AlarmReceiver alarmReceiver = new AlarmReceiver(context);
        if (state == AlarmActivity.State.NEW) {
            mAlarm = new Alarm(mHour, mMinute, -1, -1, mDay, mMonth, mYear, true, false, false, true, "");

            DatabaseRepository.addAlarm(context, mAlarm);
            alarmReceiver.setSingleAlarm(context, mAlarm.getId());

        } else {//TODO Protection from null interval and number of usage

            mAlarm.setDay(mDay);
            mAlarm.setMonth(mMonth);
            mAlarm.setYear(mYear);
            mAlarm.setMinute(mMinute);
            mAlarm.setHour(mHour);
            DatabaseHelper.getInstance(context).getAlarmDao().update(mAlarm);
            DatabaseRepository.deleteAlarmToPill(context, mAlarm.getId());
            if (mAlarm.isActive())
                alarmReceiver.setIntervalAlarm(context, mAlarm.getId());
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
     * Builds string
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

    /**
     * Builds string
     *
     * @param day   alarm day
     * @param month alarm month
     * @param year  alarm year
     * @return returns built string
     */
    private String buildString(int day, int month, int year) {
        month++;
        StringBuilder stringBuilder = new StringBuilder();
        String s = "/";
        if (day < 10)
            stringBuilder.append("0");
        stringBuilder.append(day);
        stringBuilder.append(s);
        if (month < 10)
            stringBuilder.append("0");
        stringBuilder.append(month);
        stringBuilder.append(s);
        stringBuilder.append(year);
        return stringBuilder.toString();
    }

}
