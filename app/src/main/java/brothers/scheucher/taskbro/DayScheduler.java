package brothers.scheucher.taskbro;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.ListIterator;

public class DayScheduler {
    private static final String tag = "DayScheduler";

    private ArrayList<TimeObj> free_slots;

    public DayScheduler(GregorianCalendar date) {
        free_slots = new ArrayList<>();
        TimeObj whole_day = new TimeObj(date);
        free_slots.add(whole_day);
    }

    public void addBlockingTime(GregorianCalendar start, GregorianCalendar end) {
        //Log.d(tag, "addBlockingTime: " + Util.getFormattedDateTimeToDateTime(start, end));
        TimeObj blocking_time = new TimeObj(start, end);
        for (ListIterator<TimeObj> it = this.free_slots.listIterator(); it.hasNext();) {
            TimeObj slot = it.next();
            TimeObj overlapping_time = Util.calculateOverlappingTime(slot, blocking_time);
            if (overlapping_time != null) { //if there is a overlapping
                if (overlapping_time.isSameAs(slot)) {
                    //Log.d(tag, "deleted slot: " + slot.description() + " because of blocking time: " + blocking_time.description() + " calculated overlapping: " + overlapping_time.description());
                    it.remove();
                } else if (overlapping_time.sameStart(slot)) {
                    slot.start = overlapping_time.end;
                    //Log.d(tag, "same start, new start: " + Util.getFormattedDateTime(slot.start));
                } else if (overlapping_time.sameEnd(slot)) {
                    slot.end = overlapping_time.start;
                    //Log.d(tag, "same end, new end: " + Util.getFormattedDateTime(slot.end));
                } else { //nothing is equal => overlapping is in the middle
                    TimeObj new_slot = new TimeObj(start);
                    new_slot.start = (GregorianCalendar) overlapping_time.end.clone();
                    new_slot.end = (GregorianCalendar) slot.end.clone();
                    slot.end = overlapping_time.start;
                    it.add(new_slot);
                    //Log.d(tag, "overlapping in the middle, new slot: " + Util.getFormattedDateTimeToTime(new_slot.start, new_slot.end));
                }
            }
        }
    }

    public TimeObj getFreeSlot(int minutes) {
        Collections.sort(free_slots);
        for (TimeObj to : free_slots) {
            if (to.getDuration() >= minutes) {
                return to;
            }
        }
        return null;
    }

    public ArrayList<TimeObj> getFreeSlots() {
        return free_slots;
    }

    public void printFreeSlots() {
        Log.d(tag, "--- printing free slot ---");
        for (TimeObj to : free_slots) {
            Log.d(tag, to.description());
        }
        Log.d(tag, "--- end printing free slot ---");
    }

    public int getPossibleWorkTime() {
        int sum_work_time = 0;
        for (TimeObj free_slot : this.free_slots) {
            sum_work_time += free_slot.getDuration();
        }
        return sum_work_time;
    }

    public TimeObj getFreeSlotOrBiggest(int duration_in_minutes) {
        if (free_slots.size() <= 0) {
            return null;
        }
        TimeObj ret = getFreeSlot(duration_in_minutes);
        //if there cannot be found such a big free slot, find the biggest:
        if (ret == null) {
            ret = free_slots.get(0);
            for (TimeObj to : free_slots) {
                if (to.getDuration() > ret.getDuration()) {
                    ret = to;
                }
            }
        }
        return ret;
    }

    public void addBlockingTime(GregorianCalendar date, int start_minute, int end_minute) {
        GregorianCalendar start = (GregorianCalendar)date.clone();
        Util.setTime(start, start_minute / 60, start_minute % 60);
        GregorianCalendar end = (GregorianCalendar)date.clone();
        Util.setTime(end, end_minute / 60, end_minute % 60);
        addBlockingTime(start, end);
    }
}
