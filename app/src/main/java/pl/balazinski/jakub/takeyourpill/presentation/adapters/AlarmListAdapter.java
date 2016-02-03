package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Alarm;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillDetailActivity;

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


    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        if(minute<10)
            stringBuilder.append(String.valueOf(0));
        stringBuilder.append(String.valueOf(minute));

        holder.mTextView.setText(stringBuilder);

        if(holder.alarm.isActive())
            holder.alarmItem.setBackgroundColor(Color.WHITE);
        else
            holder.alarmItem.setBackgroundColor(Color.GRAY);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position != -1) {
                    if (holder.alarm.isActive()) {
                        holder.alarm.setIsActive(false);
                        holder.alarmItem.setBackgroundColor(Color.GRAY);
                        alarmReceiver.cancelAlarm(context, holder.alarm.getId());
                    }
                    else {
                        holder.alarm.setIsActive(true);
                        holder.alarmItem.setBackgroundColor(Color.WHITE);
                        alarmReceiver.setAlarm(context, calendar, holder.alarm.getId());
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