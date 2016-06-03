package brothers.scheucher.taskalender;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

/**
 * Created by Michael on 08.07.2015.
 */
public class TimeRank {
    private static final String tag = "TimeRank";

    private static ArrayList<MyEvent> events;
    private static ArrayList<Task> tasks;
    private static ArrayList<Day> days;
    private static ArrayList<Label> labels;
    private static ArrayList<DaySettingObject> day_settings;

    private static ArrayList<TaskBlock> task_blocks;

    private static int last_task_id = -1;
    private static int last_event_id = -1;
    private static int last_label_id = -1;

    private static Context context;

    public static float scale_factor;


    public static void startApplication(Context context) {
        TimeRank.context = context;
        if (events != null) {
            return;
        }

        events = new ArrayList<MyEvent>();
        tasks = new ArrayList<Task>();
        labels = new ArrayList<Label>();
        days = new ArrayList<Day>();

        task_blocks = new ArrayList<TaskBlock>();

        scale_factor = 1.0f;

        //SQLiteStorageHelper.getInstance(context, 1).resetDatabase();
        SQLiteStorageHelper.getInstance(context, 1).addAllTasksFromDatabase();
        SQLiteStorageHelper.getInstance(context, 1).addAllEventsFromDatabase();
        SQLiteStorageHelper.getInstance(context, 1).addAllLabelsFromDatabase();

        TimeRank.createCalculatingJob();
    }

    public static void createCalculatingJob() {
        CalculateAsync calc_async = new CalculateAsync();
        calc_async.execute();
    }

    private static int getPossibleWorkTime(GregorianCalendar start_date, GregorianCalendar end_date) {
        int sum_work_time = 0;

        if (!(Util.isSameDate(start_date, end_date) && Util.isSameTime(start_date, end_date))) {//if there is really a slot! ;)
            for (GregorianCalendar current_date : Util.getListOfDates(start_date, end_date)) {
                Day day = TimeRank.getDay(current_date);
                if (day == null) {
                    day = TimeRank.createDay(current_date);
                }
                sum_work_time += day.getPossibleWorkTime(start_date, end_date);
            }
        }
        Log.d(tag, "getPossibleWorkTime for " + Util.getFormattedDateTime(start_date) + "-" + Util.getFormattedDateTime(end_date) + " = " + sum_work_time);
        return sum_work_time;
    }

    public static ArrayList<Label> getLabels() {
        return labels;
    }

    public static void setLabels(ArrayList<Label> labels) {
        TimeRank.labels = labels;
    }

    public static ArrayList<Task> getTasks() {
        return tasks;
    }

    public static void setTasks(ArrayList<Task> tasks) {
        TimeRank.tasks = tasks;
    }

    public static ArrayList<MyEvent> getEvents() {
        return events;
    }

    public static void setEvents(ArrayList<MyEvent> events) {
        TimeRank.events = events;
    }

    public static Context getContext() {
        return context;
    }


    public static int getNewTaskID() {
        if (last_task_id == -1) {
            for (Task task : tasks) {
                if (task.getId() > last_task_id) {
                    last_task_id = task.getId();
                }
            }
        }
        last_task_id += 1;
        return last_task_id;
    }

    public static int getNewEventID() {
            if (last_event_id == -1) {
                synchronized(context) {
                    for (MyEvent event : events) {
                        if (event.getId() > last_event_id) {
                            last_event_id = event.getId();
                        }
                    }
                }
        }
        last_event_id += 1;
        return last_event_id;
    }

    public static int getNewLabelID() {
        if (last_label_id == -1) {
            for (Label label : labels) {
                if (label.getId() > last_label_id) {
                    last_label_id = label.getId();
                }
            }
        }
        last_label_id += 1;
        return last_label_id;
    }


    public static void addTaskToList(Task new_task) {
        if (!tasks.contains(new_task)) {
            tasks.add(new_task);
            //printAllTasks();
        } else {
            Log.d(tag, "new_task already in list: " + new_task.description());
        }
    }
    public static void deleteTaskFromList(Task task) {
        if (tasks.contains(task)) {
            tasks.remove(task);
        }
    }


    public static void addEventToList(MyEvent new_event) {
        synchronized(context) {
            if (!events.contains(new_event)) {
                events.add(new_event);
            } else {
                Log.d(tag, "new_event already in list: " + new_event.description());
            }
        }
    }
    public static void deleteEventFromList(MyEvent event) {
        synchronized(context) {
            if (events.contains(event)) {
                events.remove(event);
            }
        }
    }

