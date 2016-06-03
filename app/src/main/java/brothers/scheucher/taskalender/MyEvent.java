package brothers.scheucher.taskalender;

import android.content.Context;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Michael on 08.07.2015.
 */
public class MyEvent implements Comparable {
    private static final String tag = "MyEvent";

    private int id;
    private long extern_id;
    private String name;
    private String notice;
    private GregorianCalendar start;
    private GregorianCalendar end;
    private boolean availability;

    private Task task;
    private boolean not_created_by_user; //if set, then this will not be saved!

    private String priority;

    private int color;

    //database
    public static final String DB_TABLE = "EventTable";
    public static final String DB_COL_ID = "EventID";
    public static final String DB_COL_NAME = "EventName";
    public static final String DB_COL_NOTICE = "EventNotice";
    public static final String DB_COL_START = "EventStart";
    public static final String DB_COL_END = "EventEnd";
    public static final String DB_COL_TASK_ID = "EventTaskID";
    public static final String DB_COL_AVAILABILITY = "EventAvailability";
    public static final String DB_COL_PRIORITY = "EventPriority";

    public MyEvent() {
        this.id = -1;
        this.extern_id = -1;
        this.name = "";
        this.notice = "";
        this.start = new GregorianCalendar();
        this.start.set(GregorianCalendar.MINUTE, 0);
        this.end = new GregorianCalendar();
        this.end.set(GregorianCalendar.MINUTE, 0);
        this.end.add(GregorianCalendar.HOUR_OF_DAY, 1);
        this.not_created_by_user = false;
        this.priority = "C";
        this.task = null;
        this.color = -1;
        this.availability = false;
    }
    public MyEvent(int id) {
        this.id = id;
        this.extern_id = -1;
        this.name = "";
        this.notice = "";
        this.start = new GregorianCalendar();
        this.start.set(GregorianCalendar.MINUTE, 0);
        this.end = new GregorianCalendar();
        this.end.set(GregorianCalendar.MINUTE, 0);
        this.end.add(GregorianCalendar.HOUR_OF_DAY, 1);
        this.not_created_by_user = false;
        this.priority = "C";
        this.task = null;
        this.color = -1;
        this.availability = false;
    }
    public MyEvent(int duration, boolean is_task_event) {
        this.id = -1;
        this.extern_id = -1;
        this.name = "";
        this.notice = "";
        this.start = new GregorianCalendar();
        this.end = new GregorianCalendar();
        this.priority = "C";
        if (is_task_event) {
            this.start.add(GregorianCalendar.MINUTE, -duration);
        }
        this.not_created_by_user = false;
        this.task = null;
        this.color = -1;
        this.availability = false;
        checkBlocking();
    }
    public MyEvent(long extern_id) {
        this.id = -1;
        this.extern_id = extern_id;
        this.name = "";
        this.notice = "";
        this.start = new GregorianCalendar();
        this.end = new GregorianCalendar();
        this.priority = "C";
        this.not_created_by_user = true;
        this.task = null;
        this.color = -1;
        this.availability = false;
    }


    public void setStart(int year, int month, int day) {
        Util.setDate(this.start, year, month, day);
    }
    public void setEnd(int year, int month, int day) {
        Util.setDate(this.end, year, month, day);
    }

    public void setStart(int hourOfDay, int minute) {
        Util.setTime(this.start, hourOfDay, minute);
    }

    public void setEnd(int hourOfDay, int minute) {
        Util.setTime(this.end, hourOfDay, minute);
    }


