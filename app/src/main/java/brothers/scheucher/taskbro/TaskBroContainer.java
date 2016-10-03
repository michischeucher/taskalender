package brothers.scheucher.taskbro;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Michael on 08.07.2015.
 */
public class TaskBroContainer {
    private static final String tag = "TaskBroContainer";

    private static ArrayList<MyEvent> events;
    private static ArrayList<MyEvent> inactive_events;
    private static ArrayList<Task> tasks;
    private static ArrayList<Task> inactive_tasks;
    private static ArrayList<Day> days;
    private static ArrayList<Label> labels;
    private static ArrayList<DaySettingObject> day_settings;

    private static ArrayList<TaskBlock> task_blocks;
    private static ArrayList<Task> repeating_tasks;

    private static ReentrantReadWriteLock events_lock;
    private static ReentrantReadWriteLock inactive_events_lock;
    private static ReentrantReadWriteLock tasks_lock;
    private static ReentrantReadWriteLock inactive_tasks_lock;
    private static ReentrantReadWriteLock days_lock;
    private static ReentrantReadWriteLock labels_lock;
    private static ReentrantReadWriteLock day_settings_lock;
    private static ReentrantReadWriteLock task_blocks_lock;
    private static ReentrantReadWriteLock repeating_tasks_lock;


    private static int last_task_id = -1;
    private static int last_event_id = -1;
    private static int last_label_id = -1;
    private static int last_day_setting_id = -1;

    private static Context context;

    public static float scale_factor;

    public static int PERMISSIONS_REQUEST_READ_CALENDER;



    public static void initApplication(Activity activity) {
        context = activity;
        if (events != null) {
            return;
        }

        events = new ArrayList<>();
        inactive_events = new ArrayList<>();
        tasks = new ArrayList<>();
        inactive_tasks = new ArrayList<>();
        labels = new ArrayList<>();
        days = new ArrayList<>();
        day_settings = new ArrayList<>();
        repeating_tasks = new ArrayList<>();
        task_blocks = new ArrayList<>();

        events_lock = new ReentrantReadWriteLock();
        inactive_events_lock = new ReentrantReadWriteLock();
        tasks_lock = new ReentrantReadWriteLock();
        inactive_tasks_lock = new ReentrantReadWriteLock();
        days_lock = new ReentrantReadWriteLock();
        labels_lock = new ReentrantReadWriteLock();
        day_settings_lock = new ReentrantReadWriteLock();
        task_blocks_lock = new ReentrantReadWriteLock();
        repeating_tasks_lock = new ReentrantReadWriteLock();

        scale_factor = 1.0f;

    }

    public static void startApplication(Activity activity) {
        restoreScaleFactor();

        SQLiteStorageHelper sql_helper = SQLiteStorageHelper.getInstance(context, 1);

        sql_helper.addAllTasksFromDatabase();
        sql_helper.addAllEventsFromDatabase();
        sql_helper.addAllLabelsFromDatabase();
        sql_helper.addAllDaySettingObjectsFromDatabase();

        UserSettings.getAllSharedPreferencesOfUserSettings();

    }

    public static void createCalculatingJob(Activity activity) {
        CalculateAsync calc_async = new CalculateAsync();
        calc_async.execute(activity);
/* is doing that in background:
        calculateDays(activity);
        Calender.notifyChanges();
        MainActivity.notifyChanges();
        PotentialActivity.notifyChanges();
*/

    }


    private static int getPossibleWorkTime(Activity activity, GregorianCalendar start_date, GregorianCalendar end_date, boolean set_worked) {
        int sum_work_time = 0;

        if (!(Util.isSameDate(start_date, end_date) && Util.isSameTime(start_date, end_date))) {//if there is really a slot! ;)
            for (GregorianCalendar current_date : Util.getListOfDates(start_date, end_date)) {
                Day day = TaskBroContainer.getDay(current_date);
                if (day == null) {
                    day = TaskBroContainer.createDay(activity, current_date);
                }
                int possible_work_time_in_that_day = day.getPossibleWorkTime(start_date, end_date, set_worked);
                //day.addDistributedWorkTime(possible_work_time_in_that_day);
                sum_work_time += possible_work_time_in_that_day;
            }
        }
        return sum_work_time;
    }

