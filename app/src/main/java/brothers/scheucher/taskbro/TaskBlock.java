package brothers.scheucher.taskbro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

/**
 * Created by Michael on 26.08.2015.
 */
//TaskBlock are blocks with overlapping tasks... there you should work concurrently
public class TaskBlock implements Comparable {
    private static final String tag = "TaskBlock";
    private GregorianCalendar start;//start where you could begin to work on the tasks in that block (where the system is telling you about the tasks)
    private GregorianCalendar last_possible_start;//where you have to beginn with the tasks in that block
    private GregorianCalendar end; //i think last deadline of the tasks.
    private int overlapping_time; //in minutes (how many minutes in this block are overlapping)
    private int potential; //in minutes (how many minutes there can be more work in that time...)
                            // should be the same as possible work time from start to end without all remainig duration from tasks


    private ArrayList<Task> tasks;

    public TaskBlock () {
        start = null;
        last_possible_start = null;
        end = null;
        overlapping_time = 0;
        potential = 0;

        tasks = new ArrayList<>();
    }

    public int getPotential() {
        return potential;
    }

    public void setPotential(int potential) {
        this.potential = potential;
    }

    public int getOverlapping_time() {
        return overlapping_time;
    }

    public void setOverlapping_time(int overlapping_time) {
        this.overlapping_time = overlapping_time;
    }

    public void addTask(Task task) {
        tasks.add(task);

    }

    public GregorianCalendar getStart() {
        return start;
    }

    public void setStart(GregorianCalendar start) {
        this.start = start;
    }

    public GregorianCalendar getLast_possible_start() {
        return last_possible_start;
    }

    public void setLast_possible_start(GregorianCalendar last_possible_start) {
        this.last_possible_start = last_possible_start;
    }

    public GregorianCalendar getEnd() {
        return end;
    }

    public void setEnd(GregorianCalendar end) {
        this.end = end;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public int compareTo(Object another) {
        TaskBlock tb = (TaskBlock) another;
        if (this.start == null) {
            return 1;
        }
        if (tb.getEnd() == null) {
            return -1;
        }
        return this.start.compareTo(tb.start);
    }

    public int getRemainingDurationOfTasks() {
        int ret = 0;
        for (Task t : tasks) {
            ret += t.getRemaining_duration();
        }
        return ret;
    }

    public void calculateTaskFillingFactors() {
        for (Task t : tasks) {
            t.calculateFillingFactor();
        }
    }


    public String description() {
        return "TaskBlock start: " + Util.getFormattedDateTime(this.start) + " end: " + Util.getFormattedDateTime(this.end) + " potential: " + potential + " #tasks: " + this.tasks.size() + " with durations(overall): " + getRemainingDurationOfTasks();
    }

    public boolean alreadyDistributed(ArrayList<Task> tasks_to_ignore) {
        int sum_already_distributed_duration = 0;
        int sum_remaining_duration = 0;
        for (Task t : tasks) {
            if (tasks_to_ignore != null && tasks_to_ignore.contains(t)) {
                continue;
            }
            sum_already_distributed_duration += t.already_distributed_duration;
            sum_remaining_duration += t.getRemaining_duration();
        }
        if (sum_already_distributed_duration == sum_remaining_duration) {
            return true;
        } else {
            return false;
        }
    }

    public void sortTasks() {
        Collections.sort(this.tasks);
    }
}