    public static void addDayToList(Day new_day) {
        if (!days.contains(new_day)) {
            days.add(new_day);
        } else {
            Log.d(tag, "new_day already in list: " + new_day.description());
        }
    }
    public static void deleteLabelFromList(Label label) {
        if (labels.contains(label)) {
            labels.remove(label);
        }
    }

    public static void addLabelToList(Label new_label) {
        if (!labels.contains(new_label)) {
            labels.add(new_label);
        } else {
            Log.d(tag, "new_label already in list: " + new_label.description());
        }
    }


    private static void printAllTasks() {
        Log.d(tag, "Going to print all " + TimeRank.getTasks().size() + " saved tasks...");
        for (Task task : tasks) {
            Log.d(tag, "task: " + task.description());
        }
    }

    public static ArrayList<Day> getDays() {
        return days;
    }

    public static void setDays(ArrayList<Day> days) {
        TimeRank.days = days;
    }

    public static int getLast_task_id() {
        return last_task_id;
    }

    public static void setLast_task_id(int last_task_id) {
        TimeRank.last_task_id = last_task_id;
    }

    public static int getLast_event_id() {
        return last_event_id;
    }

    public static void setLast_event_id(int last_event_id) {
        TimeRank.last_event_id = last_event_id;
    }

    public static Day getDay(GregorianCalendar date_to_search) {
        for (Day day : days) {
            if (Util.isSameDate(date_to_search, day.getStart())){
                return day;
            }
        }
        return null;
    }

    public static MyEvent getEvent(int id) {
        for (MyEvent e : events) {
            if (e.getId() == id) {
                return e;
            }
        }
        return new MyEvent();
    }