    public static ArrayList<Label> getLabels() {
        return labels;
    }

    public static ArrayList<Task> getTasks() {
        return tasks;
    }

    public static ArrayList<Task> getTasksNotDone(boolean no_repeating_tasks) {
        ArrayList<Task> tasks_not_done = new ArrayList<>();
        tasks_lock.readLock().lock();
        for (Task t : tasks) {
            if (no_repeating_tasks && t.getRepeat() != 0) {
                continue;
            }
            if (!t.isDone()) {
                tasks_not_done.add(t);
            }
        }
        tasks_lock.readLock().unlock();

        return tasks_not_done;
    }

    public static ArrayList<Task> getTasksNotDone(int label_id, boolean no_repeating_tasks) {
        ArrayList<Task> tasks_not_done = new ArrayList<>();

        tasks_lock.readLock().lock();
        for (Task t : tasks) {
            if (no_repeating_tasks && t.getRepeat() != 0) {
                continue;
            }
            if (!t.isDone() && t.hasLabel(label_id)) {
                tasks_not_done.add(t);
            }
            if (label_id == -1 && t.getLabelIds().size() == 0) {
                tasks_not_done.add(t);
            }
        }
        tasks_lock.readLock().unlock();

        return tasks_not_done;
    }

    public static ArrayList<Task> getTasksDone(boolean no_repeating_tasks) {
        ArrayList<Task> tasks_done = new ArrayList<>();

        tasks_lock.readLock().lock();
        for (Task t : tasks) {
            if (no_repeating_tasks && t.getRepeat() != 0) {
                continue;
            }
            if (t.isDone()) {
                tasks_done.add(t);
            }
        }
        tasks_lock.readLock().unlock();

        return tasks_done;
    }

    public static ArrayList<MyEvent> getEvents() {
        return events;
    }

    public static Context getContext() {
        return context;
    }

    public static int getNewTaskID() {
        if (last_task_id == -1) {
            tasks_lock.readLock().lock();
            for (Task task : tasks) {
                if (task.getId() > last_task_id) {
                    last_task_id = task.getId();
                }
            }
            tasks_lock.readLock().unlock();

            inactive_tasks_lock.readLock().lock();
            for (Task task : inactive_tasks) {
                if (task.getId() > last_task_id) {
                    last_task_id = task.getId();
                }
            }
            inactive_tasks_lock.readLock().unlock();
        }
        last_task_id += 1;
        return last_task_id;
    }

    public static int getNewEventID() {
            if (last_event_id == -1) {
                events_lock.readLock().lock();
                for (MyEvent event : events) {
                    if (event.getId() > last_event_id) {
                        last_event_id = event.getId();
                    }
                }
                events_lock.readLock().unlock();
                inactive_events_lock.readLock().lock();
                for (MyEvent event : inactive_events) {
                    if (event.getId() > last_event_id) {
                        last_event_id = event.getId();
                    }
                }
                inactive_events_lock.readLock().unlock();
        }
        last_event_id += 1;
        return last_event_id;
    }

    public static int getNewLabelID() {
        labels_lock.readLock().lock();
        if (last_label_id == -1) {
            for (Label label : labels) {
                if (label.getId() > last_label_id) {
                    last_label_id = label.getId();
                }
            }
        }
        last_label_id += 1;
        labels_lock.readLock().unlock();
        return last_label_id;
    }

    public static int getNewDaySettingObjectID() {
        day_settings_lock.readLock().lock();
        if (last_day_setting_id == -1) {
            for (DaySettingObject dso : day_settings) {
                if (dso.getId() > last_day_setting_id) {
                    last_day_setting_id = dso.getId();
                }
            }
        }
        last_day_setting_id += 1;
        day_settings_lock.readLock().unlock();
        return last_day_setting_id;
    }



