package brothers.scheucher.taskbro;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by michi on 10.05.2016.
 */
public class TimeObj implements Comparable {
    public GregorianCalendar start;
    public GregorianCalendar end;

    public TimeObj(GregorianCalendar date) {
        start = (GregorianCalendar)date.clone();
        end = (GregorianCalendar)date.clone();
        Util.setTime(start, 0, 0);
        Util.setTime(end, 0, 0);
        end.add(Calendar.DAY_OF_YEAR, 1);
    }

    public TimeObj(TimeObj other) {
        this.start = (GregorianCalendar)other.start.clone();
        this.end = (GregorianCalendar)other.end.clone();
    }

    public TimeObj(GregorianCalendar first_date, GregorianCalendar second_date) {
        if (Util.earlierDate(first_date, second_date)) {
            start = (GregorianCalendar)first_date.clone();
            end = (GregorianCalendar)second_date.clone();
        } else {
            start = (GregorianCalendar)second_date.clone();
            end = (GregorianCalendar)first_date.clone();
        }
    }

    @Override
    public int compareTo(Object another) {
        TimeObj other = (TimeObj) another;
        return this.start.compareTo(other.start);
    }

    public String description() {
        return "start: " + Util.getFormattedTime(start) + " end: " + Util.getFormattedTime(end) + " = " + Util.getFormattedDuration((int)Util.getMinutesBetweenDates(start, end));
    }

    public boolean isSameAs(TimeObj other) {
        if (this.sameStart(other) && this.sameEnd(other)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sameStart(TimeObj other) {
        if (Util.isSameDate(this.start, other.start) && Util.isSameTime(this.start, other.start)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sameEnd(TimeObj other) {
        if (Util.isSameDate(this.end, other.end) && Util.isSameTime(this.end, other.end)) {
            return true;
        } else {
            return false;
        }
    }

    public int getDuration() {
        return (int)Util.getMinutesBetweenDates(this.start, this.end);
    }
}
