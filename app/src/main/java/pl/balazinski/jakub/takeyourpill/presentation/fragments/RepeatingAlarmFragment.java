package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
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
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmActivity;
import pl.balazinski.jakub.takeyourpill.presentation.views.DayOfWeekView;
import pl.balazinski.jakub.takeyourpill.presentation.views.HorizontalScrollViewItem;
import pl.balazinski.jakub.takeyourpill.utilities.AlarmReceiver;


public class RepeatingAlarmFragment extends Fragment {

    private final String TAG = "REPEATING_ALARM_FRAGMENT";

    @Bind(R.id.inside_horizontal)
    GridLayout linearInsideHorizontal;
    @Bind(R.id.day_of_week_grid)
    GridLayout dayOfWeekGrid;
    @Bind(R.id.change_time_button)
    EditText changeTimeButton;
    @Bind(R.id.number_of_usage)
    EditText numberOfUsageEditText;
    @Bind(R.id.repeatable_dummy)
    LinearLayout linearLayoutDummy;

    private List<HorizontalScrollViewItem> mPillViewList;
    private List<DayOfWeekView> mWeekViewListList;
    private Alarm mAlarm;
    private OutputProvider mOutputProvider;
    private Context mContext;
    private int mMinute;
    private int mHour;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView linearView = (ScrollView) inflater.inflate(R.layout.fragment_repetable_alarm, container, false);
        ButterKnife.bind(this, linearView);
        mContext = getContext();
        mOutputProvider = new OutputProvider(mContext);
        Bundle bundle = getArguments();
        setupContent(bundle);

