package brothers.scheucher.taskbro;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class DaySettingObject {
    private static final String tag = "DaySettingObject";

    private int id;
    private Duration total_duration;
    private int earliest_minute;
    private int latest_minute;
    private ArrayList<Integer> working_days;

    private ArrayList<Pair<Label, Duration>> labels_durations;

    //database
    public static final String DB_TABLE = "DaySettingTable";
    public static final String DB_COL_ID = "DaySettingID";
    public static final String DB_COL_TOTALDURATION = "DaySettingTotalDuration";
    public static final String DB_COL_EARLIESTMINUTE = "DaySettingEarliestMinute";
    public static final String DB_COL_LATESTMINUTE = "DaySettingLatestMinute";
    public static final String DB_COL_WORKING_DAYS = "DaySettingWorkingDays";


    private ArrayList<MyConstraint> constraints;

    public DaySettingObject() {
        this.id = TaskBroContainer.getNewDaySettingObjectID();
        this.total_duration = new Duration(60 * 6);
        this.labels_durations = new ArrayList<>();
        this.earliest_minute = 60 * 8;
        this.latest_minute = 60 * 20;
        this.working_days = new ArrayList<>();
        this.working_days.add(0);
        this.working_days.add(1);
        this.working_days.add(2);
        this.working_days.add(3);
        this.working_days.add(4);

    }
    public DaySettingObject(int id) {
        this.id = id;
        this.total_duration = new Duration(60 * 6);
        this.labels_durations = new ArrayList<>();
        this.earliest_minute = 60 * 8;
        this.latest_minute = 60 * 20;
        this.working_days = new ArrayList<>();
        this.working_days.add(0);
        this.working_days.add(1);
        this.working_days.add(2);
        this.working_days.add(3);
        this.working_days.add(4);
    }

    public String getWorkingDaysIdsString() {
        if (working_days == null) {
            working_days = new ArrayList<Integer>();
        }
        String ret_val = "";
        for (int working_day_id : working_days) {
            ret_val += working_day_id + ";";
        }
        return ret_val;
    }

    public void setWorkingDaysString(String working_days_string) {
        if (working_days == null)
            working_days = new ArrayList<Integer>();
        if (working_days_string == null)
            return;
        working_days.clear();
        String[] working_day_ids = working_days_string.split(";");
        for (String working_day_id : working_day_ids) {
            if (working_day_id != "") {
                working_days.add(Integer.valueOf(working_day_id));
            }
        }

    }



    public void addLabelDuration(Label label, Duration duration_of_that_label) {
        Pair label_duration = new Pair(label, duration_of_that_label);
        this.labels_durations.add(label_duration);
    }

    public Duration addLabelDurationIfNotExist(Label label) {
        for (Pair<Label, Duration> label_duration : this.labels_durations) {
            if (label_duration.first.equals(label)) {
                return label_duration.second;
            }
        }
        Log.d(tag, "Label not found, therefore insert in labels_durations labellist label = " + label.getName());
        Duration duration_for_that_label = new Duration(0);
        Pair label_duration = new Pair(label, duration_for_that_label);
        this.labels_durations.add(label_duration);
        return duration_for_that_label;
    }

    public Duration getTotal_duration() {
        return total_duration;
    }

    public void setTotal_duration(Duration total_duration) {
        this.total_duration = total_duration;
    }

    public ArrayList<Pair<Label, Duration>> getLabels_durations() {
        return labels_durations;
    }


    public int getTotalDurationInMinutes() {
        if (this.total_duration != null) {
            return this.total_duration.getDuration();
        } else {
            return 0;
        }
    }

    public void addConstraint(MyConstraint new_constraint) {
        if(this.constraints == null) {
            this.constraints = new ArrayList<>();
        }
        this.constraints.add(new_constraint);
    }

    public int howRelevant(GregorianCalendar date) {
        int max_relevant = -1;
        if (this.constraints == null) {
            return max_relevant;
        }
        for (MyConstraint con : this.constraints) {
            max_relevant = Math.max(con.howRelevant(date), max_relevant);
        }
        return max_relevant;
    }

    public int getId() {
        return id;
    }

    public String description() {
        return "DaySettingObject: total_duration = " + Util.getFormattedDuration(this.getTotalDurationInMinutes());
    }

    public void save(Context context) {
        if (id == -1) {
            id = TaskBroContainer.getNewDaySettingObjectID();
        }
        SQLiteStorageHelper db_helper = SQLiteStorageHelper.getInstance(context, 1);
        db_helper.openDB();
        db_helper.saveDaySettingObject(this);
        db_helper.closeDB();
        db_helper.close();

    }

    public void setTotalDuration(int total_duration_in_minutes) {
        this.total_duration.setDuration(total_duration_in_minutes);
    }

    public int getEarliest_minute() {
        return earliest_minute;
    }

    public void setEarliest_minute(int earliest_minute) {
        this.earliest_minute = earliest_minute;
    }

    public int getLatest_minute() {
        return latest_minute;
    }

    public void setLatest_minute(int latest_minute) {
        this.latest_minute = latest_minute;
    }

    public boolean isWorkingDay(int day_in_week) {
        if (this.working_days.contains(day_in_week)) {
            return true;
        } else {
            return false;
        }
    }

    public void setWorkingDays(boolean[] workingDays) {
        this.working_days.clear();
        for (int i = 0; i < workingDays.length; i++) {
            if (workingDays[i]) {
                this.working_days.add(i);
            }
        }
    }

    public boolean[] getWorkingDaysBoolean() {
        boolean[] ret_val = new boolean[SettingDay.NUM_DAYS_OF_WEEK];
        for (int i = 0; i < ret_val.length; i++) {
            if (working_days.contains(i)) {
                ret_val[i] = true;
            } else {
                ret_val[i] = false;
            }
        }

        return ret_val;
    }

    public String getWorkingDaysString() {
        String[] working_day_strings = TaskBroContainer.getContext().getResources().getStringArray(R.array.week_names);
        String ret_val = "";

        boolean until = false;
        boolean first_day = true;

        for (int i = 0; i < working_day_strings.length; i++) {
            if (until) {
                if (!working_days.contains(i+1)) {
                    //until now
                    ret_val += working_day_strings[i];
                    until = false;
                }
            } else if (working_days.contains(i)) {
                if (first_day) {
                    first_day = false;
                } else {
                    ret_val += ", ";
                }

                if (working_days.contains(i+1) && working_days.contains(i+2)) { //if there is a sequence
                    until = true;
                    ret_val += working_day_strings[i] + " - ";
//                    i = i+2;
                } else {
                    ret_val += working_day_strings[i];
                }
            }
        }
        return ret_val;
    }
}
