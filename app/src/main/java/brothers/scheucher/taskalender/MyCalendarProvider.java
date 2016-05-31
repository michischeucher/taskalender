package brothers.scheucher.taskalender;

import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by michi on 31.05.2016.
 */
public class MyCalendarProvider {
    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    private static final String tag = "MyCalendarProvider";

    private static final String DEBUG_TAG = "MyActivity";
    public static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE          // 2
    };

    // The indices for the projection array above.
//    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_END_INDEX = 2;
    private static final int PROJECTION_TITLE_INDEX = 4;


    public static void googleCalendar() {
        // Run query
/*        Cursor cur = null;
        ContentResolver cr = TimeRank.getContext().getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";

        String[] selectionArgs = new String[] {"michischeucher@gmail.com", "com.google",
                "michischeucher@gmail.com"};
        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            Log.d(tag, "clID: " + calID + " displayName: " + displayName + " accountName: " + accountName + " ownderName: " + ownerName);


        }

*/


        //#############################################################

        // Specify the date range you want to search for recurring
        // event instances
        GregorianCalendar today = new GregorianCalendar();
        Util.setTime(today, 0, 0);
        long startMillis = today.getTimeInMillis();
        long endMillis = startMillis + 24 * 60 * 60 * 1000;

/*        Cursor cur = null;
        ContentResolver cr = TimeRank.getContext().getContentResolver();

        // The ID of the recurring event whose instances you are searching
        // for in the Instances table
        String selection = "";//CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {""};

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        while (cur.moveToNext()) {
            String title = null;
            long eventID = 0;
            long beginVal = 0;

            // Get the field values
            eventID = cur.getLong(PROJECTION_ID_INDEX);
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            title = cur.getString(PROJECTION_TITLE_INDEX);

            // Do something with the values.
            Log.i(DEBUG_TAG, "Event:  " + title);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            Log.i(DEBUG_TAG, "Date: " + formatter.format(calendar.getTime()));

            Log.d(tag, "begin: " + beginVal + " title = " + title);

        }
        */

    }

    public static ArrayList<MyEvent> getEvents(long startMillis, long endMillis) {
        String[] projection =
                new String[] {
                        CalendarContract.Instances._ID,
                        CalendarContract.Instances.EVENT_ID,
                        CalendarContract.Instances.BEGIN,
                        CalendarContract.Instances.END,
                        CalendarContract.Instances.TITLE };
        int PROJECTION_ID_INDEX = 1;
        int PROJECTION_BEGIN_INDEX = 2;
        int PROJECTION_END_INDEX = 3;
        int PROJECTION_TITLE_INDEX = 4;


        Cursor cursor =
                CalendarContract.Instances.query(TimeRank.getContext().getContentResolver(), projection, startMillis, endMillis);

        ArrayList<MyEvent> events = new ArrayList<>();

        while (cursor.moveToNext()) {
            long id = -1;
            String title = null;
            long beginVal = 0;
            long endVal = 0;

            // Get the field values
            id = cursor.getLong(PROJECTION_ID_INDEX);
            beginVal = cursor.getLong(PROJECTION_BEGIN_INDEX);
            endVal = cursor.getLong(PROJECTION_END_INDEX);
            title = cursor.getString(PROJECTION_TITLE_INDEX);

            Log.d(tag, "Title: " + title + " from: " + beginVal + " to " + endVal + " id: " + id);
            MyEvent new_event = new MyEvent(TimeRank.getNewEventID());
            new_event.setName(title);
            new_event.getStart().setTimeInMillis(beginVal);
            new_event.getEnd().setTimeInMillis(endVal);
            new_event.checkBlocking();
            new_event.setExternID(id);

            events.add(new_event);
        }

        return events;
    }

}
