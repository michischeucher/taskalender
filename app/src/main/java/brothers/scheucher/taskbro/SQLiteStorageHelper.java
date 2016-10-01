package brothers.scheucher.taskbro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

public class SQLiteStorageHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskBroContainer.db";
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
            + Task.DB_COL_DONE + " int, "
            + Task.DB_COL_REPEAT + " int, "
            + Task.DB_COL_INACTIVE + " int, "
            + Task.DB_COL_LABELS + " text ) ";

    private static final String EVENT_TABLE = "CREATE TABLE IF NOT EXISTS " + MyEvent.DB_TABLE + " ( "
            + MyEvent.DB_COL_ID + " int, "
            + MyEvent.DB_COL_NAME + " text, "
            + MyEvent.DB_COL_NOTICE + " text, "
            + MyEvent.DB_COL_START + " text, "
            + MyEvent.DB_COL_END + " text, "
            + MyEvent.DB_COL_TASK_ID + " int, "
            + MyEvent.DB_COL_PRIORITY + " text, "
            + MyEvent.DB_COL_INACTIVE + " int, "
            + MyEvent.DB_COL_AVAILABILITY + " int ) ";

    private static final String LABEL_TABLE = "CREATE TABLE IF NOT EXISTS " + Label.DB_TABLE + " ( "
            + Label.DB_COL_ID + " int, "
            + Label.DB_COL_NAME + " text, "
            + Label.DB_COL_COLOR + " int, "
            + Label.DB_COL_PARENT_ID + " int ) ";
    private static final String DAYSETTING_TABLE = "CREATE TABLE IF NOT EXISTS " + DaySettingObject.DB_TABLE + " ( "
            + DaySettingObject.DB_COL_ID + " int, "
            + DaySettingObject.DB_COL_TOTALDURATION + " int, "
            + DaySettingObject.DB_COL_EARLIESTMINUTE + " int, "
            + DaySettingObject.DB_COL_LATESTMINUTE + " int, "
            + DaySettingObject.DB_COL_WORKING_DAYS + " text ) ";

    private static final String DROP_TASK_TABLE = "DROP TABLE IF EXISTS " + Task.DB_TABLE;
    private static final String DROP_EVENT_TABLE = "DROP TABLE IF EXISTS " + MyEvent.DB_TABLE;
    private static final String DROP_LABEL_TABLE = "DROP TABLE IF EXISTS " + Label.DB_TABLE;
    private static final String DROP_DAYSETTING_TABLE = "DROP TABLE IF EXISTS " + DaySettingObject.DB_TABLE;

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
        try{
            if(db.isOpen()){
                db.execSQL(TASK_TABLE);
                db.execSQL(EVENT_TABLE);
                db.execSQL(LABEL_TABLE);
                db.execSQL(DAYSETTING_TABLE);
            }

        }
        catch(Exception e){
            return;
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // some magic happens with your mama
        db.execSQL(DROP_TASK_TABLE);
        db.execSQL(DROP_EVENT_TABLE);
        db.execSQL(DROP_LABEL_TABLE);
        db.execSQL(DROP_DAYSETTING_TABLE);
        onCreate(db);
    }

    //WRITING
    public boolean saveTask(Task task){

        ContentValues values = new ContentValues();
        values.put(Task.DB_COL_NAME, task.getName());
        values.put(Task.DB_COL_NOTICE, task.getNotice());
        values.put(Task.DB_COL_DURATION, task.getRemaining_duration());
        values.put(Task.DB_COL_DEADLINE, Util.DateToString(task.getDeadline()));
        values.put(Task.DB_COL_DONE, task.getDone());
        values.put(Task.DB_COL_LABELS, task.getLabelIdsString());
        values.put(Task.DB_COL_REPEAT, task.getRepeat());
        values.put(Task.DB_COL_INACTIVE, task.getInactive());

        Cursor cursor = db.rawQuery("SELECT * from " + Task.DB_TABLE + " WHERE " +
                Task.DB_COL_ID + " = \"" + task.getId() + "\"", null);

        if (cursor.getCount() > 0) { //update
            long result = db.update(Task.DB_TABLE, values, Task.DB_COL_ID + " = \"" + task.getId() + "\"", null);
            if (result < 1) {
                return false;
            }
        } else { //insert*/
            values.put(Task.DB_COL_ID, task.getId());
            long result = db.insert(Task.DB_TABLE, null, values);
            if (result == -1){
                return false;
            }
        }
        return true;
    }

    public boolean deleteTask(Task task) {
        int id = task.getId();
        Cursor cursor = db.rawQuery("SELECT * from " + Task.DB_TABLE + " WHERE " +
                Task.DB_COL_ID + " = \"" + id + "\"", null);

        if (cursor.getCount() > 0) {
            long result = db.delete(Task.DB_TABLE, Task.DB_COL_ID + " = \"" + id + "\"", null);
            if (result < 1) {
                return false;
            }
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
        values.put(MyEvent.DB_COL_INACTIVE, event.getInactive());
        if (event.getTask() != null) {
            values.put(MyEvent.DB_COL_TASK_ID, event.getTask().getId());
        } else {
            values.put(MyEvent.DB_COL_TASK_ID, -1);
        }

        Cursor cursor = db.rawQuery("SELECT * from " + MyEvent.DB_TABLE + " WHERE " +
                MyEvent.DB_COL_ID + " = \"" + event.getId() + "\"", null);

        if (cursor.getCount() > 0) { //update
            long result = db.update(MyEvent.DB_TABLE, values, MyEvent.DB_COL_ID + " = \"" + event.getId() + "\"", null);
            if (result < 1) {
                return false;
            }
        } else { //insert*/
            values.put(MyEvent.DB_COL_ID, event.getId());
            long result = db.insert(MyEvent.DB_TABLE, null, values);
            if (result == -1){
                return false;
            }
        }
        return true;
    }

    public boolean deleteEvent(MyEvent event) {
        int id = event.getId();
        Cursor cursor = db.rawQuery("SELECT * from " + MyEvent.DB_TABLE + " WHERE " +
                MyEvent.DB_COL_ID + " = \"" + id + "\"", null);

        if (cursor.getCount() > 0) {
            long result = db.delete(MyEvent.DB_TABLE, MyEvent.DB_COL_ID + " = \"" + id + "\"", null);
            if (result < 1) {
                return false;
            }
        }
        return true;
    }

    public boolean saveLabel(Label label) {
        ContentValues values = new ContentValues();
        values.put(Label.DB_COL_NAME, label.getName());
        values.put(Label.DB_COL_COLOR, label.getColor());
        values.put(Label.DB_COL_PARENT_ID, label.getParentID());

        Cursor cursor = db.rawQuery("SELECT * from " + Label.DB_TABLE + " WHERE " +
                Label.DB_COL_ID + " = \"" + label.getId() + "\"", null);

        if (cursor.getCount() > 0) { //update
            long result = db.update(Label.DB_TABLE, values, Label.DB_COL_ID + " = \"" + label.getId() + "\"", null);
            if (result < 1) {
                return false;
            }
        } else { //insert*/
            values.put(Label.DB_COL_ID, label.getId());
            long result = db.insert(Label.DB_TABLE, null, values);
            if (result == -1){
                return false;
            }
        }
        return true;
    }

    public boolean deleteLabel(Label label) {
        int id = label.getId();
        Cursor cursor = db.rawQuery("SELECT * from " + Label.DB_TABLE + " WHERE " +
                Label.DB_COL_ID + " = \"" + id + "\"", null);

        if (cursor.getCount() > 0) {
            long result = db.delete(Label.DB_TABLE, Label.DB_COL_ID + " = \"" + id + "\"", null);
            if (result < 1) {
                return false;
            }
        }
        return true;
    }

    public boolean saveDaySettingObject(DaySettingObject dso) {
        ContentValues values = new ContentValues();
        values.put(DaySettingObject.DB_COL_TOTALDURATION, dso.getTotalDurationInMinutes());
        values.put(DaySettingObject.DB_COL_EARLIESTMINUTE, dso.getEarliest_minute());
        values.put(DaySettingObject.DB_COL_LATESTMINUTE, dso.getLatest_minute());
        values.put(DaySettingObject.DB_COL_WORKING_DAYS, dso.getWorkingDaysIdsString());

        Cursor cursor = db.rawQuery("SELECT * from " + DaySettingObject.DB_TABLE + " WHERE " +
                DaySettingObject.DB_COL_ID + " = \"" + dso.getId() + "\"", null);

        if (cursor.getCount() > 0) { //update
            long result = db.update(DaySettingObject.DB_TABLE, values, DaySettingObject.DB_COL_ID + " = \"" + dso.getId() + "\"", null);
            if (result < 1) {
                return false;
            }
        } else { //insert*/
            values.put(DaySettingObject.DB_COL_ID, dso.getId());
            long result = db.insert(DaySettingObject.DB_TABLE, null, values);
            if (result == -1){
                return false;
            }
        }
        return true;
    }

    public void addAllTasksFromDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Task.DB_TABLE, null, null, null, null, null, null, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(Task.DB_COL_ID));
            Task task = new Task(id);

            task.setName(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_NAME)));
            task.setNotice(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_NOTICE)));
            task.setRemaining_duration(cursor.getInt(cursor.getColumnIndexOrThrow(Task.DB_COL_DURATION)));
            task.setDeadline(Util.StringToDate(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_DEADLINE))));
            task.setLabelString(cursor.getString(cursor.getColumnIndexOrThrow(Task.DB_COL_LABELS)));
            task.setDone(cursor.getInt(cursor.getColumnIndexOrThrow(Task.DB_COL_DONE)));
            task.setRepeat(cursor.getInt(cursor.getColumnIndexOrThrow(Task.DB_COL_REPEAT)));
            task.setInactive(cursor.getInt(cursor.getColumnIndexOrThrow(Task.DB_COL_INACTIVE)));
            TaskBroContainer.addTaskToList(task);
        }
        cursor.close();
        db.close();
    }

    public void addAllEventsFromDatabase() {
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
            event.setInactive(cursor.getInt(cursor.getColumnIndexOrThrow(MyEvent.DB_COL_INACTIVE)));
            TaskBroContainer.addEventToList(event);
        }
        cursor.close();
        db.close();
    }

    public void addAllLabelsFromDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Label.DB_TABLE, null, null, null, null, null, null, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(Label.DB_COL_ID));
            if (id == -1) {
                Log.e(tag, "ä##### Error: id = " + id + " on label ");
            }
            Label label = new Label(id);
            label.setName(cursor.getString(cursor.getColumnIndexOrThrow(Label.DB_COL_NAME)));
            label.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(Label.DB_COL_COLOR)));
            label.setParent(cursor.getInt(cursor.getColumnIndexOrThrow(Label.DB_COL_PARENT_ID)));

            TaskBroContainer.addLabelToList(label);
        }
        cursor.close();
        db.close();
    }
    public void addAllDaySettingObjectsFromDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DaySettingObject.DB_TABLE, null, null, null, null, null, null, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DaySettingObject.DB_COL_ID));
            DaySettingObject dso = new DaySettingObject(id);
            dso.setTotalDuration(cursor.getInt(cursor.getColumnIndexOrThrow(DaySettingObject.DB_COL_TOTALDURATION)));
            dso.setEarliest_minute(cursor.getInt(cursor.getColumnIndexOrThrow(DaySettingObject.DB_COL_EARLIESTMINUTE)));
            dso.setLatest_minute(cursor.getInt(cursor.getColumnIndexOrThrow(DaySettingObject.DB_COL_LATESTMINUTE)));
            dso.setWorkingDaysString(cursor.getString(cursor.getColumnIndexOrThrow(DaySettingObject.DB_COL_WORKING_DAYS)));

            TaskBroContainer.addDaySettingObject(dso);
        }
        cursor.close();
        db.close();
    }

    public static SQLiteStorageHelper getInstance(Context context, int vers){

        if(instance == null || vers != version){
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


