package pl.balazinski.jakub.takeyourpill.utilities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static MediaPlayer mPlayer;
    private static Vibrator mVibrator;
    private final String TAG = getClass().getSimpleName();
    private OutputProvider outputProvider;
    private Context mContext;

    public AlarmReceiver() {
    }

    public AlarmReceiver(Context context) {
        this.mContext = context;
        outputProvider = new OutputProvider(context);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Constants.MAIN_FROM_ALARM_KEY, Constants.MAIN_FROM_ALARM);
        i.putExtra(Constants.EXTRA_LONG_ALARM_ID, bundle.getLong(Constants.EXTRA_LONG_ALARM_ID));

        ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmService.class.getName());
        startWakefulService(context, (i.setComponent(comp)));

        setResultCode(Activity.RESULT_OK);
    }

    /**
     * Method sets repeating alarm what means that you choose hour,minute and days
     * you want alarm to repeat. If you won't choose days to repeat alarm will fire
     * in given hour next day.
     *
     * @param context application context
     * @param alarmID id of alarm
     */
    public void setRepeatingAlarm(Context context, Long alarmID) {

        List<Long> daysList = new ArrayList<>();
        String daysOfWeek;
        char[] daysOfWeekArray;

        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            alarm.setIsActive(true);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);

            int alarmMinute, alarmHour;
            alarmMinute = alarm.getMinute();
            alarmHour = alarm.getHour();

            daysOfWeek = alarm.getDaysRepeating();
            daysOfWeekArray = daysOfWeek.toCharArray();

            outputProvider.displayLog(TAG, "alarm.getHour = " + alarm.getHour() + ";  alarm.getMinute = " + alarm.getMinute());

            Calendar now = Calendar.getInstance();
            outputProvider.displayLog(TAG, "now day = " + now.get(Calendar.DAY_OF_WEEK) + ";  date: " + now.getTime());

            int i;
            for (i = 0; i < daysOfWeekArray.length; i++) {
                outputProvider.displayLog(TAG, " ");
                outputProvider.displayLog(TAG, "i == " + i);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
                calendar.set(Calendar.MINUTE, alarm.getMinute());
                if (daysOfWeekArray[i] == '1') {
                    if (i < 6) {
                        int day = i + 2;
                        calendar.set(Calendar.DAY_OF_WEEK, day);
                        if (day == now.get(Calendar.DAY_OF_WEEK)) {
                            outputProvider.displayLog(TAG, "i + 2 == day of week");
                            if (alarmHour == now.get(Calendar.HOUR_OF_DAY)) {
                                outputProvider.displayLog(TAG, "alarm.getHour == now.getHourOfDay");
                                if (alarmMinute <= now.get(Calendar.MINUTE)) {
                                    outputProvider.displayLog(TAG, "alarm.getMinute <= now.getMinute");
                                    daysList.add(nextWeekDay(calendar));
                                    continue;
                                }
                            } else if (alarmHour < now.get(Calendar.HOUR_OF_DAY)) {
                                outputProvider.displayLog(TAG, "alarm.getHour < now.getHourOfDay");
                                daysList.add(nextWeekDay(calendar));
                                continue;
                            }
                        } else if (day < now.get(Calendar.DAY_OF_WEEK)) {
                            outputProvider.displayLog(TAG, "i + 2 < day of week");
                            daysList.add(nextWeekDay(calendar));
                            continue;
                        }

                        if (now.get(Calendar.DAY_OF_WEEK) == 1) {
                            outputProvider.displayLog(TAG, "i + 2 < day of week");
                            daysList.add(nextWeekDay(calendar));
                            continue;
                        }
                        daysList.add(calendar.getTimeInMillis());
                    } else if (i == 6) {
                        int sunday = 1;
                        calendar.set(Calendar.DAY_OF_WEEK, sunday);
                        if (sunday == now.get(Calendar.DAY_OF_WEEK)) {
                            outputProvider.displayLog(TAG, "i  == dayOfWeek");
                            if (alarmHour == now.get(Calendar.HOUR_OF_DAY)) {
                                outputProvider.displayLog(TAG, "alarm.getHour == now.getHourOfDay");
                                if (alarmMinute <= now.get(Calendar.MINUTE)) {
                                    outputProvider.displayLog(TAG, "alarm.getMinute <= now.getMinute");
                                    daysList.add(nextWeekDay(calendar));
                                    continue;
                                }
                            } else if (alarmHour < now.get(Calendar.HOUR_OF_DAY)) {
                                outputProvider.displayLog(TAG, "alarm.getHour < now.getHourOfDay");
                                daysList.add(nextWeekDay(calendar));
                                continue;
                            }
                        }
                        daysList.add(calendar.getTimeInMillis());
                    }
                }

            }

            outputProvider.displayLog(TAG, "collections min = " + Collections.min(daysList));
            for (Long l : daysList) {
                Calendar test = Calendar.getInstance();
                test.setTimeInMillis(l);
                outputProvider.displayLog(TAG, " " + test.getTime());
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmID);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
            long alarmTimeInMillis = Collections.min(daysList);
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
            outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));

            outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
        }
    }

    /**
     * Method sets alarm with interval. Alarm will fire from chosen date every given time (minute/hour)
     *
     * @param context application context
     * @param alarmID id of alarm
     */
    public void setIntervalAlarm(Context context, Long alarmID) {

        int interval, day, month, year, hour, currentHour, currentMinute, minute;
        Calendar calendar = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            alarm.setIsActive(true);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
            interval = alarm.getInterval();
            day = alarm.getDay();
            month = alarm.getMonth();
            year = alarm.getYear();
            hour = alarm.getHour();
            minute = alarm.getMinute();
            currentHour = now.get(Calendar.HOUR_OF_DAY);
            currentMinute = now.get(Calendar.MINUTE);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);

            //if alarm date is in the past
            if ((calendar.getTimeInMillis() - now.getTimeInMillis()) <= 0) {

                //if alarm date is still in the past
                while (calendar.getTimeInMillis() < now.getTimeInMillis()) {
                    //add interval until it reaches future
                    calendar.add(Calendar.HOUR_OF_DAY, interval);
                    outputProvider.displayDebugLog(TAG, "while = " + calendar.get(Calendar.HOUR_OF_DAY));
                }
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                //if alarm hour + interval is in the future subtract interval from hour and begin algorithm
                if (hour > now.get(Calendar.HOUR_OF_DAY))
                    hour -= interval;

                outputProvider.displayDebugLog(TAG, "hour = " + hour);
                outputProvider.displayDebugLog(TAG, "interval = " + interval);
                int newHour = currentHour - hour;
                outputProvider.displayDebugLog(TAG, "new hour = " + newHour);
                int newStartingHour = interval - newHour;
                outputProvider.displayDebugLog(TAG, "new starting hour = " + newStartingHour);
                hour = currentHour + newStartingHour;
                outputProvider.displayDebugLog(TAG, "hour = " + hour);
                if (hour == currentHour) {
                    if (minute < currentMinute)
                        hour += interval;
                }
                now.set(Calendar.HOUR_OF_DAY, hour);
                now.set(Calendar.MINUTE, minute);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmID);
                long alarmTimeInMillis = now.getTimeInMillis();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, 1000 * 60 * 60 * interval, pendingIntent);
                outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
                outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));

            } else {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmID);
                long alarmTimeInMillis = calendar.getTimeInMillis();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, 1000 * 60 * 60 * interval, pendingIntent);
                outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
                outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));
            }
        }
    }

    /**
     * Method sets single alarm what means it will fire only once at exact time and date.
     * You can re enable alarm by changing it's date in edit alarm option. Chosen pill will
     * stay with alarm.
     *
     * @param context application context
     * @param alarmID id of alarm
     */
    public void setSingleAlarm(Context context, Long alarmID) {

        int day, month, year;
        Calendar calendar = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        Alarm alarm = DatabaseRepository.getAlarmById(context, alarmID);
        if (alarm != null) {
            day = alarm.getDay();
            month = alarm.getMonth();
            year = alarm.getYear();

            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
            if ((calendar.getTimeInMillis() - now.getTimeInMillis()) <= 0) {
                outputProvider.displayShortToast(context.getString(R.string.toast_add_new_date_to_interval));
                alarm.setIsActive(false);
                DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
            } else {
                alarm.setIsActive(true);
                DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, alarmID);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(alarmID), intent, 0);
                long alarmTimeInMillis = calendar.getTimeInMillis();
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);
                outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
                outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));
            }
        }
    }

    public void setSnoozeAlarm(Context context, Long id) {
        SharedPreferences getAlarms = PreferenceManager.getDefaultSharedPreferences(context);
        int snoozeTime = Integer.parseInt(getAlarms.getString("snooze", "10"));


        Calendar now = Calendar.getInstance();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.EXTRA_LONG_ALARM_ID, id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, longToInt(id), intent, 0);
        long alarmTimeInMillis = now.getTimeInMillis() + (snoozeTime * 60000);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent);

        outputProvider.displayLongToast(context.getString(R.string.toast_alarm_will_fire_in) + buildString(alarmTimeInMillis));
        outputProvider.displayLog(TAG, "(snooze) alarmID == " + String.valueOf(id));
    }

    /**
     * Cancel every type of alarm. You can reactive alarm by setting it again with same alarm id
     *
     * @param context Activity context
     * @param id      alarm id
     */
    public void cancelAlarm(Context context, Long id) {
        Alarm alarm = DatabaseRepository.getAlarmById(context, id);
        if (alarm != null) {
            alarm.setIsActive(false);
            DatabaseHelper.getInstance(context).getAlarmDao().update(alarm);
        }
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, longToInt(id), intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        outputProvider.displayLog(TAG, "cancel alarm  id == " + String.valueOf(id));

    }

    public void startRingtone(Context context) {
        SharedPreferences getAlarms = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtone = getAlarms.getString("ringtone", "default ringtone");
        boolean isVibration = getAlarms.getBoolean("vibration", false);

        long[] pattern = {0, 200, 2000};

        mPlayer = MediaPlayer.create(context, Uri.parse(ringtone));
        mPlayer.setLooping(true);
        mPlayer.start();
        if (isVibration) {
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            mVibrator.vibrate(pattern, 0);
        }
    }

    private Long nextWeekDay(Calendar calendar) {
        /*LocalTime localTime = new LocalTime(calendar);
        LocalDate localDate = new LocalDate(calendar);
        outputProvider.displayLog(TAG, "LocalDate(calendar) == " + localDate.toDate());
        LocalDate nextWeek = localDate.plusDays(7);
        outputProvider.displayLog(TAG, "LocalDate + 1 week  == " + nextWeek.toDate() + ";  dayOfWeek == " + dayOfWeek);
        DateTime dateTime = nextWeek.toDateTime(localTime);
        outputProvider.displayLog(TAG, "DateTime(localTime) == " + dateTime.toDate());
        return dateTime.toInstant().getMillis();*/
        outputProvider.displayLog(TAG, "calendar == " + calendar.getTime());
        calendar.add(Calendar.DATE, 7);
        outputProvider.displayLog(TAG, "calendar == " + calendar.getTime());
        return calendar.getTimeInMillis();
    }

    /**
     * Stops annoying ringtone from ringing!!
     */
    public void stopRingtone() {
        if (mPlayer != null) mPlayer.stop();
        if (mVibrator != null) mVibrator.cancel();
    }

    /**
     * Converts Long value to int value.
     *
     * @param l Long value we want to transform.
     * @return Transformed int value.
     */
    private int longToInt(Long l) {
        return (int) (long) l;
    }

    private String buildString(long alarmTimeInMillis) {
        Calendar now = Calendar.getInstance();
        alarmTimeInMillis = alarmTimeInMillis - now.getTimeInMillis();
        int minutes = (int) (alarmTimeInMillis / (1000 * 60)) % 60;
        int hours = (int) ((alarmTimeInMillis / (1000 * 60 * 60)) % 24);
        int days = (int) ((alarmTimeInMillis / (1000 * 60 * 60)) / 24);
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days);
            sb.append(mContext.getString(R.string.days));
            sb.append(", ");
            hours = hours % 24;
            sb.append(hours);
            sb.append(mContext.getString(R.string.hours));
            sb.append(", ");
            sb.append(minutes);
            sb.append(mContext.getString(R.string.minutes));
        } else if (hours > 0) {
            sb.append(hours);
            sb.append(mContext.getString(R.string.hours));
            sb.append(", ");
            sb.append(minutes);
            sb.append(mContext.getString(R.string.minutes));
        } else {
            sb.append(minutes);
            sb.append(mContext.getString(R.string.minutes));
        }
        return sb.toString();
    }
}
