package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;

/**
 * Created by Kuba on 2016-01-31.
 */
public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private int mBackground;
    private Context context;
    //private List<Alarm> alarms;
    private AlarmReceiver alarmReceiver;

    public AlarmListAdapter(Context context) {
        this.context = context;
        TypedValue mTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        //alarms = DatabaseRepository.getAllAlarms(context);
        alarmReceiver = new AlarmReceiver();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {
        public Alarm alarm;
        public final View mView;
        public final TextView mTextView;
        public final LinearLayout alarmItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            mTextView = (TextView) view.findViewById(R.id.alarm_time);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AlarmReceiver alarmReceiver = new AlarmReceiver();
            int hour = alarm.getHour();
            int minute = alarm.getMinute();
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            switch (item.getItemId()) {
                case R.id.set_activity:
                    if (alarm.isActive()) {
                        item.setTitle("Deactivate alarm");
                        alarm.setIsActive(false);
                        DatabaseHelper.getInstance(mView.getContext()).getAlarmDao().update(alarm);
                        alarmItem.setBackgroundColor(Color.GRAY);
                        alarmReceiver.cancelAlarm(mView.getContext(), alarm.getId());
                    } else {
                        item.setTitle("Activate alarm");
                        alarm.setIsActive(true);
                        DatabaseHelper.getInstance(mView.getContext()).getAlarmDao().update(alarm);
                        alarmItem.setBackgroundColor(Color.WHITE);
                        alarmReceiver.setAlarm(mView.getContext(), calendar, alarm.getPillId(), alarm.getId());
                    }
                    break;
                case R.id.edit_alarm:
                    Toast.makeText(mView.getContext(), "to be done", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.delete_alarm:
                    alarmReceiver.cancelAlarm(mView.getContext(), alarm.getId());
                    DatabaseHelper.getInstance(mView.getContext()).getAlarmDao().delete(alarm);
                    mView.postInvalidate();
                    Toast.makeText(mView.getContext(), "Alarm deleted!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return true;
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    public Alarm getItem(int position) {
        return DatabaseRepository.getAllAlarms(context).get(position);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.alarm = getItem(position);
        int hour = holder.alarm.getHour();
        int minute = holder.alarm.getMinute();

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        String s = " : ";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(hour));
        stringBuilder.append(s);
        if (minute < 10)
            stringBuilder.append(String.valueOf(0));
        stringBuilder.append(String.valueOf(minute));

        holder.mTextView.setText(stringBuilder);

        if (holder.alarm.isActive())
            holder.alarmItem.setBackgroundColor(Color.WHITE);
        else
            holder.alarmItem.setBackgroundColor(Color.GRAY);


        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.alarm_context_menu);
                popupMenu.setOnMenuItemClickListener(holder);
                popupMenu.show();
                return true;
            }
        });


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position != -1) {
                    if (holder.alarm.isActive()) {
                        holder.alarm.setIsActive(false);
                        DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
                        holder.alarmItem.setBackgroundColor(Color.GRAY);
                        alarmReceiver.cancelAlarm(context, holder.alarm.getId());
                    } else {
                        holder.alarm.setIsActive(true);
                        DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
                        holder.alarmItem.setBackgroundColor(Color.WHITE);
                        alarmReceiver.setAlarm(context, calendar, holder.alarm.getPillId(), holder.alarm.getId());
                    }
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return DatabaseRepository.getAllAlarms(context).size();
    }
}