    public void checkBlocking() {
        //FIXME: Ganztägige Termine sollten nicht blockierend sein!
        if (Util.getMinutesBetweenDates(this.start, this.end) >= 24 * 60) {
            this.availability = true;
        }
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

    public boolean isNot_created_by_user() {
        return not_created_by_user;
    }

    public void setNot_created_by_user(boolean not_created_by_user) {
        this.not_created_by_user = not_created_by_user;
    }

    public GregorianCalendar getStart() {
        return start;
    }

    public void setStart(GregorianCalendar start) {
        this.start = start;
    }

    public GregorianCalendar getEnd() {
        return end;
    }

    public void setEnd(GregorianCalendar end) {
        this.end = end;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void save(Context context) {
        if (id == -1) {
            id = TimeRank.getNewEventID();
        }

        SQLiteStorageHelper db_helper = SQLiteStorageHelper.getInstance(context, 1);
        db_helper.openDB();
        db_helper.saveEvent(this);
        db_helper.closeDB();
        db_helper.close();
    }

    public void delete(Context context) {
        if (id == -1) {
            return;
        } else {
            SQLiteStorageHelper db_helper = SQLiteStorageHelper.getInstance(context, 1);
            db_helper.openDB();
            db_helper.deleteEvent(this);
            db_helper.closeDB();
            db_helper.close();
            TimeRank.deleteEventFromList(this);
        }
    }

    public String description() {
        return "name = " + name + " start = " + Util.getFormattedDateTime(start) + " end = " + Util.getFormattedDateTime(end) + " availability: " + this.availability;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public boolean isTotallyInThatDay(GregorianCalendar day_to_check) {
        if (this.start.get(GregorianCalendar.YEAR) == day_to_check.get(GregorianCalendar.YEAR)
                && this.start.get(GregorianCalendar.DAY_OF_YEAR) == day_to_check.get(GregorianCalendar.DAY_OF_YEAR)
                && this.end.get(GregorianCalendar.YEAR) == day_to_check.get(GregorianCalendar.YEAR)
                && this.end.get(GregorianCalendar.DAY_OF_YEAR) == day_to_check.get(GregorianCalendar.DAY_OF_YEAR)) {
            //völlig in diesem Tag
            return true;
        } else {
            return false;
        }
    }
    public boolean isRelevantFotThatDay(GregorianCalendar day_to_check) {
        if (isTotallyInThatDay(day_to_check)) {
            return true;
        }
        if (this.start.get(GregorianCalendar.DAY_OF_YEAR) <= day_to_check.get(GregorianCalendar.DAY_OF_YEAR)
                && this.start.get(GregorianCalendar.YEAR) <= day_to_check.get(GregorianCalendar.YEAR)
                && this.end.get(GregorianCalendar.DAY_OF_YEAR) >= day_to_check.get(GregorianCalendar.DAY_OF_YEAR)
                && this.end.get(GregorianCalendar.YEAR) >= day_to_check.get(GregorianCalendar.YEAR)) {
            //ganztägig oder es hört an dem tag auf bzw. fängt an
            return true;
        }
        return false;
    }

    public int getEndMinute() {
        return Util.getMinuteOfDay(this.end);
    }
    public int getStartMinute() {
        return Util.getMinuteOfDay(this.start);
    }

    public boolean hasProblem(GregorianCalendar date, int start_minute, int end_minute) {
        if (!isTotallyInThatDay(date)) {
            return false;
        }
        int event_start = getStartMinute() - Settings.getPause_before_event();
        int event_end = getEndMinute() + Settings.getPause_after_event();
        if (event_start <= start_minute && start_minute < event_end)
            return true;
        if (event_start < end_minute && end_minute <= event_end)
            return true;
        if (start_minute <= event_start && event_end <= end_minute)
            return true;
        return false;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }


    @Override
    public int compareTo(Object another) {
        MyEvent other = (MyEvent)another;
        return this.start.compareTo(other.start);
    }

    //returns a list of dates e.g. if start is 1.3. and end of event is 4.3. this returns 1.3,2.3,3.3,4.3
    public ArrayList<GregorianCalendar> getDates() {
        return Util.getListOfDates(this.start, this.end);
    }

    public boolean isBlocking() {
        return !availability;
    }

    public void setEndWithDuration(int duration) {
        this.end = (GregorianCalendar) start.clone();
        this.end.add(GregorianCalendar.MINUTE, duration);
    }

    public void setTask(int task_id) {
        Task task = TimeRank.getTask(task_id);
        this.task = task;
    }

    public int getDurationInMinutes() {
        return (int)((this.end.getTimeInMillis() - this.start.getTimeInMillis()) / 1000 / 60);
    }

    public int getColor() {
        if (this.color != -1) {
            return this.color;
        }
        if (this.task != null) {
            this.color = this.task.getColor();
        } else {
            this.color = Util.getRandomColor();
        }
        return this.color;
    }

    public TimeObj getTimeObj() {
        return new TimeObj(this.start, this.end);
    }

    public boolean getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        if (availability > 0) {
            this.availability = true;
        } else {
            this.availability = false;
        }
    }

    public long getExternID() {
        return extern_id;
    }

    public void setExternID(long id) {
        this.extern_id = id;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
}