        return linearView;
    }

    private void setupContent(Bundle bundle) {
        AlarmActivity.State state;
        if (bundle == null) {
            state = AlarmActivity.State.NEW;
            setupView(state);
        } else {
            state = AlarmActivity.State.EDIT;
            Long alarmId = bundle.getLong(Constants.EXTRA_LONG_ID);

            mAlarm = DatabaseRepository.getAlarmById(mContext, alarmId);
            if (mAlarm == null)
                mOutputProvider.displayShortToast(getString(R.string.error_loading_pills));
            else {
                setupView(state);
            }
        }
    }

    private void setupView(AlarmActivity.State state) {
        linearLayoutDummy.requestFocus();
        mPillViewList = new ArrayList<>();
        mWeekViewListList = new ArrayList<>();
        List<Pill> pills = DatabaseRepository.getAllPills(mContext);


        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            DayOfWeekView dayOfWeekView = new DayOfWeekView(mContext, dayOfWeek.getId(), dayOfWeek.getDay());
            dayOfWeekGrid.addView(dayOfWeekView);
            mWeekViewListList.add(dayOfWeekView);
        }

        if (pills != null) {
            for (Pill p : pills) {
                HorizontalScrollViewItem item = new HorizontalScrollViewItem(mContext, p.getPhoto(), p.getName(), p.getId());
                linearInsideHorizontal.addView(item);
                mPillViewList.add(item);
            }
        } else
            mOutputProvider.displayShortToast(getString(R.string.error_loading_pills));


        if (state == AlarmActivity.State.NEW) {
            Calendar calendar = Calendar.getInstance();
            mMinute = calendar.get(Calendar.MINUTE);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            //changeTimeButton.setText(buildString(mMinute, mHour));
        } else {
            //STATE EDIT
            mMinute = mAlarm.getMinute();
            mHour = mAlarm.getHour();
            int mNumberOfAlarms = mAlarm.getUsageNumber();
            changeTimeButton.setText(buildString(mMinute, mHour));

            if (mNumberOfAlarms != -1) {
                numberOfUsageEditText.setText(String.valueOf(mNumberOfAlarms));
            }
            List<Long> pillIds = DatabaseRepository.getPillsByAlarm(mContext, mAlarm.getId());
            for (Long id : pillIds) {
                getViewItem(id);
            }
            String daysOfWeek = mAlarm.getDaysRepeating();
            mOutputProvider.displayLog(TAG, "days of week:  " + daysOfWeek);
            char[] daysArray = daysOfWeek.toCharArray();

            for (int i = 0; i < daysArray.length; i++) {
                mOutputProvider.displayLog(TAG, "daysArray[" + i + "] = " + daysArray[i]);
                if (daysArray[i] == '1')
                    mWeekViewListList.get(i).setClick();
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
        mTimePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                changeTimeButton.setText(buildString(selectedMinute, selectedHour));
                mMinute = selectedMinute;
                mHour = selectedHour;
            }
        }, hour, minute, true);
        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();

    }


    public boolean addAlarm(AlarmActivity.State state) {
        boolean isDayChecked = false;
        AlarmReceiver alarmReceiver = new AlarmReceiver(mContext);
        String numberOfUsage = numberOfUsageEditText.getText().toString();
        StringBuilder stringBuilder = new StringBuilder();
        for (DayOfWeekView dayOfWeekView : mWeekViewListList) {
            if (dayOfWeekView.isChecked()) {
                isDayChecked = true;
                stringBuilder.append("1");
            } else
                stringBuilder.append("0");
        }

        mOutputProvider.displayLog(TAG, "addAlarm days list " + stringBuilder.toString());
        if (state == AlarmActivity.State.NEW) {

            if (changeTimeButton.getText().toString().equals("")) {
                changeTimeButton.setError(getString(R.string.error_choose_alarm_time));
                return false;
            } else
                changeTimeButton.setError(null);

            if (!isDayChecked) {
                mOutputProvider.displayShortToast(getString(R.string.toast_set_repeating_days));
                return false;
            }

            if (numberOfUsage.equals("")) {
                int nou = -1;
                mAlarm = new Alarm(mHour, mMinute, -1, nou, -1, -1, -1, true, true, false, false, stringBuilder.toString());
                DatabaseRepository.addAlarm(mContext, mAlarm);
                alarmReceiver.setRepeatingAlarm(mContext, mAlarm.getId());
            } else if (!numberOfUsage.equals("") && !numberOfUsage.startsWith(".")) {
                int nou = Integer.parseInt(numberOfUsage);
                mAlarm = new Alarm(mHour, mMinute, -1, nou, -1, -1, -1, true, true, false, false, stringBuilder.toString());
                DatabaseRepository.addAlarm(mContext, mAlarm);
                alarmReceiver.setRepeatingAlarm(mContext, mAlarm.getId());
            } else {
                mOutputProvider.displayShortToast(getString(R.string.toast_dot_error));
                return false;
            }

        } else {

            if (numberOfUsage.equals("")) {
                mAlarm.setUsageNumber(-1);
            } else if (!numberOfUsage.equals("") && !numberOfUsage.startsWith("."))
                mAlarm.setUsageNumber(Integer.parseInt(numberOfUsage));
            else {
                mOutputProvider.displayShortToast(getString(R.string.toast_error_number_of_alarms));
                return false;
            }

            if (!isDayChecked) {
                mOutputProvider.displayShortToast(getString(R.string.toast_set_repeating_days));
                return false;
            }

            mAlarm.setMinute(mMinute);
            mAlarm.setHour(mHour);
            mAlarm.setDaysRepeating(stringBuilder.toString());
            mAlarm.setIsActive(true);
            DatabaseHelper.getInstance(mContext).getAlarmDao().update(mAlarm);
            DatabaseRepository.deleteAlarmToPill(mContext, mAlarm.getId());
            alarmReceiver.setRepeatingAlarm(mContext, mAlarm.getId());

        }

        for (HorizontalScrollViewItem item : mPillViewList) {
            if (item.isChecked()) {
                DatabaseRepository.addPillToAlarm(getContext(), new PillToAlarm(mAlarm.getId(), item.getPillId()));
            }
        }
        return true;
    }

    private void getViewItem(Long id) {
        for (HorizontalScrollViewItem item : mPillViewList) {
            if (item.getPillId().equals(id))
                item.setClick();
        }
    }


    /**
     * Builds string to be display in list item
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
        MON(0, Constants.MONDAY),
        TUE(1, Constants.TUESDAY),
        WED(2, Constants.WEDNESDAY),
        THU(3, Constants.THURSDAY),
        FRI(4, Constants.FRIDAY),
        SAT(5, Constants.SATURDAY),
        SUN(6, Constants.SUNDAY);

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
