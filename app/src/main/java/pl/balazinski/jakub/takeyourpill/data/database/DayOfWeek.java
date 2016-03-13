package pl.balazinski.jakub.takeyourpill.data.database;

//TODO Update database with this class

public class DayOfWeek {

    private Long alarmId;

    private int dayOfWeek;

    public DayOfWeek(){
    }

    public DayOfWeek(Long id, int day){
        this.alarmId = id;
        this.dayOfWeek = day;
    }

    public Long getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(Long alarmId) {
        this.alarmId = alarmId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
