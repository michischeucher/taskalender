package brothers.scheucher.taskbro;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import static java.util.Collections.sort;

public class Day implements Comparable {
    private static final String tag = "Day";
    private DaySettingObject day_settings;

    private GregorianCalendar start;
    private GregorianCalendar end;
    private ArrayList<MyEvent> events;
    private DayScheduler scheduler;
    private ArrayList<Block> event_blocks;
    private ArrayList<MyEvent> events_whole_day;
    private static final int MIN_DISPLAY_DURATION_FOR_ONE_EVENT = 30;

    private int already_distributed; //just for calculation
    private boolean already_distributed_boolean;

    public void resetForCalculation(Activity activity) {
        this.already_distributed = 0;
        this.already_distributed_boolean = false;

        initScheduler();
    }

    private void initScheduler() {
        this.scheduler = new DayScheduler(this.start);
        GregorianCalendar now = new GregorianCalendar();
        if (Util.isSameDate(this.start, now)) {
            //if today: past is not possible...!
            GregorianCalendar today_start = new GregorianCalendar();
            Util.setTime(today_start, 0, 0);
            scheduler.addBlockingTime(today_start, now);
        } else if (Util.earlierDate(this.start, now)) { // if past, nothing possible!
            scheduler.addBlockingTime(this.start, 0, 60*24);
        }

        GregorianCalendar earliest_start_date = (GregorianCalendar)start.clone();
        earliest_start_date.add(GregorianCalendar.MINUTE, day_settings.getEarliest_minute());
        scheduler.addBlockingTime(start, earliest_start_date);

        GregorianCalendar latest_minute_date = (GregorianCalendar)start.clone();
        latest_minute_date.add(GregorianCalendar.MINUTE, day_settings.getLatest_minute());
        scheduler.addBlockingTime(latest_minute_date, end);

        if (!day_settings.isWorkingDay(Util.calculateWorkingDay(this.start))) {
            Log.d(tag, description() + "is not working day!!!");
            scheduler.addBlockingTime(start, end);
        }

        //every normal event is added... no extra task event, but worked time also (created by user) and also repeating task events
        ArrayList<MyEvent> events_to_delete = new ArrayList<>();
        for (MyEvent e : events) {
            if (e.getTask() == null && !e.isAll_day() || !e.isAll_day() && !e.isNot_created_by_user() || e.isRepeatingTaskEvent()) {
                scheduler.addBlockingTime(e.getStart(), e.getEnd());
            } else if (e.isNot_created_by_user()){
                events_to_delete.add(e);
            }
        }
        events.removeAll(events_to_delete);
    }


    public Day(GregorianCalendar date) {
        this.start = (GregorianCalendar)date.clone();
        Util.setTime(this.start, 0, 0);
        this.end = (GregorianCalendar)start.clone();
        this.end.add(GregorianCalendar.DAY_OF_YEAR, 1);
        this.events = new ArrayList<>();

        this.event_blocks = new ArrayList<>();
        this.events_whole_day = new ArrayList<>();
        this.day_settings = TaskBroContainer.getDaySettingObject(this.start);

        this.already_distributed = 0;
        this.already_distributed_boolean = false;

        initScheduler();
    }

    public GregorianCalendar getStart() {
        return start;
    }

    public GregorianCalendar getEnd() {
        return end;
    }

    public ArrayList<MyEvent> getEvents() {
        return events;
    }

    public void addEvent(MyEvent event_to_add) {
        if (!this.events.contains(event_to_add)) {
            for (MyEvent e : this.events) {
                if (e.getExternID() != -1 && e.getExternID() == event_to_add.getExternID()) {
                    return;
                }
            }
            this.events.add(event_to_add);

            if (event_to_add.isBlocking()) {
                this.scheduler.addBlockingTime(event_to_add.getStart(), event_to_add.getEnd());
            }
        }
    }

    private void addTaskEventWithoutChecking(MyEvent new_event) {
        events.add(new_event);
        if (new_event.isBlocking()) {
            scheduler.addBlockingTime(new_event.getStart(), new_event.getEnd());
        }

    }

    public String description() {
        return "Day: " + Util.getFormattedDate(this.start) + " #events = " + this.events.size();
    }

    public void sortEvents() {
        sort(events);
    }

