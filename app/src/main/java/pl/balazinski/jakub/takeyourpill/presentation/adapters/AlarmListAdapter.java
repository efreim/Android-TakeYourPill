package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.utilities.Constants;
import pl.balazinski.jakub.takeyourpill.data.model.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.model.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.AlarmActivity;
import pl.balazinski.jakub.takeyourpill.utilities.AlarmReceiver;


public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private int mBackground;
    private Context mContext;
    private AlarmListRefreshListener mRefreshListener;
    private OutputProvider mOutputProvider;
    private ViewHolder mViewHolder;

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
        mViewHolder = new ViewHolder(view, this);
        return mViewHolder;
    }

    public Alarm getItem(int position) {
        return DatabaseRepository.getAllAlarms(mContext).get(position);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.alarm = getItem(position);
        int hour = holder.alarm.getHour();
        int minute = holder.alarm.getMinute();
        int numberOfUsage = holder.alarm.getUsageNumber();

        String daysOfWeek = holder.alarm.getDaysRepeating();
        char[] daysOfWeekArray = daysOfWeek.toCharArray();


        if (holder.alarm.isRepeatable()) {
            holder.alarmDateTextView.setText("");
            holder.alarmTimeTextView.setText(buildString(minute, hour));
            if (numberOfUsage == -1)
                holder.alarmInfoTextView.setText(mContext.getString(R.string.infinite_usage) + "." + "\n");
            else
                holder.alarmInfoTextView.setText(Html.fromHtml(mContext.getString(R.string.alarm_usage_left) + ": <font color=#673AB7>" + numberOfUsage + "</font><br>"));

            int i = 0;
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                if (daysOfWeekArray[i] == '1') {
                    holder.alarmDateTextView.append(Html.fromHtml("<font color=#673AB7>" + dayOfWeek.getDay(mContext) + "</font>"));
                    holder.alarmDateTextView.append(" ");
                } else if (daysOfWeekArray[i] == '0') {
                    holder.alarmDateTextView.append(dayOfWeek.getDay(mContext));
                    holder.alarmDateTextView.append(" ");
                }
                i++;
            }
        } else if (holder.alarm.isInterval()) {
            holder.alarmTimeTextView.setText(buildString(minute, hour));
            holder.alarmDateTextView.setText(buildString(holder.alarm.getDay(), holder.alarm.getMonth(), holder.alarm.getYear())+ " ");
            holder.alarmDateTextView.append(Html.fromHtml(mContext.getString(R.string.interval) + ": <font color=#673AB7>" + holder.alarm.getIntervalTime() + "</font> " + mContext.getString(R.string.hours) + "."));
            if (numberOfUsage == -1)
                holder.alarmInfoTextView.setText(mContext.getString(R.string.infinite_usage) + "." + "\n");
            else
                holder.alarmInfoTextView.setText(Html.fromHtml(mContext.getString(R.string.alarm_usage_left) + ": <font color=#673AB7>" + numberOfUsage + "</font><br>"));
        } else if (holder.alarm.isSingle()) {
            holder.alarmTimeTextView.setText(buildString(minute, hour));
            holder.alarmDateTextView.setText(buildString(holder.alarm.getDay(), holder.alarm.getMonth(), holder.alarm.getYear()));
            holder.alarmInfoTextView.setText("");
        }

        holder.alarmInfoTextView.append(mContext.getString(R.string.pills_attached) + ": " + "\n");
        List<Long> items = DatabaseRepository.getPillsByAlarm(mContext, holder.alarm.getId());
        if (!items.isEmpty()) {
            for (Long pillId : items) {
                Pill p = DatabaseRepository.getPillByID(mContext, pillId);
                if (p != null) {
                    if (Objects.equals(pillId, items.get(items.size() - 1)))
                        holder.alarmInfoTextView.append(Html.fromHtml("<font color=#673AB7>" + p.getName() + "</font>."));
                    else
                        holder.alarmInfoTextView.append(Html.fromHtml("<font color=#673AB7>" + p.getName() + "</font>, "));
                }
            }
        } else {
            holder.alarmInfoTextView.append(mContext.getString(R.string.no_pill_attached_to_alarm));
        }

        holder.isBind = true;
        if (holder.alarm.isActive())
            holder.aSwitch.setChecked(true);
        else
            holder.aSwitch.setChecked(false);
        holder.isBind = false;


        if (holder.alarm.isActive()) {
            if (Constants.VERSION >= Build.VERSION_CODES.M) {
                holder.alarmItem.setBackground(ContextCompat.getDrawable(mContext, R.drawable.alarm_list_item_active_background));
            } else {
                holder.alarmItem.setBackground(mContext.getResources().getDrawable(R.drawable.alarm_list_item_active_background));
            }
        } else {
            if (Constants.VERSION >= Build.VERSION_CODES.M) {
                holder.alarmItem.setBackground(ContextCompat.getDrawable(mContext, R.drawable.alarm_list_item_inactive_background));
            } else {
                holder.alarmItem.setBackground(mContext.getResources().getDrawable(R.drawable.alarm_list_item_inactive_background));
            }
        }

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
        MON(0, R.string.monday),
        TUE(1, R.string.tuesday),
        WED(2, R.string.wednesday),
        THU(3, R.string.thursday),
        FRI(4, R.string.friday),
        SAT(5, R.string.saturday),
        SUN(6, R.string.sunday);

        private int id;
        private int day;

        DayOfWeek(int id, int day) {
            this.id = id;
            this.day = day;
        }

        public int getId() {
            return id;
        }

        public String getDay(Context context) {

            return context.getString(day);
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
        public Alarm alarm;
        public final View mView;
        public final TextView alarmTimeTextView, alarmDateTextView, alarmInfoTextView;
        public final LinearLayout alarmItem, expandableAlarmItem;
        public final RelativeLayout innerAlarmItem;
        public final Switch aSwitch;
        public final Context context;
        private AlarmReceiver alarmReceiver;
        private OutputProvider outputProvider;
        private AlarmListAdapter adapter;
        private boolean isExpended = false;
        public boolean isBind = false;

        public ViewHolder(final View view, AlarmListAdapter adapter) {
            super(view);
            mView = view;
            alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            innerAlarmItem = (RelativeLayout) view.findViewById(R.id.inner_alarm_item);
            expandableAlarmItem = (LinearLayout) view.findViewById(R.id.expandable_item);

            alarmTimeTextView = (TextView) view.findViewById(R.id.alarm_time_list_item);
            alarmDateTextView = (TextView) view.findViewById(R.id.alarm_date_list_item);
            alarmInfoTextView = (TextView) view.findViewById(R.id.expandable_item_textview);
            aSwitch = (Switch) view.findViewById(R.id.alarm_list_item_switch);

            context = view.getContext();
            alarmReceiver = new AlarmReceiver(context);
            outputProvider = new OutputProvider(context);

            aSwitch.setOnCheckedChangeListener(this);


            expandableAlarmItem.setOnClickListener(this);
            expandableAlarmItem.setOnLongClickListener(this);
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
            //mView.invalidate();
            adapter.mRefreshListener.onListRefresh();
            outputProvider.displayShortToast(context.getString(R.string.alarm_deleted));
            adapter.mRefreshListener.onListRefresh();
        }

        public void alarmActivator(final ViewHolder holder) {
            if (holder.alarm.isActive()) {
                holder.aSwitch.setChecked(false);
                holder.alarm.setActive(false);
                DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
                alarmReceiver.cancelAlarm(context, holder.alarm.getId());
            } else {
                if (holder.alarm.isRepeatable()) {
                    if (holder.alarm.getUsageNumber() == 0) {
                        outputProvider.displayShortToast(mContext.getString(R.string.usage_used_edit));
                        holder.alarm.setActive(false);
                        holder.aSwitch.setChecked(false);
                    }
                    else {
                        holder.alarm.setActive(true);
                        alarmReceiver.setRepeatingAlarm(context, holder.alarm.getId());
                    }
                } else if (holder.alarm.isInterval()) {
                    if (holder.alarm.getUsageNumber() == 0) {
                        outputProvider.displayShortToast(mContext.getString(R.string.usage_used_edit));
                        holder.alarm.setActive(false);
                        holder.aSwitch.setChecked(false);
                    }
                    else {
                        holder.alarm.setActive(true);
                        alarmReceiver.setIntervalAlarm(context, holder.alarm.getId());
                    }
                } else if (holder.alarm.isSingle())
                    alarmReceiver.setSingleAlarm(context, holder.alarm.getId());
            }
            DatabaseHelper.getInstance(context).getAlarmDao().update(holder.alarm);
            adapter.mRefreshListener.onListRefresh();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isBind) alarmActivator(this);
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