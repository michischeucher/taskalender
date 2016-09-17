package brothers.scheucher.taskbro;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class AddTask extends AppCompatActivity {
    private static final String tag = "AddTask";
    public static final int MONTLY_REPEAT_MINUTES = 43830;
    private Task task;
    private Context context;

    private TextView deadline_date_view;
    private TextView add_task_label;
    private TextView add_task_label_color;
    private boolean[] selected_labels;

    private int selected_item_repeat = 4;

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
            int parent_label_id = b.getInt("parent_label_id");
            if (id == -1) {
                task = new Task();
                task.addLabelId(parent_label_id);
                findViewById(R.id.add_task_title).requestFocus();
            } else {
                task = TaskBroContainer.getTask(id);
            }
        } else {
            task = new Task();
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
        final TextView add_task_repeat = (TextView)findViewById(R.id.add_task_repeat);


        duration_dialog = new DurationPickerDialog(AddTask.this, duration_view, task.getDuration());
        duration_dialog.setTitle("Verbleibende Dauer");

        //SETTING VIEWS
        ((TextView)findViewById(R.id.add_task_title)).setText(task.getName());
        ((TextView)findViewById(R.id.add_task_notice)).setText(task.getNotice());
        duration_view.setText(Util.getFormattedDuration(task.getRemaining_duration()));
        deadline_date_view.setText(Util.getFormattedDate(task.getDeadline()));
        deadline_time_view.setText(Util.getFormattedTime(task.getDeadline()));

        if (task.getRepeat() == 24 * 60) {
            add_task_repeat.setText("Täglich");
            selected_item_repeat = 0;
        } else if (task.getRepeat() == 7 * 24 * 60) {
            add_task_repeat.setText("Wöchentlich");
            selected_item_repeat = 1;
        } else if (task.getRepeat() == MONTLY_REPEAT_MINUTES) {
            add_task_repeat.setText("Monatlich");
            selected_item_repeat = 2;
        } else if (task.getRepeat() == 0) {
            add_task_repeat.setText("Keine Wiederholung");
            selected_item_repeat = 4;
        } else {
            add_task_repeat.setText(Util.getFormattedRepeat(task.getRepeat()));
            selected_item_repeat = 3;
        }

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
                showDatePicker(v);
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
                final ArrayList<Label> label_sequence = TaskBroContainer.getLabelSequence(null);
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

        add_task_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                ArrayList<String> reapeat_strings = new ArrayList<>();
                reapeat_strings.add("Täglich");
                reapeat_strings.add("Wöchentlich");
                reapeat_strings.add("Monatlich");
                reapeat_strings.add("Benutzerdefiniert");
                reapeat_strings.add("Keine Wiederholung");

                builder.setSingleChoiceItems(reapeat_strings.toArray(new CharSequence[reapeat_strings.size()])
                        , selected_item_repeat, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item_repeat = which;
                    }
                })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(tag, "selected = " + selected_item_repeat + " id = " + id);
                                if (selected_item_repeat == 0) {
                                    //täglich
                                    add_task_repeat.setText("Täglich");
                                    task.setRepeat(24*60);
                                } else if (selected_item_repeat == 1) {
                                    //wöchentlich
                                    add_task_repeat.setText("Wöchentlich");
                                    task.setRepeat(24*7*60);
                                } else if (selected_item_repeat == 2) {
                                    //monatlich
                                    add_task_repeat.setText("Monatlich");
                                    task.setRepeat(MONTLY_REPEAT_MINUTES);
                                } else if (selected_item_repeat == 3) {
                                    //benutzerdefiniert
                                    add_task_repeat.setText("Alle 2 Tage");
                                    task.setRepeat(24*2*60);
                                    Log.d(tag, "Noch nicht implementiert, benutzerdefiniert!");
                                    Toast toast = Toast.makeText(TaskBroContainer.getContext(), "Noch nicht implementiert", Toast.LENGTH_SHORT);
                                    toast.show();
                                } else if (selected_item_repeat == 4) {
                                    task.setRepeat(0);
                                    add_task_repeat.setText("Keine Wiederholung");
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
                task.delete(TaskBroContainer.getContext());
                TaskBroContainer.deleteTaskFromList(task);
                TaskBroContainer.createCalculatingJob();
                Calender.notifyChanges();
                finish();
            }
        });

    }

    private void showDurationPicker() {

        duration_dialog.show();
    }

    public void showDatePicker(View view) {
        //Log.d(tag, "showDatePicker - start");
        GregorianCalendar deadline = task.getDeadline();
        if (deadline == null) {
            deadline = new GregorianCalendar();
        }
        int year = deadline.get(GregorianCalendar.YEAR);
        int month = deadline.get(GregorianCalendar.MONTH);
        int day = deadline.get(GregorianCalendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new onDateSetListener(), year, month, day);
        dialog.show();
    }

    public void showTimePicker(View view) {
        //Log.d(tag, "showTimePicker");
        GregorianCalendar deadline = task.getDeadline();
        if (deadline == null) {
            deadline = new GregorianCalendar();
            deadline.set(GregorianCalendar.MINUTE, 0);
            deadline.add(GregorianCalendar.HOUR_OF_DAY, 1);
        }
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
            TaskBroContainer.addTaskToList(task);
            TaskBroContainer.createCalculatingJob();

//            MyNotifications.setAlarmNotification(this);
//            MyNotifications.createNotification(this);

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class onDateSetListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            Log.d(tag, "year = " + year + " month = " + monthOfYear + " day = " + dayOfMonth);
            task.setDeadline(year,monthOfYear,dayOfMonth);
            //Util.setDate(this.date, year, monthOfYear, dayOfMonth);
            ((TextView)findViewById(R.id.add_task_deadline_time)).setText(Util.getFormattedTime(task.getDeadline()));
            ((TextView)findViewById(R.id.add_task_deadline_date)).setText(Util.getFormattedDate(task.getDeadline()));
        }
    }


    private class onTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//            Log.d(tag, "hour = " + hourOfDay + " minute = " + minute);
            task.setDeadline(hourOfDay, minute);
            Log.d(tag, "deadline is now = " + Util.getFormattedDateTime(task.getDeadline()));
            ((TextView)findViewById(R.id.add_task_deadline_time)).setText(Util.getFormattedTime(task.getDeadline()));
            ((TextView)findViewById(R.id.add_task_deadline_date)).setText(Util.getFormattedDate(task.getDeadline()));

        }
    }
}
