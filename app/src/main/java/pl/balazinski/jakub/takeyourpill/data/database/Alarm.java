package pl.balazinski.jakub.takeyourpill.data.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "alarm")
public class Alarm {

    @DatabaseField(generatedId = true, columnName = "id")
    private Long mId;

    @DatabaseField(columnName = "hour")
    private int mHour;

    @DatabaseField(columnName = "minute")
    private int mMinute;

    @DatabaseField
    private int mInterval;

    @DatabaseField
    private int mUsageNumber;

    @DatabaseField(columnName = "day")
    private int mDay;

    @DatabaseField(columnName = "month")
    private int mMonth;

    @DatabaseField(columnName = "year")
    private int mYear;

    @DatabaseField(columnName = "active")
    private boolean mIsActive;

    @DatabaseField(columnName = "repeatable")
    private boolean mIsRepeatable;

    @DatabaseField
    private String mDaysRepeating;

    public Alarm() {
    }

    public Alarm(int hour, int minute, int interval, int usageNumber, int day, int month, int year, boolean isActive, boolean isRepeatable, String daysRepeating) {
        this.mHour = hour;
        this.mMinute = minute;
        this.mInterval = interval;
        this.mUsageNumber = usageNumber;
        this.mDay = day;
        this.mMonth = month;
        this.mYear = year;
        this.mIsActive = isActive;
        this.mIsRepeatable = isRepeatable;
        this.mDaysRepeating = daysRepeating;
    }

    /* public Alarm(int hour, int minute, int day, int month, int year, int usageNumber, boolean isActive, boolean isRepeatable, boolean isInterval, String ringtone, boolean isVibrating) {
        this.isActive = isActive;
        this.hour = hour;
        this.minute = minute;
        this.day = day;
        this.month = month;
        this.year = year;
        this.usageNumber = usageNumber;
        this.isRepeatable = isRepeatable;
        this.isInterval = isInterval;
        this.ringtone = ringtone;
        this.isVibrating = isVibrating;
    }*/

   /* public Alarm(int hour, int minute, boolean isActive) {
        this.isActive = isActive;
        this.hour = hour;
        this.minute = minute;
    }*/

    public boolean isActive() {
        return mIsActive;
    }

    public void setIsActive(boolean isActive) {
        this.mIsActive = isActive;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long mId) {
        this.mId = mId;
    }

    public int getHour() {
        return mHour;
    }

    public void setHour(int hour) {
        this.mHour = hour;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setMinute(int minute) {
        this.mMinute = minute;
    }

    public String getDaysRepeating() {
        return mDaysRepeating;
    }

    public void setDaysRepeating(String daysRepeating) {
        this.mDaysRepeating = daysRepeating;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        this.mYear = year;
    }

    public boolean isRepeatable() {
        return mIsRepeatable;
    }

    public void setIsRepeatable(boolean isRepeatable) {
        this.mIsRepeatable = isRepeatable;
    }

    public int getMonth() {
        return mMonth;
    }

    public void setMonth(int month) {
        this.mMonth = month;
    }

    public int getDay() {
        return mDay;
    }

    public void setDay(int day) {
        this.mDay = day;
    }

    public int getUsageNumber() {
        return mUsageNumber;
    }

    public void setUsageNumber(int usageNumber) {
        this.mUsageNumber = usageNumber;
    }

    public int getInterval() {
        return mInterval;
    }

    public void setInterval(int interval) {
        this.mInterval = interval;
    }
}
