package brothers.scheucher.taskbro;

import android.content.SharedPreferences;

/**
 * Created by michi on 20.09.2016.
 */
public class UserSettings {
    private static SharedPreferences.Editor shared_settings_editor;

    private static Duration STANDARD_DURATION_TASK;
    private static int STANDARD_DEADLINE_HOUR_IF_DATE_SET;
    private static int STANDARD_DEADLINE_MINUTE_IF_DATE_SET;

    private static final String SPREF_STANDARD_DURATION_TASK = "STANDARD_DURATION_TASK";
    private static final String SPREF_STANDARD_DEADLINE_HOUR_IF_DATE_SET = "STANDARD_DEADLINE_HOUR_IF_DATE_SET";
    private static final String SPREF_STANDARD_DEADLINE_MINUTE_IF_DATE_SET = "STANDARD_DEADLINE_MINUTE_IF_DATE_SET";

    public static void getAllSharedPreferencesOfUserSettings() {
        SharedPreferences shared_settings = TaskBroContainer.getContext().getSharedPreferences("UserSettings", 0);
        shared_settings_editor = shared_settings.edit();

        STANDARD_DURATION_TASK = new Duration(shared_settings.getInt(SPREF_STANDARD_DURATION_TASK, 60));
        STANDARD_DEADLINE_HOUR_IF_DATE_SET = shared_settings.getInt(SPREF_STANDARD_DEADLINE_HOUR_IF_DATE_SET, 23);
        STANDARD_DEADLINE_MINUTE_IF_DATE_SET = shared_settings.getInt(SPREF_STANDARD_DEADLINE_MINUTE_IF_DATE_SET, 0);

    }



    public static Duration getStandardDurationTask() {
        return STANDARD_DURATION_TASK;
    }

    public static void setStandardDurationTask(int minutes) {
        STANDARD_DURATION_TASK.setDuration(minutes);
        saveStandardDurationTask();
    }

    public static int getDeadlineHourIfDateSet() {
        return STANDARD_DEADLINE_HOUR_IF_DATE_SET;
    }

    public static void setStandardDeadlineHourIfDateSet(int standardDeadlineHourIfDateSet) {
        STANDARD_DEADLINE_HOUR_IF_DATE_SET = standardDeadlineHourIfDateSet;
        if (shared_settings_editor == null) {
            UserSettings.getAllSharedPreferencesOfUserSettings();
        }
        shared_settings_editor.putInt(SPREF_STANDARD_DEADLINE_HOUR_IF_DATE_SET, standardDeadlineHourIfDateSet);
        shared_settings_editor.commit();
    }

    public static int getDeadlineMinuteIfDateSet() {
        return STANDARD_DEADLINE_MINUTE_IF_DATE_SET;
    }

    public static void setStandardDeadlineMinuteIfDateSet(int standardDeadlineMinuteIfDateSet) {
        STANDARD_DEADLINE_MINUTE_IF_DATE_SET = standardDeadlineMinuteIfDateSet;
        if (shared_settings_editor == null) {
            UserSettings.getAllSharedPreferencesOfUserSettings();
        }
        shared_settings_editor.putInt(SPREF_STANDARD_DEADLINE_MINUTE_IF_DATE_SET, standardDeadlineMinuteIfDateSet);
        shared_settings_editor.commit();
    }

    public static void saveStandardDurationTask() {
        shared_settings_editor.putInt(SPREF_STANDARD_DURATION_TASK, STANDARD_DURATION_TASK.getDuration());
        shared_settings_editor.commit();
    }
}
