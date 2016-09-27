package brothers.scheucher.taskbro;


import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.GregorianCalendar;

public class UserSettingActivity extends ActionBarActivity {
    private static final String tag = "UserSettingActivity";
    private Context context;
    private Activity activity;
    private GregorianCalendar deadline_time;
    private TextView duration_task_view;
    private TextView deadline_time_view;
    private Duration duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        this.context = this;
        this.activity = this;

        duration_task_view = (TextView) findViewById(R.id.standard_duration_task);
        deadline_time_view = (TextView) findViewById(R.id.standard_deadline_time);


        //DURATION TASK
        duration = Settings.getStandardDurationTask();
        duration_task_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DurationPickerDialog dpd = new DurationPickerDialog(activity, duration_task_view, duration);
                dpd.setTitle("Aufgabendauer");
                dpd.show();
            }
        });

        //DEADLINE TIME
        deadline_time = new GregorianCalendar();
        Util.setTime(deadline_time, UserSettings.getDeadlineHourIfDateSet(), UserSettings.getDeadlineMinuteIfDateSet());
        final onDeadlineTimeSetListener ots_deadline_time = new onDeadlineTimeSetListener(deadline_time_view, deadline_time);

        deadline_time_view.setOnClickListener(new View.OnClickListener() {
            private TimePickerDialog tpd;

            @Override
            public void onClick(View v) {
                tpd = new TimePickerDialog(context, ots_deadline_time, Util.getHour(deadline_time), Util.getMinute(deadline_time), true);
                tpd.show();
            }
        });

        fillFieldsBecauseOfData();

    }

    private void fillFieldsBecauseOfData() {
        this.deadline_time_view.setText(Util.getFormattedTime(deadline_time));
        this.duration_task_view.setText(Util.getFormattedDuration(duration.getDuration()));
    }

    private class onDeadlineTimeSetListener implements TimePickerDialog.OnTimeSetListener {
        private TextView text_view;
        private GregorianCalendar time;

        public onDeadlineTimeSetListener(TextView view, GregorianCalendar time) {
            this.text_view = view;
            this.time = time;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Util.setTime(time, hourOfDay, minute);
            this.text_view.setText(Util.getFormattedTime(time));
            Log.d(tag, "onDealineTimeSetListener: time set to: " + Util.getFormattedTime(time));
            UserSettings.setStandardDeadlineHourIfDateSet(hourOfDay);
            UserSettings.setStandardDeadlineMinuteIfDateSet(minute);
        }
    }

    @Override
    protected void onPause() {
        UserSettings.saveStandardDurationTask();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