    public static void addTaskToList(Task new_task) {
        if (new_task.isInactive()) {
            inactive_tasks_lock.writeLock().lock();
            if (!inactive_tasks.contains(new_task)) {
                inactive_tasks.add(new_task);
            }
            inactive_tasks_lock.writeLock().unlock();
        } else {
            tasks_lock.writeLock().lock();
            if (!tasks.contains(new_task)) {
                tasks.add(new_task);
            }
            tasks_lock.writeLock().unlock();
        }
    }

    public static void deleteTaskFromList(Task task) {
        tasks_lock.writeLock().lock();
        if (tasks.contains(task)) {
            tasks.remove(task);
        }
        tasks_lock.writeLock().unlock();

        inactive_tasks_lock.writeLock().lock();
        if (inactive_tasks.contains(task)) {
            inactive_tasks.remove(task);
        }
        inactive_tasks_lock.writeLock().unlock();
    }


    public static void addEventToList(MyEvent new_event) {
        if (new_event.isInactive()) {
            inactive_events_lock.writeLock().lock();
            if (!inactive_events.contains(new_event)) {
                inactive_events.add(new_event);
            }
            inactive_events_lock.writeLock().unlock();
        } else {
            events_lock.writeLock().lock();
            if (!events.contains(new_event)) {
                events.add(new_event);
            }
            events_lock.writeLock().unlock();
        }
    }
    public static void deleteEventFromList(MyEvent event) {
        events_lock.writeLock().lock();
        if (events.contains(event)) {
            events.remove(event);
        }
        events_lock.writeLock().unlock();
        inactive_events_lock.writeLock().lock();
        if (inactive_events.contains(event)) {
            inactive_events.remove(event);
        }
        inactive_events_lock.writeLock().unlock();
    }

    public static void addDayToList(Day new_day) {
        if (!days.contains(new_day)) {
            days.add(new_day);
        }
    }
    public static void deleteLabelFromList(Label label) {
        labels_lock.writeLock().lock();
        if (labels.contains(label)) {
            labels.remove(label);
        }
        labels_lock.writeLock().unlock();
    }

    public static void addLabelToList(Label new_label) {
        labels_lock.writeLock().lock();
        Log.d(tag, "in lock");
        if (!labels.contains(new_label)) {
            labels.add(new_label);
        }
        labels_lock.writeLock().unlock();
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
        events_lock.readLock().lock();
        for (MyEvent e : events) {
            if (e.getId() == id) {
                events_lock.readLock().unlock();
                return e;
            }
        }
        events_lock.readLock().unlock();
        return null;
    }

    public static Task getTask(int id) {
        tasks_lock.readLock().lock();
        for (Task t : tasks) {
            if (t.getId() == id) {
                tasks_lock.readLock().unlock();
                return t;
            }
        }
        tasks_lock.readLock().unlock();
        return null;
    }

    public static Label getLabel(int label_id) {
        labels_lock.readLock().lock();
        for (Label label : labels) {
            if (label.getId() == label_id) {
                labels_lock.readLock().unlock();
                return label;
            }
        }
        labels_lock.readLock().unlock();
        return null;
    }

    public static ArrayList<Label> getLabelSequence(Label me) {
        labels_lock.writeLock().lock();
            Collections.sort(labels);
        labels_lock.writeLock().unlock();

        ArrayList<Label> ret = new ArrayList<Label>();

        labels_lock.readLock().lock();
        for (Label l : labels) {
            if (l.getParent() == null && l != me) { //only top level labels
                ret.addAll(getRecursiveLabelStructure(l, me));
            }
        }
        labels_lock.readLock().unlock();
        return ret;
    }

