package brothers.scheucher.taskalender;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Michael on 07.07.2015.
 */
public class Task implements Comparable {
    private static final String tag = "Task";

    //attributes
    private int id;
    private String name;
    private String notice;
    private Duration remaining_duration;
    private GregorianCalendar earliest_start;
    private GregorianCalendar deadline;
    private boolean done;

    private int overlapping_minutes; //just for calculation --- minutes overlapping with tasks in the future
    public int already_distributed_duration; //just for calculation
    public float filling_factor; //just for calculation

    private ArrayList<Integer> label_ids;

    //database
    public static final String DB_TABLE = "TaskTable";
    public static final String DB_COL_ID = "TaskID";
    public static final String DB_COL_NAME = "TaskName";
    public static final String DB_COL_NOTICE = "TaskNotice";
    public static final String DB_COL_DURATION = "TaskDuration";
    public static final String DB_COL_EARLIEST_START = "TaskEarliestStart";
    public static final String DB_COL_DEADLINE = "TaskDeadline";
    public static final String DB_COL_LABELS = "TaskLabels";
    public static final String DB_COL_DONE = "TaskDone";

    public static final String task_duration_description = TimeRank.getContext().getResources().getString(R.string.task_duration_description);
    public static final String earliest_start_description = TimeRank.getContext().getResources().getString(R.string.earliest_start);
    public static final String deadline_description = TimeRank.getContext().getResources().getString(R.string.deadline_description);
    public static final String repeat_description = TimeRank.getContext().getResources().getString(R.string.repeat_description);
    public static final String label_description = TimeRank.getContext().getResources().getString(R.string.label_description);

    public Task() {
        int new_id = -1;
        this.name = "";
        this.notice = "";
        this.id = new_id;
        this.earliest_start = new GregorianCalendar();
        this.earliest_start.set(GregorianCalendar.HOUR, 0);
        this.earliest_start.set(GregorianCalendar.MINUTE, 0);
        this.deadline = null;
        this.remaining_duration = new Duration(0);
        this.label_ids = new ArrayList<>();
        this.done = false;

        this.overlapping_minutes = 0;
        this.already_distributed_duration = 0;
        this.filling_factor = 0;
    }

    public Task(int id) {
        this.id = id;
        this.earliest_start = new GregorianCalendar();
        this.deadline = null;
        this.already_distributed_duration = 0;
        this.overlapping_minutes = 0;
        this.remaining_duration = new Duration(0);
        this.filling_factor = 0;
        this.label_ids = new ArrayList<Integer>();
        this.done = false;
    }

    public void resetJustForCalculations() {
        this.already_distributed_duration = 0;
        this.overlapping_minutes = 0;
        this.filling_factor = 0;
    }

    public int getOverlapping_minutes() {
        return overlapping_minutes;
    }

    public void setOverlapping_minutes(int overlapping_minutes) {
        this.overlapping_minutes = overlapping_minutes;
    }

