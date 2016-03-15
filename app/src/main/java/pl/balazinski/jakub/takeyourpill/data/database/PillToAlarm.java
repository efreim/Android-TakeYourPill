package pl.balazinski.jakub.takeyourpill.data.database;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class joins pill ids with alarm ids
 */
@DatabaseTable(tableName = "pilltoalarm")
public class PillToAlarm {

    @DatabaseField(columnName = "pillId")
    private Long pillId;

    @DatabaseField(columnName = "alarmId")
    private Long alarmId;

    public PillToAlarm() {

    }

    public PillToAlarm(Long alarmId, Long pillId) {
        this.alarmId = alarmId;
        this.pillId = pillId;
    }

    public Long getPillId() {
        return pillId;
    }

    public void setPillId(Long pillId) {
        this.pillId = pillId;
    }

    public Long getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(Long alarmId) {
        this.alarmId = alarmId;
    }
}
