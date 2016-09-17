package brothers.scheucher.taskbro;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.GregorianCalendar;

public class AddEvent extends AppCompatActivity {
    private static final String tag = "AddEvent";
    private MyEvent event;

    private boolean is_start;

    private TextView event_start_date_view;
    private TextView event_start_time_view;
    private TextView event_end_date_view;
    private TextView event_end_time_view;
    private TextView title_view;
    private TextView notice_view;
    private Switch blocking_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Bundle b = getIntent().getExtras();
        int current_date_offset = 0;
        if (b != null) {
            int id = b.getInt("id");
            event = TaskBroContainer.getEvent(id);
            current_date_offset = b.getInt("current_date_offset");
        }
        if (event == null) {
            event = new MyEvent();
            event.getStart().add(GregorianCalendar.DAY_OF_YEAR, current_date_offset);
            event.getEnd().add(GregorianCalendar.DAY_OF_YEAR, current_date_offset);
            findViewById(R.id.add_event_title).requestFocus();
        }

        Log.d(tag, "AddEvent: " + event.description());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        event_start_date_view = ((TextView)findViewById(R.id.add_event_start_date));
        event_start_time_view = ((TextView)findViewById(R.id.add_event_start_time));
        event_end_date_view = ((TextView)findViewById(R.id.add_event_end_date));
        event_end_time_view = ((TextView)findViewById(R.id.add_event_end_time));
        Button delete_button = ((Button) findViewById(R.id.add_event_delete_button));
        title_view = ((TextView)findViewById(R.id.add_event_title));
        notice_view = ((TextView)findViewById(R.id.add_event_notice));
        blocking_switch = (Switch)findViewById(R.id.blocking_switch);
        blocking_switch.setChecked(event.isBlocking());

        updateDateAndTimeFields();
        title_view.setText(event.getName());
        notice_view.setText(event.getNotice());



        event_start_date_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(true);
            }
        });
        event_start_time_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(true);
            }
        });
        event_end_date_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(false);
            }
        });
        event_end_time_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(false);
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //event.delete(TaskBroContainer.getContext());
                //TaskBroContainer.deleteEventFromList(event);
                event.setInactive(true);
                event.save(TaskBroContainer.getContext());
                TaskBroContainer.createCalculatingJob();
                finish();
            }
        });
    }

    public void showDatePicker(boolean start_bool) {
        //Log.d(tag, "showDatePicker");
        int year;
        int month;
        int day;
        if (start_bool) {
            year = event.getStart().get(GregorianCalendar.YEAR);
            month = event.getStart().get(GregorianCalendar.MONTH);
            day = event.getStart().get(GregorianCalendar.DAY_OF_MONTH);
        } else {
            year = event.getEnd().get(GregorianCalendar.YEAR);
            month = event.getEnd().get(GregorianCalendar.MONTH);
            day = event.getEnd().get(GregorianCalendar.DAY_OF_MONTH);
        }

        is_start = start_bool;
        DatePickerDialog dialog = new DatePickerDialog(this, new onDateSetListener(), year, month, day);
        dialog.show();
    }

    public void showTimePicker(boolean start_bool) {
        //Log.d(tag, "showTimePicker");
        int minute;
        int hour;
        if (start_bool) {
            minute = event.getStart().get(GregorianCalendar.MINUTE);
            hour = event.getStart().get(GregorianCalendar.HOUR_OF_DAY);
        } else {
            minute = event.getEnd().get(GregorianCalendar.MINUTE);
            hour = event.getEnd().get(GregorianCalendar.HOUR_OF_DAY);
        }

        is_start = start_bool;
        TimePickerDialog dialog = new TimePickerDialog(this, new onTimeSetListener(), hour, minute, true);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            event.setName(String.valueOf(title_view.getText()));
            event.setNotice(String.valueOf(notice_view.getText()));
            event.setAvailability(!blocking_switch.isChecked());
            //Log.d(tag, "going to save event = " + event.description());
            event.save(this);
            TaskBroContainer.addEventToList(event);
            TaskBroContainer.createCalculatingJob();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class onDateSetListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            //Log.d(tag, "year = " + year + " month = " + monthOfYear + " day = " + dayOfMonth);

            if (is_start) {
                long old_millis = event.getStart().getTimeInMillis();
                event.setStart(year, monthOfYear, dayOfMonth);
                long new_millis = event.getStart().getTimeInMillis();
                event.getEnd().add(GregorianCalendar.DAY_OF_YEAR, (int)((new_millis - old_millis) / 1000 / 60 / 60 / 24));
                if (event.getStart().compareTo(event.getEnd()) == 1) {
                    event.setEnd(year, monthOfYear, dayOfMonth);
                }
            } else {
                event.setEnd(year, monthOfYear, dayOfMonth);
                if (event.getEnd().compareTo(event.getStart()) == -1) {
                    event.setStart(year, monthOfYear, dayOfMonth);
                }
            }
            updateDateAndTimeFields();
        }
    }

    private void updateDateAndTimeFields() {
        event_start_time_view.setText(Util.getFormattedTime(event.getStart()));
        event_start_date_view.setText(Util.getFormattedDate(event.getStart()));
        event_end_time_view.setText(Util.getFormattedTime(event.getEnd()));
        event_end_date_view.setText(Util.getFormattedDate(event.getEnd()));
    }

    private class onTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            //Log.d(tag, "hour = " + hourOfDay + " minute = " + minute);

            if (is_start) {
                int old_minute_of_day = Util.getMinuteOfDay(event.getStart());
                event.setStart(hourOfDay, minute);
                int new_minute_of_day = Util.getMinuteOfDay(event.getStart());
                event.getEnd().add(GregorianCalendar.MINUTE, new_minute_of_day - old_minute_of_day);
                if (event.getStart().compareTo(event.getEnd()) == 1) {
                    event.setEnd(hourOfDay, minute);
                }
            } else {
                event.setEnd(hourOfDay, minute);
                if (event.getEnd().compareTo(event.getStart()) == -1) {
                    event.setStart(hourOfDay, minute);
                }
            }
            updateDateAndTimeFields();
        }
    }
}
