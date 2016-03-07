package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmActivity;
import pl.balazinski.jakub.takeyourpill.presentation.views.HorizontalScrollViewItem;

/**
 * Created by Kuba on 2016-01-31.
 */
public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private int mBackground;
    private Context context;
    private AlarmListRefreshListener refreshListener;
    private OutputProvider outputProvider;

    public AlarmListAdapter(Context context) {
        this.context = context;
        TypedValue mTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        outputProvider = new OutputProvider(context);
    }

    /**
     * Interface implemented in AlarmListFragment in order to refresh list after deleting item from.
     */
    public interface AlarmListRefreshListener {
        void onListRefresh();
    }

    public void setListRefreshListener(AlarmListRefreshListener l) {
        this.refreshListener = l;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);
        view.setBackgroundResource(mBackground);
        ViewHolder viewHolder = new ViewHolder(view, this);
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

        String text = buildString(minute, hour);

        holder.mTextView.setText(text);


        GridLayout gridLayout = (GridLayout) holder.expendableItem.getChildAt(0);
        gridLayout.removeAllViews();

        List<Long> items = DatabaseRepository.getPillsByAlarm(context, holder.alarm.getId());
        if (items != null) {
            for (Long pillId : items) {
                Pill p = DatabaseRepository.getPillByID(context, pillId);
                HorizontalScrollViewItem item = new HorizontalScrollViewItem(context, p.getPhoto(), p.getName(), p.getId());
                item.setCheckboxGone();
                item.setClickable(false);
                gridLayout.addView(item);
            }
        } else {
            HorizontalScrollViewItem item = new HorizontalScrollViewItem(context, "", "No pills attached to alarm", null);
            item.setCheckboxGone();
            item.setClickable(false);
            gridLayout.addView(item);
        }


        if (holder.alarm.isActive()) {
            if (holder.alarm.isRepeatable()) {
                holder.alarmItem.setBackgroundColor(Color.WHITE);
                holder.expendableItem.setBackgroundColor(Color.WHITE);
            }
            else{
                holder.alarmItem.setBackgroundColor(Color.GREEN);
                holder.expendableItem.setBackgroundColor(Color.GREEN);
            }

        } else {
            if (holder.alarm.isRepeatable()) {
                holder.alarmItem.setBackgroundColor(Color.GRAY);
                holder.expendableItem.setBackgroundColor(Color.GRAY);
            }
            else{
                holder.alarmItem.setBackgroundColor(Color.DKGRAY);
                holder.expendableItem.setBackgroundColor(Color.DKGRAY);
            }
        }

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
        public final HorizontalScrollView expendableItem;
        public final Context context;
        private AlarmReceiver alarmReceiver;
        private OutputProvider outputProvider;
        private AlarmListAdapter adapter;
        private boolean isExpended = false;

        public ViewHolder(View view, AlarmListAdapter adapter) {
            super(view);
            mView = view;
            alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            mTextView = (TextView) view.findViewById(R.id.alarm_time);
            expendableItem = (HorizontalScrollView) view.findViewById(R.id.expendable_item);
            context = view.getContext();
            alarmReceiver = new AlarmReceiver();
            outputProvider = new OutputProvider(context);
            expendableItem.setOnClickListener(this);
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
                    Intent intent = new Intent(context, AlarmActivity.class);
                    intent.putExtra(Constants.EXTRA_LONG_ID, alarm.getId());
                    context.startActivity(intent);
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
                alarmReceiver.setRepeatingAlarm(context, holder.alarm.getId());
                adapter.refreshListener.onListRefresh();
            }
        }

        @Override
        public void onClick(View v) {
            if (!isExpended) {
                expendableItem.setVisibility(View.VISIBLE);
                isExpended = true;
                return;
            } else {
                expendableItem.setVisibility(View.GONE);
                isExpended = false;
                return;
            }

        }

        @Override
        public boolean onLongClick(View v) {
            outputProvider.displayPopupMenu(this, v, R.menu.alarm_context_menu);
            return false;
        }
    }

}