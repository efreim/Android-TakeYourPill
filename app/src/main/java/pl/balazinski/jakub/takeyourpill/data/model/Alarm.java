package pl.balazinski.jakub.takeyourpill.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@DatabaseTable(tableName = "alarm")
public @Data
class Alarm {

    @DatabaseField(generatedId = true, columnName = "id")
    private Long id;

    @DatabaseField(columnName = "hour")
    private int hour;

    @DatabaseField(columnName = "minute")
    private int minute;

    @DatabaseField
    private int intervalTime;

    @DatabaseField
    private int usageNumber;

    @DatabaseField(columnName = "day")
    private int day;

    @DatabaseField(columnName = "month")
    private int month;

    @DatabaseField(columnName = "year")
    private int year;

    @DatabaseField(columnName = "active")
    private boolean active;

    @DatabaseField(columnName = "repeatable")
    private boolean repeatable;

    @DatabaseField(columnName = "interval")
    private boolean interval;

    @DatabaseField(columnName = "single")
    private boolean single;

    @DatabaseField
    private String daysRepeating;

    public Alarm() {
    }

    public Alarm(int hour, int minute, int intervalTime, int usageNumber, int day, int month, int year, boolean active, boolean repeatable, boolean isInterval, boolean single, String daysRepeating) {
        this.hour = hour;
        this.minute = minute;
        this.intervalTime = intervalTime;
        this.usageNumber = usageNumber;
        this.day = day;
        this.month = month;
        this.year = year;
        this.active = active;
        this.repeatable = repeatable;
        this.interval = isInterval;
        this.single = single;
        this.daysRepeating = daysRepeating;
    }
}
