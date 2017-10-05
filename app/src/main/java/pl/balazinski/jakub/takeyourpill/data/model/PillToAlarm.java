package pl.balazinski.jakub.takeyourpill.data.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

/**
 * Class joins pill ids with alarm ids
 */
@DatabaseTable(tableName = "pilltoalarm")
public @Data
class PillToAlarm {

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

}
