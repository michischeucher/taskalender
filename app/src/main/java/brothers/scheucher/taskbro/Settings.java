package brothers.scheucher.taskbro;

/**
 * Created by Michael on 04.09.2015.
 */
public class Settings {
    private static final int pause_before_event = 30; //in minutes
    private static final int pause_after_event = 30; //in minutes
    private static final int pause_between_tasks = 15; //in minutes

    public static final long THRESHOLD_MINUTES_FOR_NOW = 5; //when it is 2:34 and the date is 2:37 it is NOW ;)
    public static final int COLOR_ATTENTION = 0xFFFF0000;
    public static final int COLOR_PERFECT = 0xFF00B000;

    private static final int DAYS_TO_LOOK_FORWARD = 7;

    public static int getPause_before_event() {
        return pause_before_event;
    }

    public static int getPause_after_event() {
        return pause_after_event;
    }

    public static int getPause_between_tasks() {
        return pause_between_tasks;
    }

    public static Duration getStandardDurationTask() {
        return UserSettings.getStandardDurationTask();
    }
    public static int getDeadlineHourIfDateSet() {
        return UserSettings.getDeadlineHourIfDateSet();
    }
    public static int getDeadlineMinuteIfDateSet() {
        return UserSettings.getDeadlineMinuteIfDateSet();
    }

    public static int getDaysToLookForward() {
        return DAYS_TO_LOOK_FORWARD;
    }

}
