package brothers.scheucher.taskbro;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.GregorianCalendar;

public class WorkFinishedDialog extends Dialog {

    private static final String tag = "WorkFinishedDialog";
    private final Activity activity;

    private Task task;
    private MyEvent event;
    private Duration worked_duration;
    private Duration remaining_duration;
    private boolean remaining_duration_modified = false;

    private RelativeLayout work_finished_dialog;
    private TextView start_time;
    private TextView end_time;
    private TextView date_view;
    private TextView worked_duration_view;
    protected TextView remaining_duration_view;

    private String last_changed;
    private String before_last_change;

    private static final String START_TIME = "start_date";
    private static final String END_TIME = "end_time";
    private static final String DURATION = "duration";
    public boolean was_ok = false;

    public WorkFinishedDialog(Activity a, Task task, int duration) {
        super(a);
        this.activity = a;
        this.task = task;
        this.remaining_duration = new Duration(task.getRemaining_duration());
        this.remaining_duration.addMinutes(-duration);
        this.event = new MyEvent(duration, true);
        this.worked_duration = new Duration(duration);
        this.event.setTask(task);

        last_changed = DURATION;
        before_last_change = END_TIME;
        was_ok = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.work_finished_dialog);
        setTitle("Einheit eintragen");

        work_finished_dialog = (RelativeLayout)findViewById(R.id.work_finished_dialog);
        date_view = (TextView)work_finished_dialog.findViewById(R.id.date);
        date_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker((TextView) v, event.getStart());
            }
        });
        start_time = (TextView)work_finished_dialog.findViewById(R.id.start_time);
        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(start_time, event.getStart(), true);
            }
        });
        end_time = (TextView)work_finished_dialog.findViewById(R.id.end_time);
        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(end_time, event.getEnd(), false);
            }
        });
        worked_duration_view = (TextView)work_finished_dialog.findViewById(R.id.duration);
        worked_duration_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "TODO: muss zusammenkalkuliet werden");
                DurationPickerDialog dialog = new DurationPickerDialog(activity, worked_duration_view, worked_duration);
                dialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (last_changed.equals(START_TIME) || (last_changed.equals(DURATION) && before_last_change.equals(START_TIME))) {
                            event.setEndWithDuration(worked_duration.getDuration());
                        } else if (last_changed.equals(END_TIME) || (last_changed.equals(DURATION) && before_last_change.equals(END_TIME))) {
                            Util.setTime(event.getStart(), event.getEnd());
                            event.getStart().add(GregorianCalendar.MINUTE, -worked_duration.getDuration());
                        }

                        if (!last_changed.equals(DURATION)) {
                            before_last_change = last_changed;
                        }
                        last_changed = DURATION;

                        if (!remaining_duration_modified) {
                            remaining_duration.setDuration(task.getRemaining_duration() - worked_duration.getDuration());
                        }

                        setFieldsBecauseOfData();
                    }
                });
                dialog.show();
            }
        });
        remaining_duration_view = (TextView)work_finished_dialog.findViewById(R.id.remaining_duration);
        remaining_duration_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DurationPickerDialog dialog = new DurationPickerDialog(activity, remaining_duration_view, remaining_duration);
                dialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        remaining_duration_modified = true;
                    }
                });
                dialog.show();
            }
        });

        ((Button)findViewById(R.id.negative_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "abbrechen clicked");
                cancel();
            }
        });
        ((Button)findViewById(R.id.positive_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "ok clicked, save work finished dialog");
                task.setRemaining_duration(remaining_duration.getDuration());
                event.setName(task.getName());
                event.setNotice("Eingetragene Arbeitseinheit");
                event.setEndWithDuration(worked_duration.getDuration());
                event.save(TaskBroContainer.getContext());
                TaskBroContainer.addEventToList(event);
                TaskBroContainer.createCalculatingJob(activity);
                was_ok = true;
                dismiss();
            }
        });

        setFieldsBecauseOfData();

    }

    private void setFieldsBecauseOfData() {
        date_view.setText(Util.getFormattedDate(event.getStart()));
        start_time.setText(Util.getFormattedTime(event.getStart()));
        end_time.setText(Util.getFormattedTime(event.getEnd()));
        worked_duration_view.setText(Util.getFormattedDuration(worked_duration.getDuration()));

        remaining_duration_view.setText(Util.getFormattedDuration(remaining_duration.getDuration()));
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        Log.d(tag, "ondismiss");
        super.setOnDismissListener(listener);
    }

    @Override
    public void setOnCancelListener(OnCancelListener listener) {
        Log.d(tag, "oncancel");
        super.setOnCancelListener(listener);
    }

    @Override
    protected void onStart() {
        super.onStop();
    }

    public void showDatePicker(TextView view, GregorianCalendar date) {
        //Log.d(tag, "showDatePicker - start");
        int year = date.get(GregorianCalendar.YEAR);
        int month = date.get(GregorianCalendar.MONTH);
        int day = date.get(GregorianCalendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(), new onDateSetListener(view, date), year, month, day);
        dialog.show();
    }

    public void setStart(GregorianCalendar start) {
        this.event.setStart(start);
    }
    public void setEnd(GregorianCalendar end) {
        this.event.setEnd(end);
    }

    private class onDateSetListener implements DatePickerDialog.OnDateSetListener {
        private TextView view;
        private GregorianCalendar date;

        public onDateSetListener(TextView view, GregorianCalendar date) {
            this.view = view;
            this.date = date;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            Log.d(tag, "year = " + year + " month = " + monthOfYear + " day = " + dayOfMonth);
            Util.setDate(this.date, year, monthOfYear, dayOfMonth);
            this.view.setText(Util.getFormattedDate(this.date));
        }
    }

    public void showTimePicker(View view, GregorianCalendar time, final boolean is_start_time) {
        //Log.d(tag, "showTimePicker");
        int minute = time.get(GregorianCalendar.MINUTE);
        int hour = time.get(GregorianCalendar.HOUR_OF_DAY);
        TimePickerDialog dialog = new TimePickerDialog(getContext(), new onTimeSetListener((TextView)view, time), hour, minute, true);
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (is_start_time && (last_changed.equals(END_TIME) || last_changed.equals(START_TIME) && before_last_change.equals(END_TIME) ) ||
                        !is_start_time && (last_changed.equals(START_TIME) || last_changed.equals(END_TIME) && before_last_change.equals(START_TIME)) ) {
                    if (event.getEndMinute() > event.getStartMinute()) {
                        worked_duration.setDuration(event.getEndMinute() - event.getStartMinute());
                    } else {
                        worked_duration.setDuration(24 * 60 - (event.getStartMinute() - event.getEndMinute()));
                    }
                } else {
                    if (is_start_time && last_changed.equals(DURATION)) {
                        event.setEndWithDuration(worked_duration.getDuration());
                    } else if (!is_start_time && last_changed.equals(DURATION)) {
                        event.setStart((GregorianCalendar)event.getEnd().clone());
                        event.getStart().add(GregorianCalendar.MINUTE, -worked_duration.getDuration());
                    } else {
                        Log.d(tag, "ERROR: NOT that case implemented... is_start_time = " + is_start_time + " last_changed = " + last_changed + " before_last_changed = " + before_last_change);
                    }
                }

                if (is_start_time) {
                    if (!last_changed.equals(START_TIME)) {
                        before_last_change = last_changed;
                    }
                    last_changed = START_TIME;
                } else {
                    if (!last_changed.equals(END_TIME)) {
                        before_last_change = last_changed;
                    }
                    last_changed = END_TIME;
                }

                if (!remaining_duration_modified) {
                    remaining_duration.setDuration(task.getRemaining_duration() - worked_duration.getDuration());
                }

                setFieldsBecauseOfData();
            }
        });
        dialog.show();
    }

    private class onTimeSetListener implements TimePickerDialog.OnTimeSetListener {
        private TextView text_view;
        private GregorianCalendar time;

        public onTimeSetListener(TextView text_view, GregorianCalendar time) {
            this.text_view = text_view;
            this.time = time;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//            Log.d(tag, "hour = " + hourOfDay + " minute = " + minute);
            Util.setTime(time, hourOfDay, minute);
            Log.d(tag, "time is set to = " + Util.getFormattedDateTime(time));
            text_view.setText(Util.getFormattedTime(time));
        }
    }
}