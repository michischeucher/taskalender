package brothers.scheucher.taskalender;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by Michael on 08.07.2015.
 */
public class SQLiteStorageHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TimeRank.db";
    private static SQLiteStorageHelper instance;
    private static int version;
    private static final String tag = "SQLiteStorageHelper";

    private static SQLiteDatabase db = null;

    private static final String TASK_TABLE = "CREATE TABLE IF NOT EXISTS " + Task.DB_TABLE + " ( "
            + Task.DB_COL_ID + " int, "
            + Task.DB_COL_NAME + " text, "
            + Task.DB_COL_NOTICE + " text, "
            + Task.DB_COL_DURATION + " int, "
            + Task.DB_COL_DEADLINE + " text, "
            + Task.DB_COL_EARLIEST_START + " text, "
            + Task.DB_COL_PRIORITY + " text, "
            + Task.DB_COL_LABELS + " text ) ";

    private static final String EVENT_TABLE = "CREATE TABLE IF NOT EXISTS " + MyEvent.DB_TABLE + " ( "
            + MyEvent.DB_COL_ID + " int, "
            + MyEvent.DB_COL_NAME + " text, "
            + MyEvent.DB_COL_NOTICE + " text, "
            + MyEvent.DB_COL_START + " text, "
            + MyEvent.DB_COL_END + " text, "
            + MyEvent.DB_COL_TASK_ID + " int, "
            + MyEvent.DB_COL_PRIORITY + " text, "
            + MyEvent.DB_COL_AVAILABILITY + " int ) ";

    private static final String LABEL_TABLE = "CREATE TABLE IF NOT EXISTS " + Label.DB_TABLE + " ( "
            + Label.DB_COL_ID + " int, "
            + Label.DB_COL_NAME + " text, "
            + Label.DB_COL_COLOR + " int, "
            + Label.DB_COL_PARENT_ID + " int ) ";


    private static final String DROP_TASK_TABLE = "DROP TABLE IF EXISTS " + Task.DB_TABLE;
    private static final String DROP_EVENT_TABLE = "DROP TABLE IF EXISTS " + MyEvent.DB_TABLE;
    private static final String DROP_LABEL_TABLE = "DROP TABLE IF EXISTS " + Label.DB_TABLE;

    public boolean openDB() {
        db = this.getWritableDatabase();
        return true;
    }
    public boolean closeDB() {
        db.close();
        return true;
    }

    private SQLiteStorageHelper(Context context, int version){
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        Log.d(tag, "onCreate");
        try{
            if(db.isOpen()){
//                db.execSQL(DROP_TASK_TABLE);
                db.execSQL(TASK_TABLE);
//                db.execSQL(DROP_EVENT_TABLE);
                db.execSQL(EVENT_TABLE);
//                db.execSQL(DROP_LABEL_TABLE);
                db.execSQL(LABEL_TABLE);
                Log.d(tag, "db created");
            }

        }
        catch(Exception e){
            Log.d(tag, "EXCEPTION onCreateDB " + e.getMessage());
            return;
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // some magic happens with your mama
        db.execSQL(DROP_TASK_TABLE);
        onCreate(db);
        Log.d(tag, "db updated from version " + oldVersion + " to version " + newVersion);
    }

    //WRITING
    public boolean saveTask(Task task){
        //Log.d(tag, "saveTask");

        ContentValues values = new ContentValues();
        values.put(Task.DB_COL_NAME, task.getName());
        values.put(Task.DB_COL_NOTICE, task.getNotice());
        values.put(Task.DB_COL_DURATION, task.getRemaining_duration());
        values.put(Task.DB_COL_EARLIEST_START, Util.DateToString(task.getEarliestStart()));
        values.put(Task.DB_COL_DEADLINE, Util.DateToString(task.getDeadline()));
        Log.d(tag, "saved date = "+ Util.DateToString((task.getDeadline())));
        values.put(Task.DB_COL_PRIORITY, task.getPriority());
        values.put(Task.DB_COL_LABELS, task.getLabelIdsString());

        Cursor cursor = db.rawQuery("SELECT * from " + Task.DB_TABLE + " WHERE " +
                Task.DB_COL_ID + " = \"" + task.getId() + "\"", null);

        if (cursor.getCount() > 0) { //update
            Log.d(tag, "update: " + task.description());
            long result = db.update(Task.DB_TABLE, values, Task.DB_COL_ID + " = \"" + task.getId() + "\"", null);
            if (result < 1) {
                Log.d(tag, "saveTask update ERROR (result = " + result + ")");
                return false;
            }
        } else { //insert*/
            Log.d(tag, "insert:" + task.description());
            values.put(Task.DB_COL_ID, task.getId());
            long result = db.insert(Task.DB_TABLE, null, values);
            if (result == -1){
                Log.d(tag, "saveTask insertion ERROR (result = " + result + ")");
                return false;
            }
        }
        return true;
    }

    public boolean deleteTask(Task task) {
        //Log.d(tag, "deleteTask");
        int id = task.getId();
        Cursor cursor = db.rawQuery("SELECT * from " + Task.DB_TABLE + " WHERE " +
                Task.DB_COL_ID + " = \"" + id + "\"", null);

        if (cursor.getCount() > 0) {
            long result = db.delete(Task.DB_TABLE, Task.DB_COL_ID + " = \"" + id + "\"", null);
            if (result < 1) {
                Log.d(tag, "deleteTask: ERROR (result = " + result + ")");
                return false;
            } else {
                Log.d(tag, "deleteTask: finished with id = " + id);
            }
        } else {
            Log.d(tag, "deleteTask: cursor < 0 => not in database?! Nothing to delete... id = " + id);
        }
        return true;
    }


    public boolean saveEvent(MyEvent event) {
        ContentValues values = new ContentValues();
        values.put(MyEvent.DB_COL_NAME, event.getName());
        values.put(MyEvent.DB_COL_NOTICE, event.getNotice());
        values.put(MyEvent.DB_COL_START, Util.DateToString(event.getStart()));
        values.put(MyEvent.DB_COL_END, Util.DateToString(event.getEnd()));
        values.put(MyEvent.DB_COL_PRIORITY, event.getPriority());
        values.put(MyEvent.DB_COL_AVAILABILITY, event.getAvailability());
        if (event.getTask() != null) {
            values.put(MyEvent.DB_COL_TASK_ID, event.getTask().getId());
        } else {
            values.put(MyEvent.DB_COL_TASK_ID, -1);
        }

        Cursor cursor = db.rawQuery("SELECT * from " + MyEvent.DB_TABLE + " WHERE " +
                MyEvent.DB_COL_ID + " = \"" + event.getId() + "\"", null);

        if (cursor.getCount() > 0) { //update
            Log.d(tag, "update: " + event.description());
            long result = db.update(MyEvent.DB_TABLE, values, MyEvent.DB_COL_ID + " = \"" + event.getId() + "\"", null);
            if (result < 1) {
                Log.d(tag, "saveEvent update ERROR (result = " + result + ")");
                return false;
            }
        } else { //insert*/
            Log.d(tag, "insert:" + event.description());
            values.put(MyEvent.DB_COL_ID, event.getId());
            long result = db.insert(MyEvent.DB_TABLE, null, values);
            if (result == -1){
                Log.d(tag, "saveEvent insertion ERROR (result = " + result + ")");
                return false;
            }
        }
        return true;
    }

    public boolean deleteEvent(MyEvent event) {
        //Log.d(tag, "deleteEvent");
        int id = event.getId();
        Cursor cursor = db.rawQuery("SELECT * from " + MyEvent.DB_TABLE + " WHERE " +
                MyEvent.DB_COL_ID + " = \"" + id + "\"", null);

        if (cursor.getCount() > 0) {
            long result = db.delete(MyEvent.DB_TABLE, MyEvent.DB_COL_ID + " = \"" + id + "\"", null);
            if (result < 1) {
                Log.d(tag, "deleteEvent: ERROR (result = " + result + ")");
                return false;
            } else {
                Log.d(tag, "deleteEvent: finished with id = " + id);
            }
        } else {
            Log.d(tag, "deleteEvent: cursor < 0 => not in database?! Nothing to delete... id = " + id);
        }
        return true;
    }

    public boolean saveLabel(Label label) {
        Log.d(tag, "saveLabel - start label = " + label.description());
        ContentValues values = new ContentValues();
        values.put(Label.DB_COL_NAME, label.getName());
        values.put(Label.DB_COL_COLOR, label.getColor());
        values.put(Label.DB_COL_PARENT_ID, label.getParentID());

        Cursor cursor = db.rawQuery("SELECT * from " + Label.DB_TABLE + " WHERE " +
                Label.DB_COL_ID + " = \"" + label.getId() + "\"", null);

        if (cursor.getCount() > 0) { //update
            Log.d(tag, "update: " + label.description());
            long result = db.update(Label.DB_TABLE, values, Label.DB_COL_ID + " = \"" + label.getId() + "\"", null);
            if (result < 1) {
                Log.d(tag, "saveLabel update ERROR (result = " + result + ")");
                return false;
            }
        } else { //insert*/
            Log.d(tag, "insert:" + label.description());
            values.put(Label.DB_COL_ID, label.getId());
            long result = db.insert(Label.DB_TABLE, null, values);
            if (result == -1){
                Log.d(tag, "saveLabel insertion ERROR (result = " + result + ")");
                return false;
            }
        }
        return true;
    }

    public boolean deleteLabel(Label label) {
        //Log.d(tag, "deleteLabel");
        int id = label.getId();
        Cursor cursor = db.rawQuery("SELECT * from " + Label.DB_TABLE + " WHERE " +
                Label.DB_COL_ID + " = \"" + id + "\"", null);

        if (cursor.getCount() > 0) {
            long result = db.delete(Label.DB_TABLE, Label.DB_COL_ID + " = \"" + id + "\"", null);
            if (result < 1) {
                Log.d(tag, "deleteLabel: ERROR (result = " + result + ")");
                return false;
            } else {
                Log.d(tag, "deleteLabel: finished with id = " + id);
            }
        } else {
            Log.d(tag, "deleteLabel: cursor < 0 => not in database?! Nothing to delete... id = " + id);
        }
        return true;
    }

    public void addAllTasksFromDatabase() {
        Log.d(tag, "addAllTasksFromDatabase - start");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Task.DB_TABLE, null, null, null, null, null, null, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(Task.DB_COL_ID));
            Task task = new Task(id);

            task.setName(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_NAME)));
            task.setNotice(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_NOTICE)));
            task.setRemaining_duration(cursor.getInt(cursor.getColumnIndexOrThrow(Task.DB_COL_DURATION)));
            task.setEarliestStart(Util.StringToDate(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_EARLIEST_START))));
            task.setDeadline(Util.StringToDate(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_DEADLINE))));
            task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_PRIORITY)));
            task.setLabelString(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_LABELS)));

            TimeRank.addTaskToList(task);

        }
        cursor.close();
        db.close();
    }

    public void addAllEventsFromDatabase() {
        Log.d(tag, "addAllEventsFromDatabase - start");
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MyEvent.DB_TABLE, null, null, null, null, null, null, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_ID));
            MyEvent event = new MyEvent(id);
            event.setName(cursor.getString(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_NAME)));
            event.setNotice(cursor.getString(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_NOTICE)));
            event.setStart(Util.StringToDate(cursor.getString(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_START))));
            event.setEnd(Util.StringToDate(cursor.getString(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_END))));
            event.setTask(cursor.getInt(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_TASK_ID)));
            event.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_PRIORITY)));
            event.setAvailability(cursor.getInt(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_AVAILABILITY)));
            Log.d(tag, "MyEvent read: " + event.description());
            TimeRank.addEventToList(event);
        }
        cursor.close();
        db.close();
    }

    public void addAllLabelsFromDatabase() {
        Log.d(tag, "addAllLabelsFromDatabase - start");
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Label.DB_TABLE, null, null, null, null, null, null, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(Label.DB_COL_ID));
            Label label = new Label(id);
            label.setName(cursor.getString(cursor.getColumnIndexOrThrow(Label.DB_COL_NAME)));
            label.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(Label.DB_COL_COLOR)));
            label.setParent(cursor.getInt(cursor.getColumnIndexOrThrow(Label.DB_COL_PARENT_ID)));

            Log.d(tag, "Label read: " + label.description());
            TimeRank.addLabelToList(label);
        }
        cursor.close();
        db.close();
    }

    public static SQLiteStorageHelper getInstance(Context context, int vers){

        if(instance == null || vers != version){
            Log.d(tag, "instance == null || vers != version");
            instance = new SQLiteStorageHelper(context, vers);
            version = vers;
        }
        return instance;
    }

    public void resetTaskTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(DROP_TASK_TABLE);
        db.execSQL(TASK_TABLE);
        db.close();
    }

    public static boolean checkDatabase(Context context) {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }


    public void resetDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(DROP_TASK_TABLE);
        db.execSQL(TASK_TABLE);
        db.execSQL(DROP_EVENT_TABLE);
        db.execSQL(EVENT_TABLE);
        db.close();
    }

}