    public int getPossibleWorkTime(GregorianCalendar start_date, GregorianCalendar end_date, boolean set_worked) {
        DayScheduler new_scheduler = new DayScheduler(scheduler);
        for (MyEvent e : events) {
            if (e.getTask() != null) {
                new_scheduler.addBlockingTime(e.getStart(), e.getEnd());
            }
        }

        int sum_work_time = 0;

        TimeObj dates_to_check = new TimeObj(start_date, end_date);

        for (TimeObj to : new_scheduler.getFreeSlots()) {
            TimeObj overlapping = Util.calculateOverlappingTime(to, dates_to_check);
            if (overlapping != null) {
                sum_work_time += (int)Util.getMinutesBetweenDates(overlapping.start, overlapping.end);
            }
        }

        sum_work_time = checkAvailableWorkTime(sum_work_time, set_worked, false);

        return sum_work_time;
    }

    public int getTheoreticalWorkTime(GregorianCalendar start_date, GregorianCalendar end_date) {
        int sum_work_time = 0;

        TimeObj dates_to_check = new TimeObj(start_date, end_date);

        for (TimeObj to : this.scheduler.getFreeSlots()) {
            TimeObj overlapping = Util.calculateOverlappingTime(to, dates_to_check);
            if (overlapping != null) {
                sum_work_time += (int)Util.getMinutesBetweenDates(overlapping.start, overlapping.end);
            }
        }
        for (MyEvent e : events) {
            if (!e.isNot_created_by_user() && e.getTask() != null) {
                //it is worked time!
                sum_work_time += e.getDurationInMinutes();
            }
        }

        sum_work_time = checkAvailableWorkTime(sum_work_time, false, true);

        return sum_work_time;
    }




    private int checkAvailableWorkTime(int available_work_time, boolean set_worked, boolean without_task_events) {
        int already_worked = calculateWorkedTime(without_task_events);

        int difference = day_settings.getTotalDurationInMinutes() - already_worked;
        if (set_worked) {
            difference -= this.already_distributed;
        }
        if (available_work_time > difference) {
            if (difference > 0) {
                if (set_worked) {
                    this.already_distributed += difference;
                }
                return difference;
            } else {
                return 0;
            }
        } else {
            if (set_worked) {
                this.already_distributed += available_work_time;
            }
            return available_work_time;
        }
    }

    private int calculateWorkedTime(boolean without_task_events) {
        DayScheduler scheduler = new DayScheduler(this.start);
        scheduler.addBlockingTime(this.start, 0, day_settings.getEarliest_minute());
        scheduler.addBlockingTime(this.start, day_settings.getLatest_minute(), 24 * 60);
        for (MyEvent e : this.events) {
            if (e.isRepeatingTaskEvent() ||
                    e.isBlocking() && !e.isNot_created_by_user() && (!without_task_events || e.getTask() == null)) {
                scheduler.addBlockingTime(e.getStart(), e.getEnd());
            }
        }
        return 24 * 60 - scheduler.getPossibleWorkTime() - day_settings.getEarliest_minute() - (24 * 60 - day_settings.getLatest_minute());
    }

    public void addTask(Task task) {
        if (task.already_distributed_duration == task.getRemaining_duration()) {
            //dont need more time...
            return;
        }

        int available_work_time = getAvailableWorkTime(false) - this.already_distributed;
        int work_time_for_that_task = (int)(available_work_time * task.getFilling_factor());
        if (available_work_time > 0 && work_time_for_that_task == 0) {
            work_time_for_that_task = 1;
        }

        if (work_time_for_that_task > (task.getRemaining_duration() - task.already_distributed_duration)) { //not necessary to have so much time... ;)
            work_time_for_that_task = task.getRemaining_duration() - task.already_distributed_duration; //rest of duration...
            task.already_distributed_duration = task.getRemaining_duration();
        } else {
            task.already_distributed_duration += work_time_for_that_task;
        }
        this.already_distributed += work_time_for_that_task;

        //addTaskWithDuration(task, work_time_for_that_task);
    }