    private static ArrayList<Label> getRecursiveLabelStructure(Label label, Label me) {
        ArrayList<Label> ret = new ArrayList<>();
        ArrayList<Label> childs = label.getChildLabels();
        if (label == me) {
            return ret;
        }
        ret.add(label);
        for (Label l : childs) {
            ret.addAll(getRecursiveLabelStructure(l, me));
        }
        return ret;
    }


    public static ArrayList<Task> getTasks(int label_id) {
        labels_lock.writeLock().lock();
            Collections.sort(labels);
        labels_lock.writeLock().unlock();
        ArrayList<Task> tasks_with_that_label = new ArrayList<>();
        tasks_lock.readLock().lock();
        for (Task task : tasks) {
            if (task.hasLabel(label_id)) {
                tasks_with_that_label.add(task);
            } else if (label_id == -1 && task.getLabelIds().size() == 0) { //task has no labels...
                tasks_with_that_label.add(task);
            }
        }
        tasks_lock.readLock().unlock();
        return tasks_with_that_label;
    }

    public static ArrayList<Label> getRootLabels() {
        labels_lock.writeLock().lock();
            Collections.sort(labels);
        labels_lock.writeLock().unlock();

        ArrayList<Label> ret = new ArrayList<Label>();
        labels_lock.readLock().lock();
        for (Label l : labels) {
            if (l.getParent() == null) {
                ret.add(l);
            }
        }
        labels_lock.readLock().unlock();
        return ret;
    }

    public static void addDaySettingObject(DaySettingObject new_day_setting) {
        if (day_settings == null) {
            day_settings = new ArrayList<>();
        }
        day_settings_lock.writeLock().lock();
        if (!day_settings.contains(new_day_setting)) {
            day_settings.add(new_day_setting);
        }
        day_settings_lock.writeLock().unlock();
    }
    //TODO: returns the correct daysettingobjet for that specific date
    public static DaySettingObject getDaySettingObject(GregorianCalendar date) {
        DaySettingObject most_relevant = null;
        int max_relevant = -2;
        int relevant = -2;
        day_settings_lock.readLock().lock();
        for (DaySettingObject ds : day_settings) {
            relevant = ds.howRelevant(date);
            if (relevant > max_relevant) {
                max_relevant = relevant;
                most_relevant = ds;
            }
        }
        day_settings_lock.readLock().unlock();
        if (most_relevant != null) {
            return most_relevant;
        } else {
            return new DaySettingObject();
        }
    }

    public static Day createDay(Activity activity, GregorianCalendar date) {
        Day new_day = new Day(date);

        ArrayList events_from_other_calendars = MyCalendarProvider.getEvents(activity, new_day.getStart().getTimeInMillis() + 1000, new_day.getEnd().getTimeInMillis() - 1000);
        events_lock.writeLock().lock();
        events.addAll(events_from_other_calendars);
        events_lock.writeLock().unlock();

        events_lock.readLock().lock();
        for (MyEvent e : events) {
            if (e.isAll_day() && Util.isSameDate(e.getEnd(), new_day.getStart())) {
                continue;
            }
            if (e.isRelevantFotThatDay(date)) {
                new_day.addEvent(e);
            }
        }
        events_lock.readLock().unlock();

        days_lock.writeLock().lock();
        days.add(new_day);
        days_lock.writeLock().unlock();

        ArrayList<Task> repeating_tasks = TaskBroContainer.getRepeatingTasks();
        repeating_tasks_lock.readLock().lock();
        for (Task t : repeating_tasks) {
            if (t.isRelevantRepeatForThatDay(date)) {
                int already_worked_on_that_task = new_day.calculateWorkedTimeOfTask(t);

                int not_distributed_minutes = new_day.addRepeatingTask(t, t.getRemaining_duration() - already_worked_on_that_task);//TODO!!!
                GregorianCalendar date_before = (GregorianCalendar)date.clone();
                while (not_distributed_minutes > 0) {
                    date_before.add(GregorianCalendar.DAY_OF_YEAR, -1);
                    Day day_before = TaskBroContainer.getDay(date_before);
                    if (day_before == null) {
                        day_before = TaskBroContainer.createDay(activity, date_before);
                    }
                    not_distributed_minutes = day_before.addRepeatingTask(t, not_distributed_minutes);
                }
            }
        }
        repeating_tasks_lock.readLock().unlock();

        return new_day;
    }

