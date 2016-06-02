package brothers.scheucher.taskalender;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.util.Collections.sort;


/**
 * Created by Michael on 09.07.2015.
 */
public class Day {
    private static final String tag = "Day";

    private GregorianCalendar start;
    private GregorianCalendar end;
    private ArrayList<MyEvent> events;
    private DayScheduler scheduler;
    private ArrayList<Block> event_blocks;
    private ArrayList<MyEvent> events_whole_day;

    public Day() {
        this.start = new GregorianCalendar();
        Util.setTime(this.start, 0, 0);
        this.end = (GregorianCalendar)start.clone();
        this.end.add(GregorianCalendar.DAY_OF_YEAR, 1);
        this.events = new ArrayList<MyEvent>();
        this.scheduler = new DayScheduler(start);
        this.event_blocks = new ArrayList<>();
        this.events_whole_day = new ArrayList<>();
    }

    public Day(GregorianCalendar date) {
        this.start = date;
        Util.setTime(this.start, 0, 0);
        this.end = (GregorianCalendar)start.clone();
        this.end.add(GregorianCalendar.DAY_OF_YEAR, 1);
        this.events = new ArrayList<MyEvent>();
        this.scheduler = new DayScheduler(date);
        GregorianCalendar now = new GregorianCalendar();
        if (Util.isSameDate(this.start, now)) {
            //if today: past is not possible...!
            GregorianCalendar today_start = new GregorianCalendar();
            Util.setTime(today_start, 0, 0);
            scheduler.addBlockingTime(today_start, now);
        }
        this.event_blocks = new ArrayList<>();
        this.events_whole_day = new ArrayList<>();
    }


