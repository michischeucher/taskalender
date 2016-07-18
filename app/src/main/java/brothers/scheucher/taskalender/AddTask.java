package brothers.scheucher.taskalender;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class AddTask extends AppCompatActivity {
    private static final String tag = "AddTask";
    private Task task;
    private Context context;

    private TextView deadline_date_view;
    private TextView add_task_label;
    private TextView add_task_label_color;
    private boolean[] selected_labels;

    DurationPickerDialog duration_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        context = this;

        Bundle b = getIntent().getExtras();
        Log.d(tag, "Task == null");
        if (b != null) {
            int id = b.getInt("id");
            task = TimeRank.getTask(id);
            Log.d(tag, "fetched task");
        } else {
            task = new Task();
            Log.d(tag, "created new task");
            findViewById(R.id.add_task_title).requestFocus();
        }

        //GETTING VIEWS
        TextView duration_view = ((TextView) findViewById(R.id.add_task_duration));
        deadline_date_view = ((TextView)findViewById(R.id.add_task_deadline_date));
        TextView deadline_time_view = ((TextView) findViewById(R.id.add_task_deadline_time));
        LinearLayout add_task_label_container = ((LinearLayout) findViewById(R.id.add_task_label_container));
        add_task_label = ((TextView) add_task_label_container.findViewById(R.id.add_task_label));
        add_task_label_color = ((TextView) add_task_label_container.findViewById(R.id.add_task_label_color));
        Button delete_button = ((Button) findViewById(R.id.add_task_delete_button));


        duration_dialog = new DurationPickerDialog(AddTask.this, duration_view, task.getDuration());
        duration_dialog.setTitle("Verbleibende Dauer");

        //SETTING VIEWS
        ((TextView)findViewById(R.id.add_task_title)).setText(task.getName());
        ((TextView)findViewById(R.id.add_task_notice)).setText(task.getNotice());
        duration_view.setText(Util.getFormattedDuration(task.getRemaining_duration()));
        deadline_date_view.setText(Util.getFormattedDate(task.getDeadline()));
        deadline_time_view.setText(Util.getFormattedTime(task.getDeadline()));

        //Setting selected labels
        if (task.getLabelIds().size() > 0) {
            add_task_label.setText(task.getLabelString());
        } else {
            add_task_label.setText(R.string.choose_label_text);
        }
        if (task.getColor() != -1) {
            add_task_label_color.setBackgroundColor(0xFF000000 | task.getColor());
        } else {
            add_task_label_color.setBackgroundColor(0x00FFFFFF);
        }



        //SETTING LISTENERS
        duration_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDurationPicker();
            }
        });
        deadline_date_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(deadline_date_view, task.getDeadline());
            }
        });


        deadline_time_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(v);
            }
        });

        add_task_label_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "Click On Label Selection...");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final ArrayList<Label> label_sequence = TimeRank.getLabelSequence(null);
                selected_labels = new boolean[label_sequence.size()];
                ArrayList<String> label_sequence_strings = new ArrayList<>();
                int i = 0;
                final ArrayList<Integer> selected_item_index_list = new ArrayList<>();
                for (Label l : label_sequence) {
                    label_sequence_strings.add(l.getLabelStringWithHirarchie());
                    if (task.getLabelIds().contains(l.getId())) {
                        selected_item_index_list.add(i);
                        selected_labels[i++] = true;
                    } else {
                        selected_labels[i++] = false;
                    }
                }
                builder.setMultiChoiceItems(label_sequence_strings.toArray(new CharSequence[label_sequence_strings.size()])
                        , selected_labels, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selected_item_index_list.add(which);
                        } else {
                            selected_item_index_list.remove(Integer.valueOf(which));
                        }
                    }
                })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                Log.d(tag, "OK clicked");
                                ArrayList<Integer> selected_label_ids = new ArrayList<>();
                                for (int i : selected_item_index_list) {
                                    Log.d(tag, "selected = " + i);
                                    selected_label_ids.add(label_sequence.get(i).getId());
                                }
                                task.setLabelIds(selected_label_ids);
                                if (task.getLabelIds().size() == 0) {
                                    add_task_label.setText(R.string.choose_label);
                                    add_task_label_color.setBackgroundColor(0x00FFFFFF);

                                } else {
                                    int color = 0xFF000000 | task.getColor();
                                    add_task_label.setText(task.getLabelString());
                                    add_task_label_color.setBackgroundColor(color);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the duration_dialog
                                Log.d(tag, "cancelled");
                            }
                        });
                // Create the AlertDialog object and return it

                builder.show();
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.delete(TimeRank.getContext());
                TimeRank.deleteTaskFromList(task);
                TimeRank.createCalculatingJob();
                Calender.notifyChanges();
                finish();
            }
        });

    }

    private void showDurationPicker() {
        duration_dialog.show();
    }

    public void showDatePicker(TextView view, GregorianCalendar date) {
        //Log.d(tag, "showDatePicker - start");
        int year = date.get(GregorianCalendar.YEAR);
        int month = date.get(GregorianCalendar.MONTH);
        int day = date.get(GregorianCalendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new onDateSetListener(view, date), year, month, day);
        dialog.show();
    }

    public void showTimePicker(View view) {
        //Log.d(tag, "showTimePicker");
        GregorianCalendar deadline = task.getDeadline();
        int minute = deadline.get(GregorianCalendar.MINUTE);
        int hour = deadline.get(GregorianCalendar.HOUR_OF_DAY);
        TimePickerDialog dialog = new TimePickerDialog(this, new onTimeSetListener(), hour, minute, true);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_add_task, menu);
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_task) {
            View add_task_view = findViewById(R.id.add_task);
            task.setName((String.valueOf(((TextView) add_task_view.findViewById(R.id.add_task_title)).getText())));
            task.setNotice((String.valueOf(((TextView) add_task_view.findViewById(R.id.add_task_notice)).getText())));

            int duration = duration_dialog.getDuration();
            task.setRemaining_duration(duration);

            task.save(this);
            TimeRank.addTaskToList(task);
            TimeRank.createCalculatingJob();

            MyNotifications.createNotification(this);

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            checkCorrectValues();
        }
    }

    private void checkCorrectValues() {
        if (task.getEarliestStart().compareTo(task.getDeadline()) == 1) {
            deadline_date_view.setTextColor(0xFFFF0000);
        } else {
            deadline_date_view.setTextColor(0xFF000000);
        }
    }

    private class onTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//            Log.d(tag, "hour = " + hourOfDay + " minute = " + minute);
            task.setDeadline(hourOfDay, minute);
            Log.d(tag, "deadline is now = " + Util.getFormattedDateTime(task.getDeadline()));
            ((TextView)findViewById(R.id.add_task_deadline_time)).setText(Util.getFormattedTime(task.getDeadline()));
        }
    }
}