    public static ArrayList<TaskBlock> getTaskBlocks() {
        return task_blocks;
    }

    public static void saveScaleFactor() {
        SharedPreferences settings =  context.getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("ScaleFactor", scale_factor);
        editor.commit();
    }
    public static void restoreScaleFactor() {
        SharedPreferences settings = context.getSharedPreferences("Settings", 0);
        scale_factor = settings.getFloat("ScaleFactor", 1);
    }

    public static int getPotential() {
        task_blocks_lock.readLock().lock();
        if (task_blocks.size() > 0) {
            TaskBlock tb = task_blocks.get(task_blocks.size() - 1);
            if (tb != null) {
                task_blocks_lock.readLock().unlock();
                return tb.getPotential();
            }
        }
        task_blocks_lock.readLock().unlock();
        return 0;
    }

    public static void resetForCalculation() {
        ArrayList<Task> tasks_to_delete = new ArrayList<>();
        tasks_lock.readLock().lock();
            for (Task t : tasks) {
                t.resetJustForCalculations();
                if (t.isNotCreatedByUser()) {
                    tasks_to_delete.add(t);
                }
            }
        tasks_lock.readLock().unlock();

        tasks_lock.writeLock().lock();
            tasks.removeAll(tasks_to_delete);
        tasks_lock.writeLock().unlock();

        ArrayList<MyEvent> events_to_delete = new ArrayList<>();
        events_lock.readLock().lock();
            for (MyEvent e : events) {
                if (e.isNot_created_by_user()) {
                    events_to_delete.add(e);
                }
            }
        events_lock.readLock().unlock();

        events_lock.writeLock().lock();
            events.removeAll(events_to_delete);
        events_lock.writeLock().unlock();

        repeating_tasks_lock.writeLock().lock();
            repeating_tasks.clear();
        repeating_tasks_lock.writeLock().unlock();

        days_lock.writeLock().lock();
            days.clear();
        days_lock.writeLock().unlock();


    }

