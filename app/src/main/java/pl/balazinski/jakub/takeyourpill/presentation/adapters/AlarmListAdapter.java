package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Created by Kuba on 2016-01-31.
 */
public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private int mBackground;
    private Context context;
    private ListRefreshListener refreshListener;
    private ViewHolder viewHolder;
    private View view;
    public AlarmListAdapter(Context context) {
        this.context = context;
        TypedValue mTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
    }

    /**
     * Interface implemented in AlarmListFragment in order to refresh list after deleting item from.
     */
    public interface ListRefreshListener {
        void onListRefresh();
    }

    public void setListRefreshListener(ListRefreshListener l) {
        this.refreshListener = l;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);
        view.setBackgroundResource(mBackground);
        viewHolder = new ViewHolder(view,this);
        return viewHolder;
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


        String text = buildString(minute, hour);

        holder.mTextView.setText(text);

        if (holder.alarm.isActive())
            holder.alarmItem.setBackgroundColor(Color.WHITE);
        else
            holder.alarmItem.setBackgroundColor(Color.GRAY);

    }

    @Override
    public int getItemCount() {
        return DatabaseRepository.getAllAlarms(context).size();
    }

    /**
     * Builds string to be display in list item
     *
     * @param minute alarm minute
     * @param hour   alarm hour
     * @return returns built string
     */
    private String buildString(int minute, int hour) {
        String s = " : ";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(hour));
        stringBuilder.append(s);
        if (minute < 10)
            stringBuilder.append(String.valueOf(0));
        stringBuilder.append(String.valueOf(minute));
        return stringBuilder.toString();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener, View.OnLongClickListener {
        public Alarm alarm;
        public final View mView;
        public final TextView mTextView;
        public final LinearLayout alarmItem;
        public final Context context;
        private AlarmReceiver alarmReceiver;
        private OutputProvider outputProvider;
        private AlarmListAdapter adapter;

        public ViewHolder(View view, AlarmListAdapter adapter) {
            super(view);
            mView = view;
            alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            mTextView = (TextView) view.findViewById(R.id.alarm_time);
            context = view.getContext();
            alarmReceiver = new AlarmReceiver();
            outputProvider = new OutputProvider(context);
            mView.setOnClickListener(this);
            mView.setOnLongClickListener(this);
            this.adapter = adapter;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int hour = alarm.getHour();
            int minute = alarm.getMinute();
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            switch (item.getItemId()) {
                case R.id.set_activity:
                    alarmActivator(this);
                    break;
                case R.id.edit_alarm:
                    outputProvider.displayShortToast("to be done");
                    break;
                case R.id.delete_alarm:
                    alarmDeleter();
                    break;
                default:
                    break;
            }
            return true;
        }

        public void alarmDeleter() {
            alarmReceiver.cancelAlarm(mView.getContext(), alarm.getId());
            DatabaseRepository.deleteAlarm(context, alarm);
            mView.invalidate();
            adapter.refreshListener.onListRefresh();
            outputProvider.displayShortToast("Alarm deleted!");
        }

        public void alarmActivator(final ViewHolder holder) {
            int hour = alarm.getHour();
            int minute = alarm.getMinute();
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            if (holder.alarm.isActive()) {
                holder.alarm.setIsActive(false);
                DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
                holder.alarmItem.setBackgroundColor(Color.GRAY);
                alarmReceiver.cancelAlarm(context, holder.alarm.getId());
                adapter.refreshListener.onListRefresh();
            } else {
                holder.alarm.setIsActive(true);
                DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
                holder.alarmItem.setBackgroundColor(Color.WHITE);
                alarmReceiver.setAlarm(context, calendar, holder.alarm.getPillId(), holder.alarm.getId());
                adapter.refreshListener.onListRefresh();
            }
        }

        @Override
        public void onClick(View v) {
            alarmActivator(this);
        }

        @Override
        public boolean onLongClick(View v) {
            outputProvider.displayPopupMenu(this, v);
            return false;
        }
    }


}