    public float getFilling_factor() {
        return filling_factor;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public int getRemaining_duration() {
        return remaining_duration.getDuration();
    }

    public void setRemaining_duration(int remaining_duration) {
        this.remaining_duration.setDuration(remaining_duration);
    }

    public GregorianCalendar getDeadline() {
        return deadline;
    }

    public void setDeadline(GregorianCalendar deadline) {
        this.deadline = deadline;
    }

    public String getLabelIdsString() {
        if (label_ids == null) {
            label_ids = new ArrayList<>();
        }
        String ret_val = "";
        for (int label_id : label_ids) {
            ret_val += label_id + ";";

        }
        return ret_val;
    }

    public String getLabelString() {
        String ret = "";
        for (int label_id : label_ids) {
            Label label = TimeRank.getLabel(label_id);
            if (label != null) {
                ret += label.getName() + ", ";
            }
        }
        if (ret.length() < 2) {
            return ret;
        } else {
            return ret.substring(0, ret.length() - 2);
        }
    }
    public void setLabelString(String label_string) {
        if (label_ids == null)
            label_ids = new ArrayList<>();
        if (label_string == null || label_string.equals(""))
            return;

        String[] labels = label_string.split(";");
        for (String label : labels) {
            if (label.equals("")) {
                label_ids.add(Integer.valueOf(label));
            }
        }

    }

    public String description() {
        return "id = " + id + " deadline = " + Util.getFormattedDateTime(deadline) + ", name = " + name + " duration in min = " + remaining_duration.description();
    }

    public void save(Context context) {
        if (id == -1) {
            id = TimeRank.getNewTaskID();
        }
        SQLiteStorageHelper db_helper = SQLiteStorageHelper.getInstance(context, 1);
        db_helper.openDB();
        db_helper.saveTask(this);
        db_helper.closeDB();
        db_helper.close();
    }

    public void delete(Context context) {
        if (id != -1) {
            SQLiteStorageHelper db_helper = SQLiteStorageHelper.getInstance(context, 1);
            db_helper.openDB();
            db_helper.deleteTask(this);
            db_helper.closeDB();
            db_helper.close();
            TimeRank.deleteTaskFromList(this);
        }

    }


    @Override
    public int compareTo(Object another) {
        Task other = (Task)another;
        if (this.deadline == null) {
            return 1;
        } else if (other.deadline == null) {
            return -1;
        }
        return this.deadline.compareTo(other.deadline);
    }

    public void setDeadline(int hourOfDay, int minute) {
        if (this.deadline == null) {
            deadline = new GregorianCalendar();
        }
        Util.setTime(this.deadline, hourOfDay, minute);
    }

    public void setDeadline(int year, int monthOfYear, int dayOfMonth) {
        if (this.deadline == null) {
            this.deadline = new GregorianCalendar();
            this.deadline.set(GregorianCalendar.HOUR_OF_DAY, Settings.STANDARD_DEADLINE_HOUR_IF_DATE_SET);
            this.deadline.set(GregorianCalendar.MINUTE,Settings.STANDARD_DEADLINE_MINUTE_IF_DATE_SET);
        }
        Util.setDate(this.deadline, year, monthOfYear, dayOfMonth);
    }

    public int getColor() {
        if (this.label_ids.size() != 0) {
            return TimeRank.getLabel(this.label_ids.get(0)).getColor();
        } else {
            return 0xE0E0E0;
        }
    }

    public boolean hasLabel(int label_id) {
        if (this.label_ids.contains(Integer.valueOf(label_id))) {
            return true;
        } else {
            return false;
        }
    }

    public void setLabelIds(ArrayList<Integer> label_ids) {
        this.label_ids = label_ids;
    }

    public ArrayList<Integer> getLabelIds() {
        return this.label_ids;
    }

    public void setEarliestStart(GregorianCalendar date) {
        this.earliest_start = date;
    }
    public GregorianCalendar getEarliestStart() {
        return this.earliest_start;
    }

    public Duration getDuration() {
        return remaining_duration;
    }

    public boolean hasRepeat() {
        return false;
    }

    public void calculateFillingFactor() {
        this.filling_factor = ((float)this.remaining_duration.getDuration()) / (this.remaining_duration.getDuration() + this.overlapping_minutes);
    }

    public Duration getWorkedTimeTillNow() {
        int worked_minutes = 0;
        for (MyEvent e : TimeRank.getEvents()) {
            if (this == e.getTask() && Util.earlierDate(e.getEnd(),new GregorianCalendar())) {
                worked_minutes += e.getDurationInMinutes();
            }
        }
        Duration duration = new Duration(worked_minutes);
        return duration;
    }

    public View createTaskInListView(LayoutInflater inflater, LinearLayout parent) {
        final Task task = this;

        View rowView = inflater.inflate(R.layout.task_in_list, parent, false);

        TextView name_view = (TextView) rowView.findViewById(R.id.task_name);
        name_view.setText(task.getName());
        TextView notice_view = (TextView)rowView.findViewById(R.id.task_notice);
        if (!task.getNotice().equals("")) {
            notice_view.setVisibility(TextView.VISIBLE);
            notice_view.setText(task.getNotice());
        }
        TextView duration_view = ((TextView)rowView.findViewById(R.id.task_duration));
        duration_view.setText(Util.getFormattedDuration(task.getRemaining_duration()));
        TextView deadline_view = (TextView)rowView.findViewById(R.id.task_deadline);
        if (!(task.getDeadline() == null)) {
            deadline_view.setVisibility(TextView.VISIBLE);
            deadline_view.setText(Util.getFormattedDateTimeNotExact(task.getDeadline()));
        }
        Util.setColorOfDrawable(rowView, 0xA0000000 | task.getColor());
        if (Util.isDarkColor(task.getColor())) {
            name_view.setTextColor(0xFFFFFFFF);
            duration_view.setTextColor(0xFFFFFFFF);
            notice_view.setTextColor(0xFFFFFFFF);
            deadline_view.setTextColor(0xFFFFFFFF);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimeRank.getContext(), ViewTask.class);
                Bundle b = new Bundle();
                b.putInt("id", task.getId());
                intent.putExtras(b);
                TimeRank.getContext().startActivity(intent);
            }
        });

        return rowView;
    }

    public int getDone() {
        if (done) {
            return 1;
        } else {
            return -1;
        }
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(int done_int) {
        if (done_int > 0) {
            this.done = true;
        } else {
            this.done = false;
        }
    }
}