    public void createTaskEvents(ArrayList<TaskWithDuration> task_durations) {
        Collections.sort(task_durations);
        for (TaskWithDuration twd : task_durations) {
            Task task = twd.getTask();
            int work_time_for_that_task = twd.getDurationInMinutes();

            Log.d(tag, task.description() + " duration = " + twd.getDurationInMinutes());

            while(work_time_for_that_task > 0) {
                Log.d(tag, " work_time = " + work_time_for_that_task);
                TimeObj free_slot = scheduler.getFreeSlotOrBiggest(work_time_for_that_task);

                if (free_slot == null){
                    Log.e(tag, "ERROR: There is a locking problem!");
                    break;
                }
                MyEvent new_event = new MyEvent();
                TaskBroContainer.addEventToList(new_event);
                new_event.setId(TaskBroContainer.getNewEventID());
                new_event.setNot_created_by_user(true);
                new_event.setTask(task);

                new_event.setStart((GregorianCalendar) free_slot.start.clone());
                new_event.setStart(free_slot.start.get(Calendar.HOUR_OF_DAY), free_slot.start.get(Calendar.MINUTE));
                int effective_time = Math.min(free_slot.getDuration(), work_time_for_that_task);
                new_event.setEndWithDuration(effective_time);
                work_time_for_that_task -= effective_time;

                addTaskEventWithoutChecking(new_event);
                Log.d(tag, "event added = " + new_event.description());
            }
        }
        //sortEvents();
    }

    //return minutes which could not be distributed because of too less time!
    //return 0 if all was distributed
    public int addRepeatingTask(Task task, int work_time_for_that_task) {
        if (Util.earlierDate(this.end, new GregorianCalendar())) {
            return 0;
        }

        while(work_time_for_that_task > 0) {
            int available_work_time = checkAvailableWorkTime(work_time_for_that_task, false, false);

            Log.d(tag, "available work time for repeating task = " + available_work_time);
            TimeObj free_slot = scheduler.getFreeSlotOrBiggest(work_time_for_that_task);
            if (free_slot == null || available_work_time == 0) {
                Log.d(tag, "is null");
                return work_time_for_that_task;
            }

            MyEvent new_event = new MyEvent();
            TaskBroContainer.addEventToList(new_event);
            new_event.setId(TaskBroContainer.getNewEventID());
            new_event.setNot_created_by_user(true);
            new_event.setTask(task);
            new_event.setNotice("Repeating task...");

            new_event.setStart((GregorianCalendar) free_slot.start.clone());
            new_event.setStart(free_slot.start.get(Calendar.HOUR_OF_DAY), free_slot.start.get(Calendar.MINUTE));
            int effective_time = Math.min(free_slot.getDuration(), available_work_time);
            new_event.setEndWithDuration(effective_time);
            work_time_for_that_task -= effective_time;

            new_event.setRepeatingTaskEvent(true);
            Log.d(tag, "repeating task adding: " + new_event.description());

            addTaskEventWithoutChecking(new_event);
        }

        return work_time_for_that_task; //must be always 0!! because everything is distributed
    }


    private int getDistributedTaskEventsDurationOfTask(Task task) {
        int duration_of_all_task_events = 0;
        for (MyEvent e : events) {
            if (e.getTask() == task) {
                duration_of_all_task_events += e.getDurationInMinutes();
            }
        }
        return duration_of_all_task_events;
    }

    private int getDistributedTaskEventsDuration() {
        int duration_of_all_task_events = 0;
        for (MyEvent e : events) {
            if (e.getTask() != null) {
                duration_of_all_task_events += e.getDurationInMinutes();
            }
        }
        return duration_of_all_task_events;
    }


    public String getPotential() {
        if (Util.earlierDate(this.end, new GregorianCalendar())) {
            return "Kein Potential, da Vergangenheit";
        }
        TimeObj day_time = new TimeObj(this.start, this.end);
        String ret = "";
        boolean found = false;
        for (TaskBlock tb : TaskBroContainer.getTaskBlocks()) {
            TimeObj overlapping = Util.calculateOverlappingTime(day_time, new TimeObj(tb.getStart(), tb.getEnd()));
            if (overlapping != null) {
                if (found) {
                    ret += System.getProperty("line.separator");
                }
                ret += Util.getFormattedDateTimeToDateTime(tb.getStart(), tb.getEnd()) + ": " + Util.getFormattedPotential(tb.getPotential());
                found = true;
            }
        }
        if (!found) {
            return "Keine Tasks in der Zukunft gefunden!";
        }

        return ret;
    }

