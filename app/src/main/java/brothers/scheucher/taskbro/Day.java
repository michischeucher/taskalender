package brothers.scheucher.taskbro;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.util.Collections.sort;

public class Day {
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


    public Day(GregorianCalendar date) {
        this.start = (GregorianCalendar)date.clone();
        Util.setTime(this.start, 0, 0);
        this.end = (GregorianCalendar)start.clone();
        this.end.add(GregorianCalendar.DAY_OF_YEAR, 1);
        this.events = new ArrayList<>();
        this.scheduler = new DayScheduler(this.start);
        GregorianCalendar now = new GregorianCalendar();
        if (Util.isSameDate(this.start, now)) {
            //if today: past is not possible...!
            GregorianCalendar today_start = new GregorianCalendar();
            Util.setTime(today_start, 0, 0);
            scheduler.addBlockingTime(today_start, now);
        } else if (Util.earlierDate(this.start, now)) {
            scheduler.addBlockingTime(this.start, 0, 60*24);
        }


        this.event_blocks = new ArrayList<>();
        this.events_whole_day = new ArrayList<>();
        this.day_settings = TaskBroContainer.getDaySettingObject(this.start);

        GregorianCalendar earliest_start_date = (GregorianCalendar)start.clone();
        earliest_start_date.add(GregorianCalendar.MINUTE, day_settings.getEarliest_minute());
        scheduler.addBlockingTime(start, earliest_start_date);

        GregorianCalendar latest_minute_date = (GregorianCalendar)start.clone();
        latest_minute_date.add(GregorianCalendar.MINUTE, day_settings.getLatest_minute());
        scheduler.addBlockingTime(latest_minute_date, end);

        if (!day_settings.isWorkingDay(Util.calculateWorkingDay(date))) {
            scheduler.addBlockingTime(start, end);
        }
        this.already_distributed = 0;
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
                    //Log.d(tag, "addEvent: already exists, externID-proof!!!" + event_to_add.description());
                    return;
                }
            }
            this.events.add(event_to_add);

            if (event_to_add.isBlocking()) {
                this.scheduler.addBlockingTime(event_to_add.getStart(), event_to_add.getEnd());
            }//            Log.d(tag, "addEvent: Event hinzugefügt... " + event_to_add.description());
        }// else { Log.d(tag, "addEvent: already exists..." + event_to_add.description());}
    }

    private void addTaskEventWithoutChecking(MyEvent new_event) {
        events.add(new_event);
        //Log.d(tag, "addTaskEventWithoutChecking: " + new_event.description());
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
        int sum_work_time = 0;

        TimeObj dates_to_check = new TimeObj(start_date, end_date);

        for (TimeObj to : this.scheduler.getFreeSlots()) {
            TimeObj overlapping = Util.calculateOverlappingTime(to, dates_to_check);
            if (overlapping != null) {
                sum_work_time += (int)Util.getMinutesBetweenDates(overlapping.start, overlapping.end);
            }
        }

        Log.d(tag, "getPossibleWorkTime for " + description() + " worktime=" + sum_work_time);
        sum_work_time = checkAvailableWorkTime(sum_work_time, set_worked);

        Log.d(tag, "getPossibleWorkTime for " + description() + " worktime after checking=" + sum_work_time);
        return sum_work_time;
    }

    private int checkAvailableWorkTime(int available_work_time, boolean set_worked) {
        int already_worked = calculateWorkedTime();
        Log.d(tag, description() + " already worked = " + already_worked);
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

    private int calculateWorkedTime() {
        DayScheduler scheduler = new DayScheduler(this.start);
        scheduler.addBlockingTime(this.start, 0, day_settings.getEarliest_minute());
        scheduler.addBlockingTime(this.start, day_settings.getLatest_minute(), 24 * 60);
        for (MyEvent e : this.events) {
            if (e.isBlocking()) {
                scheduler.addBlockingTime(e.getStart(), e.getEnd());
            }
        }
        return 24 * 60 - scheduler.getPossibleWorkTime() - day_settings.getEarliest_minute() - (24 * 60 - day_settings.getLatest_minute());
    }

    public void addTask(Task task) {
        //Log.d(tag, "addTask: " + task.description() + " to day: " + this.description());

        int available_work_time = scheduler.getPossibleWorkTime();
        available_work_time = checkAvailableWorkTime(available_work_time, false);
        int work_time_for_that_task = (int)(available_work_time * task.getFilling_factor());

        if (work_time_for_that_task > (task.getRemaining_duration() - task.already_distributed_duration)) { //not necessary to have so much time... ;)
            work_time_for_that_task = task.getRemaining_duration() - task.already_distributed_duration; //rest of duration...
            task.already_distributed_duration = task.getRemaining_duration();
        } else {
            task.already_distributed_duration += work_time_for_that_task;
        }

        while(work_time_for_that_task > 0) {
            TimeObj free_slot = scheduler.getFreeSlotOrBiggest(work_time_for_that_task);

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

            //Log.d(tag, "   going to addEvent " + new_event.description());
            addTaskEventWithoutChecking(new_event);
        }

        sortEvents();
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
                //Log.d(tag, "Potential found " + tb.getPotential() + " in block " + tb.description());
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
            if (e.getDurationInMinutes() >= 60 * 24) {
                //Log.d(tag, "Event is over the whole day: " + e.description());
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

        /*for (Block b : this.event_blocks) {
            Log.d(tag, b.description());
            b.printColumns();
        }*/
    }

    public void drawEvents(LinearLayout calender_day_events_tasks, LayoutInflater inflater) {
        calender_day_events_tasks.removeAllViewsInLayout();

        LinearLayout event_block;
        LinearLayout event_coloumn;
        LinearLayout event;

        GregorianCalendar last_block_end = (GregorianCalendar) this.start.clone();
        Log.d(tag, "draw events for " + description());
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
                Log.d(tag, "coloumn duration = " + b.getDuration());
                event_block.addView(event_coloumn);

                GregorianCalendar last_event_end = (GregorianCalendar) b.getStart().clone();
                for (MyEvent e : c.getEvents()) {
                    Log.d(tag, e.description());
                    if (Util.earlierDate(last_event_end, e.getStart())) {
                        //create empty event...
                        event = (LinearLayout) inflater.inflate(R.layout.event_empty, event_coloumn, false);
                        Util.setWeight(event, Util.getMinutesBetweenDates(last_event_end, e.getStart()));
                        event_coloumn.addView(event);
                    }


                    String event_name = "";
                    if (e.getTask() != null) {
                        event_name = e.getTask().getName() + " - " + Util.getFormattedDuration(Util.getMinutesBetweenDates(e.getStart(), e.getEnd()));
                    } else {
                        event_name = e.getName() + " - " + Util.getFormattedTimeToTime(e.getStart(), e.getEnd());
                    }
                    event = (LinearLayout) inflater.inflate(R.layout.event, event_coloumn, false);
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
                        } else {
                            ImageView done_indicator = (ImageView)event.findViewById(R.id.event_done);
                            done_indicator.setVisibility(View.VISIBLE);
                        }
                        Util.setColorOfDrawable(event, e.getColor() | 0xA0000000);
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

        Log.d(tag, "finished drawing events for " + description());


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


