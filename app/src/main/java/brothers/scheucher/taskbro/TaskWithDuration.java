package brothers.scheucher.taskbro;

/**
 * Created by michi on 27.09.2016.
 */
public class TaskWithDuration implements Comparable {
    private Task task;
    private Duration duration;

    public TaskWithDuration(Task task, Duration duration) {
        this.task = task;
        this.duration = duration;
    }

    public TaskWithDuration(Task task, int duration_in_minutes) {
        this.task = task;
        this.duration = new Duration(duration_in_minutes);
    }

    public int getDurationInMinutes() {
        return this.duration.getDuration();
    }

    public Task getTask() {
        return task;
    }

    public void addDuration(int work_time_for_that_task) {
        this.duration.addMinutes(work_time_for_that_task);
    }

    @Override
    public int compareTo(Object another) {
        TaskWithDuration twd = (TaskWithDuration) another;
        int ret = this.getTask().compareTo(twd.getTask());
        if (ret != 0) {
            return ret;
        } else {
            int difference = this.getDurationInMinutes() - twd.getDurationInMinutes();
            if (difference == 0) {
                return 0;
            } else if (difference < 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public String description() {
        return "Task: " + this.task.description() + " duration: " + this.duration.description();
    }

    public void setDuration(int duration_in_minutes) {
        this.duration.setDuration(duration_in_minutes);
    }
}