    public static Task getTask(int id) {
        for (Task t : tasks) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public static Label getLabel(int label_id) {
        for (Label label : labels) {
            if (label.getId() == label_id) {
                return label;
            }
        }
        return null;
    }

    public static ArrayList<Label> getLabelSequence() {
        Collections.sort(labels);
        ArrayList<Label> ret = new ArrayList<Label>();

        for (Label l : labels) {
            if (l.getParent() == null) { //only top level labels
                ret.addAll(getRecursiveLabelStructure(l));
            }
        }
        return ret;
    }

    private static ArrayList<Label> getRecursiveLabelStructure(Label label) {
        Log.d(tag, "getRecursiveLabelStructure called");
        ArrayList<Label> ret = new ArrayList<Label>();
        ArrayList<Label> childs = label.getChildLabels();
        ret.add(label);
        for (Label l : childs) {
            ret.addAll(getRecursiveLabelStructure(l));
        }
        return ret;
    }


    public static ArrayList<Task> getTasks(int label_id) {
        Collections.sort(labels);
        ArrayList<Task> tasks_with_that_label = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.hasLabel(label_id)) {
                tasks_with_that_label.add(task);
            } else if (label_id == -1 && task.getLabelIds().size() == 0) { //task has no labels...
                tasks_with_that_label.add(task);
            }
        }
        return tasks_with_that_label;
    }

    public static ArrayList<Label> getRootLabels() {
        Collections.sort(labels);
        ArrayList<Label> ret = new ArrayList<Label>();
        for (Label l : labels) {
            if (l.getParent() == null) {
                ret.add(l);
            }
        }
        return ret;
    }

    public static void addDaySettingObject(DaySettingObject new_day_setting) {
        if (day_settings == null) {
            day_settings = new ArrayList<DaySettingObject>();
        }
        if (!day_settings.contains(new_day_setting)) {
            day_settings.add(new_day_setting);
        }
    }
    //TODO: returns the correct daysettingobjet for that specific date
    public static DaySettingObject getDaySettingObject(GregorianCalendar date) {
        DaySettingObject most_relevant = null;
        int max_relevant = -2;
        if (day_settings != null) {
            for (DaySettingObject ds : day_settings) {
                int relavant_value = ds.howRelevant(date);
                if (relavant_value > max_relevant) {
                    max_relevant = relavant_value;
                    most_relevant = ds;
                }
            }
            return most_relevant;
        } else {
            return new DaySettingObject();
        }
    }

    public static Day createDay(GregorianCalendar date) {
        Day new_day = new Day(date);
        synchronized(context) {
            for (MyEvent e : TimeRank.getEvents()) {
                if (e.isRelevantFotThatDay(date)) {
                    new_day.addEvent(e);
                }
            }
        }

        ArrayList events_from_other_calendars = MyCalendarProvider.getEvents(new_day.getStart().getTimeInMillis(), new_day.getEnd().getTimeInMillis());
        new_day.addAllEvents(events_from_other_calendars);
        synchronized(context) {
            events.addAll(events_from_other_calendars);
        }

        days.add(new_day);
        return new_day;
    }

    public static ArrayList<TaskBlock> getTaskBlocks() {
        return task_blocks;
    }

    public static void calculateDays() {
        Log.d(tag, "# START createCalculatingJob()");

        //START from last deadline... calculating blocks
        if (tasks.size() <= 0) {
            return;
        }
        for (Task t : tasks) {
            t.resetJustForCalculations();
        }

        //creating all days where there is a event
        days.clear();
        /*for (MyEvent event : events) {
            for (GregorianCalendar date : event.getDates()) {
                if (TimeRank.getDay(date) == null) {
                    TimeRank.createDay(date);
                }
            }
        }*/

        Log.d(tag, "##### START CALCULATING BLOCKS #####");
        Collections.sort(tasks, Collections.reverseOrder());
        //TimeRank.printAllTasks();

        task_blocks.clear();

        //first block
        TaskBlock block = new TaskBlock();
        block.setEnd((GregorianCalendar) tasks.get(0).getDeadline().clone());
        task_blocks.add(block);

        boolean last_task = false;
        GregorianCalendar earlier_date;
        int possible_work_time_for_current_task = 0;
        int overlapping_time = 0;

        for (int i = 0; i < TimeRank.getTasks().size(); i++) {
            Task current_task = tasks.get(i);
            current_task.setOverlapping_minutes(block.getOverlapping_time());
            block.addTask(current_task);

            if ((i + 1) < TimeRank.getTasks().size()) {
                earlier_date = (GregorianCalendar)tasks.get(i + 1).getDeadline().clone();
            } else { // if last task there is only now... ;)
                earlier_date = new GregorianCalendar();
                last_task = true;
            }

            possible_work_time_for_current_task = TimeRank.getPossibleWorkTime(current_task.getDeadline(), earlier_date);
            overlapping_time = current_task.getRemaining_duration() + current_task.getOverlapping_minutes() - possible_work_time_for_current_task;

            block.setOverlapping_time(overlapping_time);
            block.setStart(earlier_date);               //every time a new task added?! hmm...
            block.setPotential((-1) * overlapping_time);//every time a new task added?! hmm...

            if (overlapping_time < 0 && !last_task) { //if everything is possible in that block...
                block = new TaskBlock();
                block.setEnd((GregorianCalendar) earlier_date.clone());
                task_blocks.add(block);
            } else if (overlapping_time > 0 && last_task) {
                Log.d(tag, "########### PROBLEM: Too much todo for too less time... :( possible_work_time = " + possible_work_time_for_current_task + " for that task must be free = " + current_task.getRemaining_duration() + " with overlapping = " + current_task.getOverlapping_minutes());
            }
        }

        Log.d(tag, "##### END CALCULATING BLOCKS #####");

/*        Log.d(tag, "--- printing TaskBlocks --- size = " + task_blocks.size());
        for (TaskBlock tb : task_blocks) {
            Log.d(tag, "start: " + Util.getFormattedDateTime(tb.getStart()) + " end: " + Util.getFormattedDateTime(tb.getEnd()));
            Log.d(tag, "   potential: " + tb.getPotential() + " overlapping_time = " + tb.getOverlapping_time() +  " #tasks: " + tb.getTasks().size() + " tasks(remaining_durations) = " + tb.getRemainingDurationOfTasks());
        }
        Log.d(tag, "--- end printing TaskBlocks ---");
*/
        //CALCULATING DAYS with TASKS...
        Log.d(tag, "##### START CALCULATING DAYS with TASKS #####");
        Day last_day_where_time_left = null;
        for (int i = task_blocks.size() - 1; i >= 0; i--) { //start with the newest block
            TaskBlock tb = task_blocks.get(i);
            tb.calculateTaskFillingFactors();
            if (i == (task_blocks.size() - 1)) { //first block must start with today
                last_day_where_time_left = tb.addTasksToDays(new GregorianCalendar());
            } else { //otherwise it starts with the end of the other block
                last_day_where_time_left = tb.addTasksToDays(last_day_where_time_left.getStart());
            }
        }

        Log.d(tag, "##### END CALCULATING DAYS with TASKS #####");
    }
}