    public static void calculateBlocks(Activity activity) {
        if (tasks == null || tasks.size() <= 0) {
            return;
        }
        tasks_lock.writeLock().lock();
            Collections.sort(tasks, Collections.reverseOrder());
        tasks_lock.writeLock().unlock();
        ArrayList<Task> not_done_tasks = getTasksNotDone(true);
        if (not_done_tasks.size() <= 0) {
            return;
        }

        //first block
        task_blocks_lock.writeLock().lock();
            task_blocks.clear();
            TaskBlock block = new TaskBlock();
            task_blocks.add(block);
        task_blocks_lock.writeLock().unlock();

        tasks_lock.readLock().lock();
        if (not_done_tasks.get(0).getDeadline() != null) {
            block.setEnd((GregorianCalendar) not_done_tasks.get(0).getDeadline().clone());
        }
        tasks_lock.readLock().unlock();

        boolean last_task = false;
        GregorianCalendar earlier_date;
        int possible_work_time_for_current_task;
        int overlapping_time;

        for (int i = 0; i < not_done_tasks.size(); i++) {
            Task current_task = not_done_tasks.get(i);
            if (current_task.isDone() || current_task.getRepeat() != 0) {
                continue;
            }
            //first block with tasks without deadlines:
            if (current_task.getDeadline() == null) {
                block.addTask(current_task);
                if ((i+1) < not_done_tasks.size() &&
                        not_done_tasks.get(i+1).getDeadline() != null) { //last task without deadline...
                    block = new TaskBlock();
                    task_blocks_lock.writeLock().lock();
                        task_blocks.add(block);
                    task_blocks_lock.writeLock().unlock();
                    block.setEnd((GregorianCalendar) not_done_tasks.get(i+1).getDeadline().clone());
                }
                continue;
            }
            current_task.setOverlapping_minutes(block.getOverlapping_time());
            block.addTask(current_task);

            if ((i + 1) >= not_done_tasks.size()) {
                last_task = true;
            }

            if (Util.earlierDate(current_task.getDeadline(), new GregorianCalendar())) {
                //DL in der Vergangenheit
                earlier_date = (GregorianCalendar)current_task.getDeadline().clone();
            } else if (last_task) {
                //kein vergangener Task:
                earlier_date = new GregorianCalendar();
            } else {
                earlier_date = (GregorianCalendar)not_done_tasks.get(i + 1).getDeadline().clone();
                if (Util.earlierDate(earlier_date, new GregorianCalendar())){
                    earlier_date = new GregorianCalendar();
                }
            }

            possible_work_time_for_current_task = TaskBroContainer.getPossibleWorkTime(activity, current_task.getDeadline(), earlier_date, false);
            overlapping_time = current_task.getRemaining_duration() + current_task.getOverlapping_minutes() - possible_work_time_for_current_task;

            block.setOverlapping_time(overlapping_time);
            block.setStart(earlier_date);               //every time a new task added?! hmm...

            if (overlapping_time < 0 && !last_task) { //if everything is possible in that block...
                block = new TaskBlock();
                task_blocks_lock.writeLock().lock();
                    task_blocks.add(block);
                task_blocks_lock.writeLock().unlock();
                block.setEnd((GregorianCalendar) earlier_date.clone());
            } else if (overlapping_time > 0 && last_task) {
                current_task.setOverlapping_minutes(0);//TODO otherwise the fillingfactor is wrong...
            }
        }
    }

    public static void calculatePotentialOfBlocks(Activity activity) {
        if (task_blocks == null || days == null || task_blocks.size() <= 0) {
            return;
        }

        task_blocks_lock.readLock().lock();
        for (int i = task_blocks.size() - 1; i >= 0; i--) { //start with the newest block
            TaskBlock tb = task_blocks.get(i);
            if (tb.getStart() == null && tb.getEnd() == null) { //tasks without Deadlines
                tb.setPotential(0);
            } else {
                int potential = TaskBroContainer.getPossibleWorkTime(activity, tb.getStart(), tb.getEnd(), true);
                potential -= tb.getRemainingDurationOfTasks();
                tb.setPotential(potential);
            }
        }
        task_blocks_lock.readLock().unlock();
        for (Day day : days) {
            day.setDistributedMinutes(0);
        }

        task_blocks_lock.readLock().lock();
        for (int i = 0; i < task_blocks.size() - 1; i++) { //start with oldest block EXCEPT newest block!!!
            TaskBlock tb = task_blocks.get(i);
            int potential = tb.getPotential();
            if (potential < 0) { //cannot be newest block!!
                task_blocks.get(i+1).setPotential(task_blocks.get(i + 1).getPotential() + potential);
                tb.setPotential(0);
            }
        }
        task_blocks_lock.readLock().unlock();
    }

    public static void calculateDays(Activity activity) {
        if (tasks.size() <= 0) { return; }

        resetForCalculation();
        calculateBlocks(activity);
        calculatePotentialOfBlocks(activity);

        task_blocks_lock.writeLock().lock();
            for (TaskBlock tb : task_blocks) {
                tb.calculateTaskFillingFactors();
                Collections.sort(tb.getTasks());
            }
        task_blocks_lock.writeLock().unlock();

        //create all Days till last day saved, if not already created
        GregorianCalendar latest_date = new GregorianCalendar();
        for (Task t : tasks) {
            if (Util.earlierDate(latest_date, t.getDeadline())) {
                latest_date = (GregorianCalendar)t.getDeadline().clone();
            }
        }
        latest_date.add(GregorianCalendar.DAY_OF_YEAR, Settings.getDaysToLookForward());
        distributeTasksFromTaskBlocksTillDate(activity, latest_date);
    }

