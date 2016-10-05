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
    private static boolean all_task_blocks_distributed = false;
    private static boolean started;

    public static boolean isStarted() {
        return started;
    }

    public static void setStarted(boolean started) {
        TaskBroContainer.started = started;
    }

    public boolean isAllTaskBlocksDistributed() {
        return all_task_blocks_distributed;
    }

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
        Log.d(tag, "startApplication");
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


    private static int getPossibleWorkTime(Activity activity, GregorianCalendar start_date, GregorianCalendar end_date, boolean set_worked, boolean get_theoretical_work_time) {
        int sum_work_time = 0;

        if (!(Util.isSameDate(start_date, end_date) && Util.isSameTime(start_date, end_date))) {//if there is really a slot! ;)
            for (GregorianCalendar current_date : Util.getListOfDates(start_date, end_date)) {
                Day day = TaskBroContainer.getDay(current_date);
                if (day == null) {
                    day = TaskBroContainer.createDay(activity, current_date);
                }

                int possible_work_time_in_that_day = 0;
                if (get_theoretical_work_time) {
                    possible_work_time_in_that_day = day.getTheoreticalWorkTime(start_date, end_date);
                } else {
                    possible_work_time_in_that_day = day.getPossibleWorkTime(start_date, end_date, set_worked);
                }
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

        addRepeatingTaskIfNecessary(activity, new_day);

        return new_day;
    }

    public static void addRepeatingTaskIfNecessary(Activity activity, Day day) {
        Log.d(tag, "addRepeatingTaskIfNecessary for day " + day.description());
        ArrayList<Task> repeating_tasks = TaskBroContainer.getRepeatingTasks();
        repeating_tasks_lock.readLock().lock();
        for (Task t : repeating_tasks) {
            if (t.isRelevantRepeatForThatDay(day.getStart())) {
                int already_worked_on_that_task = day.calculateWorkedTimeOfTask(t);

                int not_distributed_minutes = day.addRepeatingTask(t, t.getRemaining_duration() - already_worked_on_that_task);//TODO!!!
                GregorianCalendar date_before = (GregorianCalendar)day.getStart().clone();
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
            TaskBlock tb = task_blocks.get(0);
            if (tb != null) {
                task_blocks_lock.readLock().unlock();
                return tb.getPotential();
            }
        }
        task_blocks_lock.readLock().unlock();
        return 0;
    }

    public static void resetForCalculation(Activity activity) {
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
                if (e.isNot_created_by_user() && !e.isRepeatingTaskEvent()) {
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
            for (Day day : days) {
                day.resetForCalculation(activity);
            }
            //days.clear();
        days_lock.writeLock().unlock();

    }

    public static ArrayList<TaskBlock> calculateBlocks(Activity activity) {
        tasks_lock.writeLock().lock();
            Collections.sort(tasks, Collections.reverseOrder());
        tasks_lock.writeLock().unlock();
        ArrayList<Task> not_done_tasks = getTasksNotDone(true);

        if (tasks == null || tasks.size() <= 0 || not_done_tasks.size() <= 0) {
            return null;
        }

        //first block
        ArrayList<TaskBlock> task_blocks_new = new ArrayList<>();

        TaskBlock block = new TaskBlock();
        task_blocks_new.add(block);
        if (not_done_tasks.get(0).getDeadline() != null) {
            block.setEnd((GregorianCalendar) not_done_tasks.get(0).getDeadline().clone());
        }

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
                    task_blocks_new.add(block);
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

            possible_work_time_for_current_task = TaskBroContainer.getPossibleWorkTime(activity, current_task.getDeadline(), earlier_date, false, false);
            overlapping_time = current_task.getRemaining_duration() + current_task.getOverlapping_minutes() - possible_work_time_for_current_task;

            block.setOverlapping_time(overlapping_time);
            block.setStart(earlier_date);               //every time a new task added?! hmm...

            if (overlapping_time < 0 && !last_task) { //if everything is possible in that block...
                block = new TaskBlock();
                task_blocks_new.add(block);
                block.setEnd((GregorianCalendar) earlier_date.clone());
            } else if (overlapping_time > 0 && last_task) {
                current_task.setOverlapping_minutes(0);//TODO otherwise the fillingfactor is wrong...
            }
        }
        return task_blocks_new;
    }


    public static void calculatePotentialOfBlocks(Activity activity, ArrayList<TaskBlock> task_blocks_new) {
        if (task_blocks_new == null || days == null || task_blocks_new.size() <= 0) {
            return;
        }

        Collections.sort(task_blocks_new);
        for (TaskBlock tb : task_blocks_new) { //start with the newest block
            if (tb.getStart() == null && tb.getEnd() == null) { //tasks without Deadlines
                tb.setPotential(0);
            } else {
                int potential = TaskBroContainer.getPossibleWorkTime(activity, tb.getStart(), tb.getEnd(), true, false);
                potential -= tb.getRemainingDurationOfTasks();
                tb.setPotential(potential);
            }
        }

        for (int i = task_blocks_new.size() - 1; i > 0; i--) { //start with oldest block EXCEPT newest block!!!
            TaskBlock tb = task_blocks_new.get(i);
            int potential = tb.getPotential();
            if (potential < 0) { //cannot be newest block!!
                task_blocks_new.get(i - 1).setPotential(task_blocks_new.get(i - 1).getPotential() + potential);
                tb.setPotential(0);
            }
        }

    }

    public static void calculateDays(Activity activity) {
        if (tasks.size() <= 0) { return; }

        resetForCalculation(activity);
        days_lock.readLock().lock();
        for (Day day : days) {
            day.deleteRepeatingTaskEvents();
            day.deleteTaskEvents();
            day.resetForCalculation(activity);
        }
        for (Day day : days) {
            TaskBroContainer.addRepeatingTaskIfNecessary(activity, day);
        }
        days_lock.readLock().unlock();

        ArrayList<TaskBlock> task_blocks_new = calculateBlocks(activity);
        resetForCalculation(activity);
        calculatePotentialOfBlocks(activity, task_blocks_new);

        for (TaskBlock tb : task_blocks_new) {
            tb.calculateTaskFillingFactors();
            Collections.sort(tb.getTasks());
        }

        task_blocks_lock.writeLock().lock();
            task_blocks = task_blocks_new;
        task_blocks_lock.writeLock().unlock();

        resetForCalculation(activity);

        GregorianCalendar latest_date = new GregorianCalendar();
        for (TaskBlock tb : task_blocks_new) {
            if (tb.getEnd() != null && Util.earlierDate(latest_date, tb.getEnd())) {
                latest_date = (GregorianCalendar)tb.getEnd().clone();
            }
        }

        //ensure to have also repeating tasks berücksichtigt...
        /*GregorianCalendar date_in_future = (GregorianCalendar)latest_date.clone();
        date_in_future.add(GregorianCalendar.DAY_OF_YEAR, Settings.getDaysToLookForward());
        for (GregorianCalendar date : Util.getListOfDates(new GregorianCalendar(), date_in_future)) {
            if (TaskBroContainer.getDay(date) == null) {
                TaskBroContainer.createDay(activity, date);
            }
        }*/
//        Day latest_day = TaskBroContainer.getDay(latest_date);
        //latest_day.distributeTaskBlocks(activity);

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

/*
    private static void distributeTaskBlocks(Activity activity) {
        boolean all_distributed = false;
        Day day = TaskBroContainer.getDay(new GregorianCalendar());
        if (day == null) {
            day = TaskBroContainer.createDay(activity, new GregorianCalendar());
        }
        while (!all_distributed) {
            all_distributed = true;
            while (day.getPossibleWorkTime(day.getStart(), day.getEnd(), false) > 0) {
                TaskBroContainer.getTaskDurationsBecauseOfTaskBlocks()

            }
        }
    }
*/

    private static void distributeTaskBlocks(Activity activity, GregorianCalendar latest_date) {


        //
/*
        boolean all_distributed = false;
        Day day = TaskBroContainer.getDay(new GregorianCalendar());
        task_blocks_lock.readLock().lock();
        for (int i = task_blocks.size() - 1; i >= 0; i--) { //start with the newest block
            TaskBlock tb = task_blocks.get(i);
            while (!tb.alreadyDistributed(null)) {

            }
        }
        task_blocks_lock.readLock().unlock();
        */
    }
/*
    public static void distributeTasksFromTaskBlocksTillDate(Activity activity, GregorianCalendar latest_date) {


        days_lock.writeLock().lock();
            Collections.sort(days);
        days_lock.writeLock().unlock();

        days_lock.readLock().lock();
            for (int i = 0; i < (days.size() - Settings.getDaysToLookForward()); i++) {
                if (all_task_blocks_distributed) {
                    break;
                }
                Day day = days.get(i);
                Log.d(tag, "START distributing for day " + day.description());
                day.resetForCalculation();

//                int theoretical_work_time = day.getTheoreticalWorkTime(day.getStart(), day.getEnd());
//                Log.d(tag, "theoretical work time = " + theoretical_work_time);
                ArrayList<MyEvent> worked_task_events = day.getListWorkedTaskEvents();
                for (MyEvent wte : worked_task_events) {
                    Log.d(tag, "-wte " + wte.description());
                }
                ArrayList<Task> tasks_to_ignore = new ArrayList<>();
                ArrayList<TaskWithDuration> task_durations = TaskBroContainer.getTaskDurationsBecauseOfTaskBlocks(theoretical_work_time, worked_task_events, null);//TODO

                ArrayList<TaskWithDuration> correct_task_duration = new ArrayList<>();

                for (MyEvent wte : worked_task_events) {
                    for (TaskWithDuration twd : task_durations) {
                        if (twd.getTask() == wte.getTask()) {
                            int diff = twd.getDurationInMinutes() - wte.getDurationInMinutes();
                            if (diff > 0) {
                                twd.addDuration(-wte.getDurationInMinutes());
                                twd.getTask().already_distributed_duration -= wte.getDurationInMinutes();
                            } else { //more worked than necessary! or exactly equal work time!
                                twd.setDuration(0);
                            }
                            correct_task_duration.add(twd);
                            tasks_to_ignore.add(twd.getTask());
                        }
                    }
                }

                for (TaskWithDuration ctwd : correct_task_duration) {
                    Log.d(tag, "--ctwd " + ctwd.getDurationInMinutes() + " task " + ctwd.getTask().description());
                }

                day.resetForCalculation();
                day.createTaskEvents(correct_task_duration);
                for (TaskWithDuration twd : correct_task_duration) {
                    twd.getTask().already_distributed_duration += twd.getDurationInMinutes();
                }

                int possible_work_time = day.getPossibleWorkTime(day.getStart(), day.getEnd(), false);
                Log.d(tag, "possible worktime = " + possible_work_time + " " + day.description());
                ArrayList<TaskWithDuration> tmp = TaskBroContainer.getTaskDurationsBecauseOfTaskBlocks(possible_work_time, worked_task_events, tasks_to_ignore);//TODO
                for (TaskWithDuration twd : tmp) {
                    Log.d(tag, "twd:" + twd.getTask().description() + " duration = " + twd.getDurationInMinutes());
                }
                day.createTaskEvents(tmp);

                day.calculateBlocksAndColoumns();
            }
        days_lock.readLock().unlock();
    }
    */

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



    public static ArrayList<TaskWithDuration> getTaskDurationsBecauseOfTaskBlocks(int time_to_distribute, ArrayList<MyEvent> worked_task_events, ArrayList<Task> tasks_to_ignore) {
        ArrayList<TaskWithDuration> ret = new ArrayList<>();
        if (time_to_distribute <= 0) {
            return ret;
        }

            task_blocks_lock.readLock().lock();
            for (TaskBlock tb : task_blocks) { //start with the newest block
                boolean task_found = true;
                while (task_found && time_to_distribute > 0) {
                    task_found = false;
                    Log.d(tag, " - task_found && time_to_distribute > 0 => " + time_to_distribute);
                    tb.sortTasks();
                    for (Task t : tb.getTasks()) {
                        int remaining_duration_to_add = 0;
                        for (MyEvent e : worked_task_events) {
                            if (e.getTask() == t) {
                                remaining_duration_to_add += e.getDurationInMinutes();
                            }
                        }

                        if (t.already_distributed_duration == (t.getRemaining_duration() + remaining_duration_to_add) ||
                                (tasks_to_ignore != null && tasks_to_ignore.contains(t))) {
                            continue;
                        } else {
                            task_found = true;
                            Log.d(tag, " - task found: " + t.description());
                        }

                        //calculate work time for that task
                        int work_time_for_that_task = (int)(time_to_distribute * t.getFilling_factor());
                        if (time_to_distribute > 0 && work_time_for_that_task == 0) {
                            work_time_for_that_task = 1;
                        }
                        if (work_time_for_that_task > (t.getRemaining_duration() + remaining_duration_to_add - t.already_distributed_duration)) {
                            work_time_for_that_task = t.getRemaining_duration() + remaining_duration_to_add - t.already_distributed_duration;
                        }

                        //setting some things
                        t.already_distributed_duration += work_time_for_that_task;

                        time_to_distribute -= work_time_for_that_task;

                        //adding to list without dublicates
                        boolean found = false;
                        for (TaskWithDuration twd : ret) {
                            if (twd.getTask() == t) {
                                found = true;
                                twd.addDuration(work_time_for_that_task);
                                Log.d(tag, "work time is now = " + twd.getDurationInMinutes() + " for " + twd.getTask().description());
                            }
                        }
                        if (!found) {
                            TaskWithDuration td = new TaskWithDuration(t, work_time_for_that_task);
                            ret.add(td);
                            Log.d(tag, "work time is = " + td.getDurationInMinutes() + " for new " + td.getTask().description());
                        }
                    }
                }
            }
            task_blocks_lock.readLock().unlock();

        return ret;
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

    public static void sortLists() {
        days_lock.writeLock().lock();
            Collections.sort(days);
        days_lock.writeLock().unlock();

        tasks_lock.writeLock().lock();
            Collections.sort(task_blocks, Collections.reverseOrder());
        tasks_lock.writeLock().unlock();

        events_lock.writeLock().lock();
            Collections.sort(events);
        events_lock.writeLock().unlock();
    }

    public static Day getDayBefore(Activity activity, GregorianCalendar date) {
        GregorianCalendar date_before = (GregorianCalendar) date.clone();
        date_before.add(GregorianCalendar.DAY_OF_YEAR, -1);
        Day day_before = TaskBroContainer.getDay(date_before);
        if (day_before == null) {
            day_before = TaskBroContainer.createDay(activity, date_before);
        }
        return day_before;
    }

    public static void addEventToRelevantDays(MyEvent event) {
        for (GregorianCalendar date : Util.getListOfDates(event.getStart(), event.getEnd())) {
            Day day_where_event_to_add = getDay(date);
            if (day_where_event_to_add != null) {
                day_where_event_to_add.addEvent(event);
            }
        }

    }

    public static void lookInTheFuture(Activity activity, GregorianCalendar date) {
        GregorianCalendar date_in_future = (GregorianCalendar)date.clone();
        date_in_future.add(GregorianCalendar.DAY_OF_YEAR, Settings.getDaysToLookForward());
        for (GregorianCalendar d : Util.getListOfDates(new GregorianCalendar(), date_in_future)) {
            if (TaskBroContainer.getDay(d) == null) {
                TaskBroContainer.createDay(activity, d);
            }
            Log.d(tag, "future : " + Util.getFormattedDate(d));
        }
    }
}
