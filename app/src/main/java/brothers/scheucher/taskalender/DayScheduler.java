package brothers.scheucher.taskalender;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.ListIterator;

/**
 * Created by michi on 10.05.2016.
 */
public class DayScheduler {
    private static final String tag = "DayScheduler";

    private ArrayList<TimeObj> free_slots;

    public DayScheduler(GregorianCalendar date) {
        free_slots = new ArrayList<TimeObj>();
        TimeObj whole_day = new TimeObj(date);
        free_slots.add(whole_day);
    }

    public void addBlockingTime(GregorianCalendar start, GregorianCalendar end) {
        TimeObj blocking_time = new TimeObj(start, end);
        for (ListIterator<TimeObj> it = this.free_slots.listIterator(); it.hasNext();) {
            TimeObj slot = it.next();
            TimeObj overlapping_time = Util.calculateOverlappingTime(slot, blocking_time);
            if (overlapping_time != null) { //if there is a overlapping
                if (overlapping_time.isSameAs(slot)) {
                    Log.d(tag, "deleted slot: " + slot.description() + " because of blocking time: " + blocking_time.description() + " calculated overlapping: " + overlapping_time.description());
                    it.remove();
                } else if (overlapping_time.sameStart(slot)) {
                    slot.start = overlapping_time.end;
                } else if (overlapping_time.sameEnd(slot)) {
                    slot.end = overlapping_time.start;
                } else { //nothing is equal => overlapping is in the middle
                    TimeObj new_slot = new TimeObj(start);
                    new_slot.start = (GregorianCalendar) overlapping_time.end.clone();
                    new_slot.end = (GregorianCalendar) slot.end.clone();
                    slot.end = overlapping_time.start;
                    it.add(new_slot);
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
        return 0;
    }

    public TimeObj getFreeSlotOrBiggest(int duration_in_minutes) {
        TimeObj ret = getFreeSlot(duration_in_minutes);
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
}