    //DEBUGGING
    private static void printAllTasks() {
        Log.d(tag, "Going to print all " + TaskBroContainer.getTasks().size() + " saved tasks...");
        tasks_lock.readLock().lock();
        for (Task task : tasks) {
            Log.d(tag, "task: " + task.description());
        }
        tasks_lock.readLock().unlock();
    }

    public static ArrayList<Task> getRepeatingTasks() {
        if (repeating_tasks.size() > 0) {
            return repeating_tasks;
        }

        repeating_tasks = new ArrayList<>();

        //TODO: LOCKING PROBLEM?!
        tasks_lock.readLock().lock();
        for (Task t : tasks) {
            if (t.getRepeat() != 0) {
                repeating_tasks_lock.writeLock().lock();
                    repeating_tasks.add(t);
                repeating_tasks_lock.writeLock().unlock();
            }
        }
        tasks_lock.readLock().unlock();

        return repeating_tasks;
    }



    public static void distributeTasksFromTaskBlocksTillDate(Activity activity, GregorianCalendar latest_date) {
        GregorianCalendar date_in_future = (GregorianCalendar)latest_date.clone();
        date_in_future.add(GregorianCalendar.DAY_OF_YEAR, Settings.getDaysToLookForward());

        for (GregorianCalendar date : Util.getListOfDates(new GregorianCalendar(), date_in_future)) {
            if (TaskBroContainer.getDay(date) == null) {
                TaskBroContainer.createDay(activity, date);
            }
        }

        days_lock.writeLock().lock();
            Collections.sort(days);
            for (int i = 0; i < (days.size() - Settings.getDaysToLookForward()); i++) {
                Day day = days.get(i);
                if (!day.alreadyDistributed()) {
                    day.distributeTaskBlockTasks();
                    day.createTaskEventsBecauseOfTaskDurations();
                    day.alreadyDistributed(true);
                }
            }
        days_lock.writeLock().unlock();
    }

    public static Day getLatestDay() {
        Day latest_day = days.get(days.size() - 1);
        for (Day d : days) {
            if (Util.earlierDate(latest_day.getStart(), d.getStart())) {
                latest_day = d;
            }
        }
        return latest_day;
    }

    public static Duration calculateWorkedTimeOfTask(Task task) {
        int worked_minutes = 0;
        events_lock.readLock().lock();
            for (MyEvent e : events) {
                if (task == e.getTask() && !e.isNot_created_by_user()) {
                    worked_minutes += e.getDurationInMinutes();
                }
            }
        events_lock.readLock().unlock();
        return new Duration(worked_minutes);
    }

    public static boolean fillDayWithTasks(Day day_to_fill) {
        boolean all_distributed = true;
        task_blocks_lock.readLock().lock();
        for (int i = task_blocks.size() - 1; i >= 0; i--) { //start with the newest block
            TaskBlock tb = task_blocks.get(i);
            if (!tb.alreadyDistributed()) {
                all_distributed = false;
                tb.sortTasks();
                for (Task t : tb.getTasks()) {
                    day_to_fill.addTask(t);
                }
            }
        }
        task_blocks_lock.readLock().unlock();
        return all_distributed;
    }

    public static boolean checkLabelName(Label current_label, String label_name_to_check) {
        String check_text = label_name_to_check.trim();
        labels_lock.readLock().lock();
        for (Label l : labels) {
            if (l.getName().equals(check_text) && l != current_label) {
                labels_lock.readLock().unlock();
                return false;
            }
        }
        labels_lock.readLock().unlock();
        return true;
    }

    public static ArrayList<Label> getChildLabelsOf(Label parent_label) {
        ArrayList<Label> ret = new ArrayList<>();
        labels_lock.readLock().lock();
        for (Label l : labels) {
            if (parent_label.equals(l.getParent())) {
                ret.add(l);
            }
        }
        labels_lock.readLock().unlock();

        return ret;
    }

}
