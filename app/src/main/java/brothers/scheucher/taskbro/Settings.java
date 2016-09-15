package brothers.scheucher.taskbro;

import java.util.GregorianCalendar;

/**
 * Created by Michael on 04.09.2015.
 */
public class Settings {
    private static final int earliest_start = 6 * 60; //in minutes TODO: wird noch ignoriert im TaskBroContainer-Algorithmus
    private static final int latest_end = 22 * 60; //in minutes
    private static final int task_time_per_day = 6 * 60; //in minutes

    private static final int pause_before_event = 30; //in minutes
    private static final int pause_after_event = 30; //in minutes
    private static final int pause_between_tasks = 15; //in minutes

    public static final int hours_before_deadline_urgent = 1; //hours before deadline...
    public static final int transparent_factor_urgent = 0xFF;
    public static final int hours_before_deadline_no_problem = 180; //hours before deadline...
    public static final int transparent_factor_no_problem = 0x40;
    public static final long THRESHOLD_MINUTES_FOR_NOW = 5; //when it is 2:34 and the date is 2:37 it is NOW ;)
    public static final int TEXT_COLOR_ATTENTION = 0xFFFF0000;
    public static final int TEXT_COLOR_PERFECT = 0xFF00B000;
    public static final int STANDARD_DURATION_TASK = 60;
    public static final int STANDARD_DEADLINE_HOUR_IF_DATE_SET = 23;
    public static final int STANDARD_DEADLINE_MINUTE_IF_DATE_SET = 0;
    public static final int STANDARD_EARLIEST_MINUTE = 60*8;
    public static final int STANDARD_LATEST_MINUTE = 60 * 21;

    public static int getPause_before_event() {
        return pause_before_event;
    }

    public static int getPause_after_event() {
        return pause_after_event;
    }

    public static int getPause_between_tasks() {
        return pause_between_tasks;
    }

    public static int getEarliest_start(GregorianCalendar current_date) {
        GregorianCalendar today = new GregorianCalendar();
        if (Util.isSameDate(today, current_date)) {
            return Util.getMinuteOfDay(today);
        }
        return earliest_start;
    }

    public static int getLatest_end() {
        return latest_end;
    }

    public static int getTask_time_per_day() {
        return task_time_per_day;
    }
}
