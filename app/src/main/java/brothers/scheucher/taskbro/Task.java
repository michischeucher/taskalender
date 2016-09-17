package brothers.scheucher.taskbro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
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
    private int repeat; //repeat that task every x minutes

    private int overlapping_minutes; //just for calculation --- minutes overlapping with tasks in the future
    public int already_distributed_duration; //just for calculation
    public float filling_factor; //just for calculation
    private boolean not_created_by_user; //if set, then this will not be saved!

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
    public static final String DB_COL_REPEAT = "TaskRepeat";

    public static final String task_duration_description = TaskBroContainer.getContext().getResources().getString(R.string.task_duration_description);
    public static final String earliest_start_description = TaskBroContainer.getContext().getResources().getString(R.string.earliest_start);
    public static final String deadline_description = TaskBroContainer.getContext().getResources().getString(R.string.deadline_description);
    public static final String repeat_description = TaskBroContainer.getContext().getResources().getString(R.string.repeat_description);
    public static final String label_description = TaskBroContainer.getContext().getResources().getString(R.string.label_description);

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
        this.not_created_by_user = false;
        this.repeat = 0;
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
        this.not_created_by_user = false;
        this.repeat = 0;
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
        if (remaining_duration > 0) {
            this.done = false;
        }
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
            Label label = TaskBroContainer.getLabel(label_id);
            if (label != null) {
                ret += label.getName() + ", ";
            }
        }
        if (ret.length() < 2) {
            Log.d(tag, "getLabelString = " + ret);
            return ret;
        } else {
            Log.d(tag, "getLabelString = " + ret.substring(0, ret.length() - 2));
            return ret.substring(0, ret.length() - 2);
        }
    }
    public void setLabelString(String label_string) {
        if (label_ids == null)
            label_ids = new ArrayList<>();
        if (label_string == null || label_string.equals(""))
            return;
        Log.d(tag, "setLabelstring = " + label_string);

        String[] labels = label_string.split(";");
        for (String label : labels) {
            if (!label.equals("")) {
                label_ids.add(Integer.valueOf(label));
            }
        }

    }

    public String description() {
        return "id = " + id + " deadline = " + Util.getFormattedDateTime(deadline) + ", name = " + name + " duration in min = " + remaining_duration.description();
    }

    public void save(Context context) {
        if (id == -1) {
            id = TaskBroContainer.getNewTaskID();
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
            TaskBroContainer.deleteTaskFromList(this);
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
        checkLabelIds();
        if (this.label_ids.size() != 0) {
            return TaskBroContainer.getLabel(this.label_ids.get(0)).getColor();
        } else {
            return 0xE0E0E0;
        }
    }

    private void checkLabelIds() {
        ArrayList<Integer> ids_to_delete = new ArrayList<>();
        for (int i : label_ids) {
            Label label = TaskBroContainer.getLabel(i);
            if (label == null) {
                ids_to_delete.add(i);
            }
        }
        label_ids.removeAll(ids_to_delete);
    }

    public boolean hasLabel(int label_id) {
        if (this.label_ids.contains(label_id)) {
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
        if (this.repeat > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void calculateFillingFactor() {
        this.filling_factor = ((float)this.remaining_duration.getDuration()) / (this.remaining_duration.getDuration() + this.overlapping_minutes);
    }

    public Duration getWorkedTimeTillNow() {
        int worked_minutes = 0;
        for (MyEvent e : TaskBroContainer.getEvents()) {
            if (this == e.getTask() && !e.isNot_created_by_user()) {
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
        TextView done_view = ((TextView)rowView.findViewById(R.id.task_done));

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
            done_view.setTextColor(0xFFFFFFFF);
        }

        if (task.hasRepeat()) {
            name_view.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.stat_notify_sync_noanim, 0);
        }

        if (task.isDone()) {
            name_view.setPaintFlags(name_view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            notice_view.setVisibility(TextView.GONE);
            deadline_view.setVisibility(TextView.GONE);
            duration_view.setVisibility(TextView.GONE);
            done_view.setVisibility(TextView.VISIBLE);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskBroContainer.getContext(), ViewTask.class);
                Bundle b = new Bundle();
                b.putInt("id", task.getId());
                intent.putExtras(b);
                TaskBroContainer.getContext().startActivity(intent);
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
            this.remaining_duration.setDuration(0);
        } else {
            this.done = false;
        }
    }
    public void setDone(boolean done) {
        this.done = done;
        if (done) {
            this.remaining_duration.setDuration(0);
            TaskBroContainer.createCalculatingJob();
        }
    }

    public void setRepeat(int reapeat_minutes) {
        this.repeat = reapeat_minutes;
    }

    public int getRepeat() {
        return this.repeat;
    }

    public void setNotCreatedByUser(boolean not_created_by_user) {
        this.not_created_by_user = not_created_by_user;
    }

    public boolean isNotCreatedByUser() {
        return this.not_created_by_user;
    }

    public void addLabelId(int label_id_to_add) {
        if (label_id_to_add == -1) {
            return;
        }
        this.label_ids.add(label_id_to_add);
    }

    public boolean isRelevantRepeatForThatDay(GregorianCalendar date) {
        GregorianCalendar date_to_check = (GregorianCalendar)date.clone();
        Util.setTime(date_to_check, this.deadline);
        int difference_in_minutes = (int)((date_to_check.getTimeInMillis() - this.deadline.getTimeInMillis()) / 1000 / 60);
        if (difference_in_minutes < 0) {
            difference_in_minutes *= (-1);
            return false;
        }
        int tmp = difference_in_minutes % this.repeat;
        if (tmp < 24 * 60) {
            return true;
        } else {
            return false;
        }
    }
}
