package brothers.scheucher.taskbro;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.TimeZone;

public class MyCalendarProvider {
    private static final String tag = "MyCalendarProvider";

    private static final String[] projection =
            new String[]{
                    CalendarContract.Instances._ID,
                    CalendarContract.Instances.EVENT_ID,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.ALL_DAY,
                    CalendarContract.Instances.DISPLAY_COLOR,
                    CalendarContract.Instances.CALENDAR_ID,
                    CalendarContract.Instances.CALENDAR_DISPLAY_NAME};

    private static final int PROJECTION_ID_INDEX = 1;
    private static final int PROJECTION_BEGIN_INDEX = 2;
    private static final int PROJECTION_END_INDEX = 3;
    private static final int PROJECTION_TITLE_INDEX = 4;
    private static final int PROJECTION_ALL_DAY_INDEX = 5;
    private static final int PROJECTION_DISPLAY_COLOR_INDEX = 6;
    private static final int PROJECTION_CALENDAR_ID_INDEX = 7;
    private static final int PROJECTION_CALENDAR_DISPLAY_NAME_INDEX = 8;

    public static ArrayList<MyEvent> getEvents(long startMillis, long endMillis) {

        Cursor cursor =
                CalendarContract.Instances.query(TaskBroContainer.getContext().getContentResolver(), projection, startMillis, endMillis);

        ArrayList<MyEvent> events = new ArrayList<>();

        while (cursor.moveToNext()) {
            long id = -1;
            String title = null;
            long beginVal = 0;
            long endVal = 0;
            int all_day = 0;
            int color = 0;
            int calender_id;
            String calender_name;

            // Get the field values
            id = cursor.getLong(PROJECTION_ID_INDEX);
            beginVal = cursor.getLong(PROJECTION_BEGIN_INDEX);
            endVal = cursor.getLong(PROJECTION_END_INDEX);
            title = cursor.getString(PROJECTION_TITLE_INDEX);
            all_day = cursor.getInt(PROJECTION_ALL_DAY_INDEX);
            color = cursor.getInt(PROJECTION_DISPLAY_COLOR_INDEX);
            calender_id = cursor.getInt(PROJECTION_CALENDAR_ID_INDEX);
            calender_name = cursor.getString(PROJECTION_CALENDAR_DISPLAY_NAME_INDEX);

            Log.d(tag, "Title: " + title + " from: " + beginVal + " to " + endVal + " id: " + id + " all_day = " + all_day);
            MyEvent new_event = new MyEvent(TaskBroContainer.getNewEventID());
            new_event.setName(title);
            new_event.getStart().setTimeInMillis(beginVal);
            new_event.getEnd().setTimeInMillis(endVal);
            new_event.checkBlocking();
            new_event.setExternID(id);
            new_event.setColor(color);
            new_event.setCalendarID(calender_id);
            new_event.setCalendarName(calender_name);

            if (all_day > 0) {
                new_event.setAllDay(true);
            }

            events.add(new_event);
        }

        return events;
    }

    public static void saveEvent(MyEvent event_to_save) {
        if (event_to_save.getExternID() == -1) {
            return;
        }

        ContentResolver cr = TaskBroContainer.getContext().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Events.CALENDAR_ID, event_to_save.getCalenderID());
        values.put(CalendarContract.Events.TITLE, event_to_save.getName());
        values.put(CalendarContract.Events.ALL_DAY, event_to_save.isAll_day());
        //values.put(CalendarContract.Events.DISPLAY_COLOR, event_to_save.getColor());
        values.put(CalendarContract.Events.DTSTART, event_to_save.getStart().getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, event_to_save.getEnd().getTimeInMillis());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());// event_to_save.getStart().getTimeZone().getID());
        //values.put(CalendarContract.Events.EVENT_END_TIMEZONE, TimeZone.getDefault().getID());// event_to_save.getStart().getTimeZone().getID());

        if (ActivityCompat.checkSelfPermission(TaskBroContainer.getContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(tag, "ERROR: Permission missing");
            return;
        }
        Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event_to_save.getExternID());
        cr.update(uri, values, null, null);
    }

    public static void deleteEvent(MyEvent event_to_delete) {
        if (event_to_delete.getExternID() == -1) {
            return;
        }
        ContentResolver cr = TaskBroContainer.getContext().getContentResolver();
        Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event_to_delete.getExternID());
        cr.delete(uri, null, null);
    }

    }