    public void calculateBlocksAndColoumns() {
        sortEvents();
        event_blocks.clear();
        events_whole_day.clear();

        Block block = null;

        for (MyEvent e : this.events) {
            if (e.getDurationInMinutes() >= 60 * 24 || e.isAll_day()) {
                events_whole_day.add(e);
                continue;
            }

            //just for displaying in the right form... (mind. 1/2 std!!)
            int duration = e.getDurationInMinutes();
            if (duration < MIN_DISPLAY_DURATION_FOR_ONE_EVENT) {
                e.setEndWithDuration(MIN_DISPLAY_DURATION_FOR_ONE_EVENT);
            }

            if (block == null || Util.calculateOverlappingTime(block.getTimeObj(), e.getTimeObj()) == null) { //no overlapping => new block
                block = new Block(e.getStart(), e.getEnd());
                if (Util.earlierDate(this.end, e.getEnd())) {
                    block.setEnd(this.end);
                }
                if (Util.earlierDate(e.getStart(), this.start)) {
                    block.setStart(this.start);
                }

                event_blocks.add(block);
            }
            block.addEvent(e);
            //resetting the right value afterwards
            e.setEndWithDuration(duration);
        }
    }

    public void drawEvents(LinearLayout calender_day_events_tasks, LayoutInflater inflater) {
        calender_day_events_tasks.removeAllViewsInLayout();

        LinearLayout event_block;
        LinearLayout event_coloumn;
        LinearLayout event;

        GregorianCalendar last_block_end = (GregorianCalendar) this.start.clone();
        //draw events in that day
        for (Block b : this.event_blocks) {
            if (Util.earlierDate(last_block_end, b.getStart())) {
                //create empty block...
                event_block = (LinearLayout) inflater.inflate(R.layout.event_block, calender_day_events_tasks, false);
                Util.setWeight(event_block, (int)Util.getMinutesBetweenDates(last_block_end, b.getStart()));
//                event_block.setBackgroundColor(0xFFFF0000);
                calender_day_events_tasks.addView(event_block);
            }
            last_block_end = (GregorianCalendar) b.getEnd();

            event_block = (LinearLayout) inflater.inflate(R.layout.event_block, calender_day_events_tasks, false);
            Util.setWeight(event_block, b.getDuration());
            calender_day_events_tasks.addView(event_block);

            for (Column c : b.getColumns()) {
                event_coloumn = (LinearLayout) inflater.inflate(R.layout.event_coloumn, event_block, false);
                event_coloumn.setWeightSum(b.getDuration());
                event_block.addView(event_coloumn);

                GregorianCalendar last_event_end = (GregorianCalendar) b.getStart().clone();
                for (MyEvent e : c.getEvents()) {
                    if (Util.earlierDate(last_event_end, e.getStart())) {
                        //create empty event...
                        event = (LinearLayout) inflater.inflate(R.layout.event_empty, event_coloumn, false);
                        Util.setWeight(event, Util.getMinutesBetweenDates(last_event_end, e.getStart()));
                        event_coloumn.addView(event);
                    }

                    event = (LinearLayout) inflater.inflate(R.layout.event, event_coloumn, false);

                    String event_name = "";
                    if (e.getTask() != null) {
                        event_name = e.getTask().getName() + " - " + Util.getFormattedDuration(Util.getMinutesBetweenDates(e.getStart(), e.getEnd()));
                        if (e.getTask().hasRepeat()) {
                            ((TextView) event.findViewById(R.id.event_name)).setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.stat_notify_sync_noanim, 0);
                        }
                    } else {
                        event_name = e.getName() + " - " + Util.getFormattedTimeToTime(e.getStart(), e.getEnd());
                    }
                    ((TextView) event.findViewById(R.id.event_name)).setText(event_name);
                    Util.setColorOfDrawable(event, e.getColor() | 0xFF000000);
                    if (Util.isDarkColor(e.getColor())) {
                        ((TextView) event.findViewById(R.id.event_name)).setTextColor(0xFFFFFFFF);
                    }

                    if (e.getTask() != null) {
                        event.setTag(R.string.task_event, true);
                        event.setTag(R.string.id, e.getId());
                        if (e.isNot_created_by_user()) {
                            ImageView not_done_indicator = (ImageView)event.findViewById(R.id.event_not_done);
                            not_done_indicator.setVisibility(View.VISIBLE);
                            Util.setColorOfDrawable(event, e.getColor() | 0xA0000000);
                        } else {
                            ImageView done_indicator = (ImageView)event.findViewById(R.id.event_done);
                            done_indicator.setVisibility(View.VISIBLE);
                            Util.setColorOfDrawable(event, e.getColor() | 0xFF000000);
                        }
                    } else {
                        event.setTag(R.string.task_event, false);
                        event.setTag(R.string.id, e.getId());
                    }

                    last_event_end = (GregorianCalendar) e.getEnd().clone();
                    int duration = e.getDurationInMinutes();
                    if (duration < MIN_DISPLAY_DURATION_FOR_ONE_EVENT) {
                        last_event_end.add(GregorianCalendar.MINUTE, MIN_DISPLAY_DURATION_FOR_ONE_EVENT - duration);
                        duration = MIN_DISPLAY_DURATION_FOR_ONE_EVENT;
                    }
                    Util.setWeight(event, duration);
                    event_coloumn.addView(event);
                }
            }
        }
    }

    public void drawWholeDayEvents(LinearLayout height_container, LinearLayout top_container_events, LayoutInflater inflater) {
        top_container_events.removeAllViewsInLayout();

        LinearLayout event;

        if (this.events_whole_day.size() > 0) {
            top_container_events.setVisibility(View.VISIBLE);

            height_container.setPadding(height_container.getPaddingLeft(),
                    Util.calculatePixelFromDB(95),
                    height_container.getPaddingRight(),
                    height_container.getPaddingBottom());
        }
        //draw events that are longer than a day...
        for (MyEvent e : this.events_whole_day) {
            event = (LinearLayout) inflater.inflate(R.layout.event_top_container, top_container_events, false);
            ((TextView) event.findViewById(R.id.event_name)).setText(e.getName());
            Util.setColorOfDrawable(event, e.getColor() | 0xFF000000);
            if (Util.isDarkColor(e.getColor())) {
                ((TextView) event.findViewById(R.id.event_name)).setTextColor(0xFFFFFFFF);
            }
            if (e.getTask() != null) {
                event.setTag(R.string.task_event, true);
                event.setTag(R.string.id, e.getTask().getId());
                Util.setColorOfDrawable(event, e.getColor() | 0xAF000000);
            } else {
                event.setTag(R.string.task_event, false);
                event.setTag(R.string.id, e.getId());
            }
            top_container_events.addView(event);
        }
    }

    public void addAllEvents(ArrayList<MyEvent> events) {
        for (MyEvent e : events) {
            addEvent(e);
        }
    }

    public void drawEarliestStartLatestEndIndicators(View earliest_start_latest_end_indicators) {
        View earliest_start_indicator = (View) earliest_start_latest_end_indicators.findViewById(R.id.earliest_start_indicator);
        View between_indicators = (View) earliest_start_latest_end_indicators.findViewById(R.id.between_indicators);
        View latest_end_indicator = (View) earliest_start_latest_end_indicators.findViewById(R.id.latest_end_indicator);

        int earliest_minute = day_settings.getEarliest_minute();
        int latest_minute = day_settings.getLatest_minute();
        GregorianCalendar now = new GregorianCalendar();
        int now_minute = Util.getMinuteOfDay(now);
        if (Util.isSameDate(start, now)) {
            earliest_minute = Math.max(earliest_minute, now_minute);
            latest_minute = Math.max(latest_minute, now_minute);
        }

        if (!(day_settings.isWorkingDay(Util.calculateWorkingDay(start))) ||
                Util.earlierDate(end, now)) {
            Util.setWeight(earliest_start_indicator, 1440);
            Util.setWeight(between_indicators, 0);
            Util.setWeight(latest_end_indicator, 0);
        } else {
            Util.setWeight(earliest_start_indicator, earliest_minute);
            Util.setWeight(between_indicators, latest_minute - earliest_minute);
            Util.setWeight(latest_end_indicator, 60 * 24 - latest_minute);
        }

    }

    public void drawNowIndicator(View now_view_container) {
        View now_offset = now_view_container.findViewById(R.id.now_view_offset);
        View now_indicator = now_view_container.findViewById(R.id.now_indicator);
        if (Util.isSameDate(start, new GregorianCalendar())) {
            Util.setWeight(now_indicator, 2);
            Util.setWeight(now_offset, (int)Util.getMinutesBetweenDates(start, new GregorianCalendar()));
        } else {
            Util.setWeight(now_indicator, 0);
            Util.setWeight(now_offset, 0);
        }
    }

    public void addDistributedWorkTime(int minutes_to_add) {
        this.already_distributed += minutes_to_add;
    }

    public void setDistributedMinutes(int value) {
        this.already_distributed = value;
    }


    private int getAvailableWorkTime(boolean set_worked) {
        int available_work_time = scheduler.getPossibleWorkTime();
        available_work_time = checkAvailableWorkTime(available_work_time, set_worked, true);
        return available_work_time;
    }


    @Override
    public int compareTo(Object another) {
        Day other = (Day) another;
        if (this.start == null) {
            return 1;
        } else if (other.start == null) {
            return -1;
        }
        return this.start.compareTo(other.start);
    }

    public int calculateWorkedTimeOfTask(Task task) {
        int worked_time = 0;
        for (MyEvent e : events) {
            if (!e.isNot_created_by_user() && e.getTask() == task) {
                worked_time += e.getDurationInMinutes();
            }
        }
        return worked_time;
    }

    public boolean isAlreadyDistributed() {
        return already_distributed_boolean;
    }
    public void alreadyDistributed(boolean value) {
        this.already_distributed_boolean = value;
    }

    public ArrayList<MyEvent> getListWorkedTaskEvents() {
        ArrayList<MyEvent> worked_task_events = new ArrayList<>();
        for (MyEvent e : events) {
            if (!e.isNot_created_by_user() && e.getTask() != null && !e.isRepeatingTaskEvent()) {
                worked_task_events.add(e);
            }
        }
        return worked_task_events;
    }

    public void distributeTaskBlocks(Activity activity, boolean without_lock) {
        if (already_distributed_boolean || Util.earlierDate(this.end, new GregorianCalendar())) {
            already_distributed_boolean = true;
            return;
        }
        Day day_before = TaskBroContainer.getDayBefore(activity, this.start);
        day_before.distributeTaskBlocks(activity, without_lock);

        if (!without_lock) {
            TaskBroContainer.getCalculateDaysLock().writeLock().lock();
        }
        //else: I am the last day to distribute
        resetForCalculation(activity);

        ArrayList<MyEvent> worked_task_events = getListWorkedTaskEvents();
        for (MyEvent e : worked_task_events) {
            Log.d(tag, "worked task event = " + e.description() + description());
        }

        for (TaskBlock tb : TaskBroContainer.getTaskBlocks()) {
            tb.calculateTaskFillingFactors();
        }
        // 1) theoretische zuordnung
        //scheduler.getSchedulerLock().writeLock().lock();
            int theoretical_work_time = getTheoreticalWorkTime(getStart(), getEnd());
            ArrayList<TaskWithDuration> task_durations = TaskBroContainer.getTaskDurationsBecauseOfTaskBlocks(theoretical_work_time, worked_task_events, null);
        //scheduler.getSchedulerLock().writeLock().unlock();
        Log.d(tag, "theoretical work time = "+ theoretical_work_time);
        for (TaskWithDuration twd : task_durations) {
            Log.d(tag, Util.getFormattedDate(this.start) + " theoretical: " + twd.getTask().description() + " duration = " + twd.getDurationInMinutes());
        }

        // 2) effektiv bereits gearbeitete zeit abziehen
        ArrayList<TaskWithDuration> correct_task_duration = new ArrayList<>();
        ArrayList<Task> tasks_to_ignore = new ArrayList<>();

        for (TaskWithDuration twd : task_durations) {
            int worked_time = calculateWorkedTimeOfTask(twd.getTask());
            if (worked_time > 0) {
                twd.addDuration(-worked_time);
                correct_task_duration.add(twd);
                tasks_to_ignore.add(twd.getTask());
            }
        }
/*
        for (MyEvent wte : worked_task_events) {
            for (TaskWithDuration twd : task_durations) {
                if (twd.getTask() == wte.getTask()) { //if there is already worked today
                    int diff = twd.getDurationInMinutes() - wte.getDurationInMinutes();
                    if (diff > 0) {
                        twd.addDuration(-wte.getDurationInMinutes());
                        correct_task_duration.add(twd);
                   } else { //more worked than necessary! or exactly equal work time!
                        twd.setDuration(0);
                    }
                    tasks_to_ignore.add(twd.getTask());
                }
            }
        }
        */
        for (TaskWithDuration twd : task_durations) {
            twd.getTask().already_distributed_duration -= twd.getDurationInMinutes();
        }

        int wieder_freie_zeit = 0;
        for (TaskWithDuration twd : correct_task_duration) {
            wieder_freie_zeit += twd.getDurationInMinutes();
            twd.getTask().already_distributed_duration += twd.getDurationInMinutes();
            Log.d(tag, "correct = " + twd.getTask().description() + " with duration = " + twd.getDurationInMinutes());
        }

        resetForCalculation(activity);
        createTaskEvents(correct_task_duration);

        // 3) übrig gebliebene zeit wieder neu vergeben
        //nicht notwendig, da eh possible worktime berechnet wird und die anderen tasks bereits drinnen sind!

        // 4) alles andere normal verteilen, allerdings ohne dieser Tasks!
        //scheduler.getSchedulerLock().writeLock().lock();
            int possible_work_time = getPossibleWorkTime(getStart(), getEnd(), false);
            //possible_work_time -= wieder_freie_zeit;
            Log.d(tag, "possible work time = " + possible_work_time + " for " + description());
            ArrayList<TaskWithDuration> task_duration_possible = TaskBroContainer.getTaskDurationsBecauseOfTaskBlocks(possible_work_time, worked_task_events, tasks_to_ignore);
        //scheduler.getSchedulerLock().writeLock().unlock();
        createTaskEvents(task_duration_possible);

        already_distributed_boolean = true;
        if (!without_lock) {
            TaskBroContainer.getCalculateDaysLock().writeLock().unlock();
        }
    }

    public void deleteRepeatingTaskEvents() {
        ArrayList<MyEvent> events_to_delete = new ArrayList<>();
        for (MyEvent e : events) {
            if (e.isRepeatingTaskEvent()) {
                events_to_delete.add(e);
            }
        }
        events.removeAll(events_to_delete);
    }

    public void deleteTaskEvents() {
        ArrayList<MyEvent> events_to_delete = new ArrayList<>();
        for (MyEvent e : events) {
            if (e.getTask() != null && e.isNot_created_by_user()) {
                events_to_delete.add(e);
            }
        }
        events.removeAll(events_to_delete);
    }

    private class Block {
        TimeObj time;
        ArrayList<Column> columns;

        Block(GregorianCalendar start, GregorianCalendar end) {
            columns = new ArrayList<>();
            time = new TimeObj(start, end);
        }
        public void addEvent(MyEvent e) {
            if (Util.earlierDate(time.end, e.getEnd())) {
                time.end = (GregorianCalendar) e.getEnd().clone();
            }
            for (Column c : columns) {
                if (Util.earlierDateOrSame(c.getEnd(), e.getStart())) {
                    //possible:
                    c.addEvent(e);
                    return;
                }
            }
            Column new_column = new Column(e.getStart(), e.getEnd());
            columns.add(new_column);
            new_column.addEvent(e);
        }

        public TimeObj getTimeObj() {
            return this.time;
        }

        public GregorianCalendar getStart() {
            return time.start;
        }
        public GregorianCalendar getEnd() {
            return time.end;
        }

        public String description() {
            return "Block: #columns = " + columns.size() + " " + this.time.description();
        }

        public void printColumns() {
            Log.d(tag, "Columns: " );
            for (Column c : columns) {
                Log.d(tag, "  " + c.description());
            }
        }

        public ArrayList<Column> getColumns() {
            return columns;
        }

        public int getDuration() {
            return time.getDuration();
        }

        public void setEnd(GregorianCalendar end) {
            time.end = (GregorianCalendar)end.clone();
        }
        public void setStart(GregorianCalendar start) {
            time.start = (GregorianCalendar)start.clone();
        }

    }
    private class Column {
        TimeObj time;
        ArrayList<MyEvent> events;
        public Column(GregorianCalendar start, GregorianCalendar end) {
            events = new ArrayList<>();
            time = new TimeObj(start, end);
        }
        public void addEvent(MyEvent e) {
            if (Util.earlierDate(time.end, e.getEnd())) {
                time.end = (GregorianCalendar) e.getEnd().clone();
            }
            events.add(e);
        }
        public GregorianCalendar getEnd() {
            return time.end;
        }

        public String description() {
            return "Column #events = " + events.size() + " time: " + time.description();
        }

        public ArrayList<MyEvent> getEvents() {
            return events;
        }

        public GregorianCalendar getStart() {
            return this.time.start;
        }
    }
}


