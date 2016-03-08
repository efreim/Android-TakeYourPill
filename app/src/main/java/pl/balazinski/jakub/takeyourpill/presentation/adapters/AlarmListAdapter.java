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
import android.widget.ImageButton;
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
        boolean isSingleAlarm = true;
        int hour = holder.alarm.getHour();
        int minute = holder.alarm.getMinute();
        StringBuilder stringBuilder = new StringBuilder();

        String text = buildString(minute, hour);
        String daysOfWeek = holder.alarm.getDaysRepeating();
        char[] daysOfWeekArray = daysOfWeek.toCharArray();
        DayOfWeek[] dayOfWeekEnum = DayOfWeek.values();
        holder.alarmTimeTextView.setText(text);
        if (holder.alarm.isRepeatable()) {
            for (int i = 0; i < daysOfWeekArray.length; i++) {
                if (daysOfWeekArray[i] == '1') {
                    stringBuilder.append(dayOfWeekEnum[i].getDay());
                    stringBuilder.append(" ");
                    isSingleAlarm = false;
                }
            }

            if (isSingleAlarm) {
                holder.alarmTimeTextView.append(" ");
                holder.alarmTimeTextView.append(buildString(holder.alarm.getDay(), holder.alarm.getMonth(), holder.alarm.getYear()));
                holder.alarmTypeTextView.setText("Repeatable alarm, no days added");
            } else {
                holder.alarmTimeTextView.append("\nDays repeating : \n" + stringBuilder.toString());
                holder.alarmTypeTextView.setText("Repeatable alarm");
            }

        } else if (holder.alarm.isInterval()) {
            holder.alarmTimeTextView.append(" " + buildString(holder.alarm.getDay(), holder.alarm.getMonth(), holder.alarm.getYear()) + "\nInterval:\n" + holder.alarm.getInterval() + " minutes");
            holder.alarmTypeTextView.setText("Interval alarm");
        } else if (holder.alarm.isSingle()) {
            holder.alarmTypeTextView.setText("Single alarm");
            holder.alarmTimeTextView.append(buildString(holder.alarm.getDay(), holder.alarm.getMonth(), holder.alarm.getYear()));
        }

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
                holder.innerAlarmItem.setBackgroundColor(Color.WHITE);
            } else if (holder.alarm.isInterval()) {
                holder.alarmItem.setBackgroundColor(Color.GREEN);
                holder.expendableItem.setBackgroundColor(Color.GREEN);
                holder.innerAlarmItem.setBackgroundColor(Color.GREEN);
            } else if (holder.alarm.isSingle()) {
                holder.alarmItem.setBackgroundColor(Color.CYAN);
                holder.expendableItem.setBackgroundColor(Color.CYAN);
                holder.innerAlarmItem.setBackgroundColor(Color.CYAN);
            }

        } else {
            if (holder.alarm.isRepeatable()) {
                holder.alarmItem.setBackgroundColor(Color.GRAY);
                holder.expendableItem.setBackgroundColor(Color.GRAY);
                holder.innerAlarmItem.setBackgroundColor(Color.GRAY);
            } else if (holder.alarm.isInterval()) {
                holder.alarmItem.setBackgroundColor(Color.DKGRAY);
                holder.expendableItem.setBackgroundColor(Color.DKGRAY);
                holder.innerAlarmItem.setBackgroundColor(Color.DKGRAY);

            } else if (holder.alarm.isInterval()) {
                holder.alarmItem.setBackgroundColor(Color.BLUE);
                holder.expendableItem.setBackgroundColor(Color.BLUE);
                holder.innerAlarmItem.setBackgroundColor(Color.BLUE);
            }
        }

        holder.imageButton.setOnClickListener(new View.OnClickListener()

                                              {
                                                  @Override
                                                  public void onClick(View v) {
                                                      holder.alarmActivator(holder);
                                                  }
                                              }

        );

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
        String s = ":";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(hour));
        stringBuilder.append(s);
        if (minute < 10)
            stringBuilder.append(String.valueOf(0));
        stringBuilder.append(String.valueOf(minute));
        stringBuilder.append(" ");
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


    public static class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener, View.OnLongClickListener {
        public Alarm alarm;
        public final View mView;
        public final TextView alarmTimeTextView, alarmTypeTextView;
        public final LinearLayout alarmItem, innerAlarmItem;
        public final ImageButton imageButton;
        public final HorizontalScrollView expendableItem;
        public final Context context;
        private AlarmReceiver alarmReceiver;
        private OutputProvider outputProvider;
        private AlarmListAdapter adapter;
        private boolean isExpended = false;

        public ViewHolder(final View view, AlarmListAdapter adapter) {
            super(view);
            mView = view;
            alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            innerAlarmItem = (LinearLayout) view.findViewById(R.id.inner_alarm_item);
            alarmTimeTextView = (TextView) view.findViewById(R.id.alarm_time_list_item);
            alarmTypeTextView = (TextView) view.findViewById(R.id.alarm_type_list_item);
            expendableItem = (HorizontalScrollView) view.findViewById(R.id.expendable_item);
            imageButton = (ImageButton) view.findViewById(R.id.image_button);
            context = view.getContext();
            alarmReceiver = new AlarmReceiver(context);
            outputProvider = new OutputProvider(context);
            expendableItem.setOnClickListener(this);

            mView.setOnClickListener(this);
            mView.setOnLongClickListener(this);
            this.adapter = adapter;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + alarmTimeTextView.getText();
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
                //holder.alarmItem.setBackgroundColor(Color.GRAY);
                //holder.innerAlarmItem.setBackgroundColor(Color.GRAY);
                alarmReceiver.cancelAlarm(context, holder.alarm.getId());
                adapter.refreshListener.onListRefresh();
            } else {

                holder.alarm.setIsActive(true);
                DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
                //holder.alarmItem.setBackgroundColor(Color.WHITE);
                //holder.innerAlarmItem.setBackgroundColor(Color.WHITE);
                if(holder.alarm.isRepeatable())
                    alarmReceiver.setRepeatingAlarm(context, holder.alarm.getId());
                else if(holder.alarm.isInterval())
                    alarmReceiver.setIntervalAlarm(context,holder.alarm.getId());
                else if(holder.alarm.isSingle())
                    alarmReceiver.setSingleAlarm(context, holder.alarm.getId());

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