    public DayScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(DayScheduler scheduler) {
        this.scheduler = scheduler;
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

    public void setEvents(ArrayList<MyEvent> events) {
        this.events = events;
    }

    public void addEvent(MyEvent event_to_add) {
        if (!this.events.contains(event_to_add)) {
            for (MyEvent e : this.events) {
                if (e.getExternID() != -1 && e.getExternID() == event_to_add.getExternID()) {
//                    Log.d(tag, "addEvent: already exists, externID-proof!!!" + event_to_add.description());
                    return;
                }
            }
            this.events.add(event_to_add);

            if (event_to_add.isBlocking()) {
                this.scheduler.addBlockingTime(event_to_add.getStart(), event_to_add.getEnd());
            }
//            Log.d(tag, "addEvent: Event hinzugefügt... " + event_to_add.description());
        } else {
//            Log.d(tag, "addEvent: already exists..." + event_to_add.description());
        }
    }

    private void addEventWithoutChecking(MyEvent new_event) {
        events.add(new_event);
        Log.d(tag, "addEventWithoutChecking: " + new_event.description());
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

    public int getPossibleWorkTime(GregorianCalendar start_date, GregorianCalendar end_date) {
        int sum_work_time = 0;

        TimeObj dates_to_check = new TimeObj(start_date, end_date);

        for (TimeObj to : this.scheduler.getFreeSlots()) {
            TimeObj overlapping = Util.calculateOverlappingTime(to, dates_to_check);
            if (overlapping != null) {
                sum_work_time += Util.getMinutesBetweenDates(overlapping.start, overlapping.end);
            }
        }

        return sum_work_time;
    }

    public void addTask(Task task) {
        Log.d(tag, "addTask: " + task.description() + " to day: " + this.description());
        GregorianCalendar start_of_day = (GregorianCalendar)this.start.clone();
        GregorianCalendar end_of_day = (GregorianCalendar)this.end.clone();

        int available_work_time = getPossibleWorkTime(start_of_day, end_of_day);
        int work_time_for_that_task = (int)(available_work_time * task.getFilling_factor());
        Log.d(tag, "   worktime: " + work_time_for_that_task + " from overall available: " + available_work_time);
        if (work_time_for_that_task > (task.getRemaining_duration() - task.already_distributed_duration)) { //not necessary to have so much time... ;)
            work_time_for_that_task = task.getRemaining_duration() - task.already_distributed_duration; //rest of duration...
            task.already_distributed_duration = task.getRemaining_duration();
        } else {
            task.already_distributed_duration += work_time_for_that_task;
        }
        Log.d(tag, "   worktime is now: " + work_time_for_that_task);
        while(work_time_for_that_task > 0) {
            TimeObj free_slot = this.scheduler.getFreeSlotOrBiggest(work_time_for_that_task);

            MyEvent new_event = new MyEvent();
            new_event.setNot_created_by_user(true);
            new_event.setTask(task);
            new_event.setName(task.getName());

            new_event.setStart((GregorianCalendar)free_slot.start.clone());
            new_event.setStart(free_slot.start.get(Calendar.HOUR_OF_DAY), free_slot.start.get(Calendar.MINUTE));
            int effective_time = Math.min(free_slot.getDuration(), work_time_for_that_task);
            new_event.setEndWithDuration(effective_time);
            work_time_for_that_task -= effective_time;

            Log.d(tag, "   going to addEvent " + new_event.description());
            addEventWithoutChecking(new_event);
        }


        sortEvents();
    }



    public String getPotential() {
        if (Util.earlierDate(this.end, new GregorianCalendar())) {
            return "Kein Potential, da Vergangenheit";
        }
        ArrayList<Integer> potential = new ArrayList<>();
        TimeObj day_time = new TimeObj(this.start, this.end);
        String ret = "";
        boolean found = false;
        for (TaskBlock tb : TimeRank.getTaskBlocks()) {
            TimeObj overlapping = Util.calculateOverlappingTime(day_time, new TimeObj(tb.getStart(), tb.getEnd()));
            if (overlapping != null) {
                if (found) {
                    ret += System.getProperty("line.separator");
                }
                ret += Util.getFormattedDateTimeToDateTime(tb.getStart(), tb.getEnd()) + ": " + Util.getFormattedPotential(tb.getPotential());
                Log.d(tag, "Potential found " + tb.getPotential() + " in block " + tb.description());
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
//                Log.d(tag, "Event is over the whole day: " + e.description());
                events_whole_day.add(e);
                continue;
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
        }

        for (Block b : this.event_blocks) {
            Log.d(tag, b.description());
            b.printColumns();
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
                Util.setWeight(event_block, Util.getMinutesBetweenDates(last_block_end, b.getStart()));
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
                    last_event_end = (GregorianCalendar) e.getEnd().clone();

                    event = (LinearLayout) inflater.inflate(R.layout.event, event_coloumn, false);
                    ((TextView) event.findViewById(R.id.event_name)).setText(e.getName());
                    event.setBackgroundColor(e.getColor() | 0xFF000000);
                    if (Util.isDarkColor(e.getColor())) {
                        ((TextView) event.findViewById(R.id.event_name)).setTextColor(0xFFFFFFFF);
                    }
                    if (e.getTask() != null) {
                        event.setTag(R.string.task_event, true);
                        event.setTag(R.string.id, e.getTask().getId());
                        event.setBackgroundColor(0xA0000000 | e.getColor());
                    } else {
                        event.setTag(R.string.task_event, false);
                        event.setTag(R.string.id, e.getId());
                    }

                    Util.setWeight(event, e.getDurationInMinutes());
                    event_coloumn.addView(event);
                }
            }
        }


    }

    public void drawWholeDayEvents(LinearLayout top_container_events, LayoutInflater inflater) {
        top_container_events.removeAllViewsInLayout();

        LinearLayout event;

        //draw events that are longer than a day...
        for (MyEvent e : this.events_whole_day) {
            top_container_events.setVisibility(View.VISIBLE);

            event = (LinearLayout) inflater.inflate(R.layout.event_top_container, top_container_events, false);
            ((TextView) event.findViewById(R.id.event_name)).setText(e.getName());
            event.setBackgroundColor(e.getColor() | 0xFF000000);
            if (Util.isDarkColor(e.getColor())) {
                ((TextView) event.findViewById(R.id.event_name)).setTextColor(0xFFFFFFFF);
            }
            if (e.getTask() != null) {
                event.setTag(R.string.task_event, true);
                event.setTag(R.string.id, e.getTask().getId());
                event.setBackgroundColor(0xA0000000 | e.getColor());
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
                if (Util.earlierDate(c.getEnd(), e.getStart())) {
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
            return "Block: #columns = " + columns.size() + " time: " + this.time.description();
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


