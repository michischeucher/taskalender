package brothers.scheucher.taskbro;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DayScheduler {
    private static final String tag = "DayScheduler";

    private ArrayList<TimeObj> free_slots;
    private ReentrantReadWriteLock scheduler_lock;

    public DayScheduler(GregorianCalendar date) {
        free_slots = new ArrayList<>();
        TimeObj whole_day = new TimeObj(date);
        free_slots.add(whole_day);
        this.scheduler_lock = new ReentrantReadWriteLock();
    }
    public DayScheduler(DayScheduler day_scheduler) {
        free_slots = (ArrayList<TimeObj>)day_scheduler.getFreeSlots().clone();
        this.scheduler_lock = new ReentrantReadWriteLock();
    }

    public void addBlockingTime(GregorianCalendar start, GregorianCalendar end) {
        scheduler_lock.writeLock().lock();
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
        scheduler_lock.writeLock().unlock();
    }

    public TimeObj getFreeSlot(int minutes) {
        scheduler_lock.writeLock().lock();
        Collections.sort(free_slots);
        for (TimeObj to : free_slots) {
            if (to.getDuration() >= minutes) {
                scheduler_lock.writeLock().unlock();
                return to;
            }
        }
        scheduler_lock.writeLock().unlock();
        return null;
    }

    public ArrayList<TimeObj> getFreeSlots() {
        return free_slots;
    }

    public void printFreeSlots() {
        scheduler_lock.readLock().lock();
        Log.d(tag, "--- printing free slot ---");
        for (TimeObj to : free_slots) {
            Log.d(tag, to.description());
        }
        Log.d(tag, "--- end printing free slot ---");
        scheduler_lock.readLock().unlock();
    }

    public int getPossibleWorkTime() {
        scheduler_lock.readLock().lock();
        int sum_work_time = 0;
        for (TimeObj free_slot : this.free_slots) {
            sum_work_time += free_slot.getDuration();
        }
        scheduler_lock.readLock().unlock();
        return sum_work_time;
    }

    public TimeObj getFreeSlotOrBiggest(int duration_in_minutes) {
        scheduler_lock.readLock().lock();
        if (free_slots.size() <= 0) {
            scheduler_lock.readLock().unlock();
            return null;
        }
        scheduler_lock.readLock().unlock();

        TimeObj ret = getFreeSlot(duration_in_minutes);
        //if there cannot be found such a big free slot, find the biggest:
        scheduler_lock.readLock().lock();
        if (ret == null) {
            ret = free_slots.get(0);
            for (TimeObj to : free_slots) {
                if (to.getDuration() > ret.getDuration()) {
                    ret = to;
                }
            }
        }
        scheduler_lock.readLock().unlock();
        return ret;
    }

    public void addBlockingTime(GregorianCalendar date, int start_minute, int end_minute) {
        GregorianCalendar start = (GregorianCalendar)date.clone();
        Util.setTime(start, start_minute / 60, start_minute % 60);
        GregorianCalendar end = (GregorianCalendar)date.clone();
        Util.setTime(end, end_minute / 60, end_minute % 60);
        addBlockingTime(start, end);
    }

    public ReentrantReadWriteLock getSchedulerLock() {
        return this.scheduler_lock;
    }
}
