package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
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
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmActivity;
import pl.balazinski.jakub.takeyourpill.presentation.views.HorizontalScrollViewItem;
import pl.balazinski.jakub.takeyourpill.utilities.AlarmReceiver;


public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private int mBackground;
    private Context mContext;
    private AlarmListRefreshListener mRefreshListener;
    private OutputProvider mOutputProvider;

    public AlarmListAdapter(Context context) {
        this.mContext = context;
        TypedValue mTypedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mOutputProvider = new OutputProvider(mContext);
    }

    /**
     * Interface implemented in AlarmListFragment in order to refresh list after deleting item from.
     */
    public interface AlarmListRefreshListener {
        void onListRefresh();
    }

    public void setListRefreshListener(AlarmListRefreshListener l) {
        this.mRefreshListener = l;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_alarm_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view, this);
    }

    public Alarm getItem(int position) {
        return DatabaseRepository.getAllAlarms(mContext).get(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.alarm = getItem(position);
        int hour = holder.alarm.getHour();
        int minute = holder.alarm.getMinute();
        StringBuilder stringBuilder = new StringBuilder();

        String text = buildString(minute, hour);
        String daysOfWeek = holder.alarm.getDaysRepeating();
        char[] daysOfWeekArray = daysOfWeek.toCharArray();
        DayOfWeek[] dayOfWeekEnum = DayOfWeek.values();

        if (holder.alarm.isRepeatable()) {
            for (int i = 0; i < daysOfWeekArray.length; i++) {
                if (daysOfWeekArray[i] == '1') {
                    stringBuilder.append(dayOfWeekEnum[i].getDay());
                    stringBuilder.append(" ");
                }
            }
            holder.alarmTimeTextView.setText(text + "\n ");
            holder.alarmInfoTextView.setText("Days repeating : \n" + stringBuilder.toString());
            holder.alarmTypeTextView.setText("Repeatable alarm");

        } else if (holder.alarm.isInterval()) {
            holder.alarmTimeTextView.setText(text + "\n" + buildString(holder.alarm.getDay(), holder.alarm.getMonth(), holder.alarm.getYear()));
            holder.alarmInfoTextView.setText("Interval:\n" + holder.alarm.getInterval() + " minutes");
            holder.alarmTypeTextView.setText("Interval alarm");
        } else if (holder.alarm.isSingle()) {
            holder.alarmTypeTextView.setText("Single alarm");
            holder.alarmTimeTextView.setText(text + buildString(holder.alarm.getDay(), holder.alarm.getMonth(), holder.alarm.getYear()));

        }

        GridLayout gridLayout = (GridLayout) holder.pillList.getChildAt(0);
        gridLayout.removeAllViews();

        List<Long> items = DatabaseRepository.getPillsByAlarm(mContext, holder.alarm.getId());
        if (!items.isEmpty()) {
            for (Long pillId : items) {
                Pill p = DatabaseRepository.getPillByID(mContext, pillId);
                HorizontalScrollViewItem item;
                if (p != null) {
                    item = new HorizontalScrollViewItem(mContext, p.getPhoto(), p.getName(), p.getId());
                    item.setCheckboxGone();
                    item.setTextColorWhite();
                    item.setClickable(false);
                    gridLayout.addView(item);
                }
            }
        } else {
            HorizontalScrollViewItem item = new HorizontalScrollViewItem(mContext, "", mContext.getString(R.string.no_pill_attached_to_alarm), null);
            item.setGravity(Gravity.CENTER);
            item.setCheckboxGone();
            item.setImageGone();
            item.setTextColorWhite();
            item.setClickable(false);
            gridLayout.addView(item);
        }

        if (holder.alarm.isActive()) {
            if (Constants.VERSION >= Build.VERSION_CODES.M) {
                holder.alarmItem.setBackground(ContextCompat.getDrawable(mContext, R.drawable.alarm_list_item_active_background));
                holder.pillList.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_corners_active));
            } else {
                holder.alarmItem.setBackground(mContext.getResources().getDrawable(R.drawable.alarm_list_item_active_background));
                holder.pillList.setBackground(mContext.getResources().getDrawable(R.drawable.rounded_corners_active));
            }
        } else {
            if (Constants.VERSION >= Build.VERSION_CODES.M) {
                holder.alarmItem.setBackground(ContextCompat.getDrawable(mContext, R.drawable.alarm_list_item_inactive_background));
                holder.pillList.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_corners_inactive));
            } else {
                holder.alarmItem.setBackground(mContext.getResources().getDrawable(R.drawable.alarm_list_item_inactive_background));
                holder.pillList.setBackground(mContext.getResources().getDrawable(R.drawable.rounded_corners_inactive));

            }
        }
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      holder.alarmActivator(holder);
                                                  }
                                              }
        );
    }

    @Override
    public int getItemCount() {
        return DatabaseRepository.getAllAlarms(mContext).size();
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


    public static class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener, View.OnLongClickListener {
        public Alarm alarm;
        public final View mView;
        public final TextView alarmTimeTextView, alarmTypeTextView, alarmInfoTextView;
        public final LinearLayout alarmItem, innerAlarmItem, expandableAlarmItem;
        public final ImageButton imageButton;
        public final HorizontalScrollView pillList;
        public final Context context;
        public final GridLayout gridLayout;
        private AlarmReceiver alarmReceiver;
        private OutputProvider outputProvider;
        private AlarmListAdapter adapter;
        private boolean isExpended = false;

        public ViewHolder(final View view, AlarmListAdapter adapter) {
            super(view);
            mView = view;
            alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            innerAlarmItem = (LinearLayout) view.findViewById(R.id.inner_alarm_item);
            expandableAlarmItem = (LinearLayout) view.findViewById(R.id.expandable_item);
            alarmTimeTextView = (TextView) view.findViewById(R.id.alarm_time_list_item);
            alarmInfoTextView = (TextView) view.findViewById(R.id.expandable_item_textview);
            alarmTypeTextView = (TextView) view.findViewById(R.id.alarm_type_list_item);
            pillList = (HorizontalScrollView) view.findViewById(R.id.pill_list);
            gridLayout = (GridLayout) view.findViewById(R.id.grid_in_pill_list);
            imageButton = (ImageButton) view.findViewById(R.id.image_button);
            context = view.getContext();
            alarmReceiver = new AlarmReceiver(context);
            outputProvider = new OutputProvider(context);

            expandableAlarmItem.setOnClickListener(this);
            expandableAlarmItem.setOnLongClickListener(this);
            pillList.setOnClickListener(this);
            pillList.setOnLongClickListener(this);
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
            adapter.mRefreshListener.onListRefresh();
            outputProvider.displayShortToast(context.getString(R.string.alarm_deleted));
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
                alarmReceiver.cancelAlarm(context, holder.alarm.getId());
                adapter.mRefreshListener.onListRefresh();
            } else {

                holder.alarm.setIsActive(true);
                DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
                if (holder.alarm.isRepeatable())
                    alarmReceiver.setRepeatingAlarm(context, holder.alarm.getId());
                else if (holder.alarm.isInterval())
                    alarmReceiver.setIntervalAlarm(context, holder.alarm.getId());
                else if (holder.alarm.isSingle())
                    alarmReceiver.setSingleAlarm(context, holder.alarm.getId());

                adapter.mRefreshListener.onListRefresh();
            }
        }

        @Override
        public void onClick(View v) {
            if (!isExpended) {
                expandableAlarmItem.setVisibility(View.VISIBLE);
                isExpended = true;
                return;
            } else {
                expandableAlarmItem.setVisibility(View.GONE);
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