package brothers.scheucher.taskalender;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.GregorianCalendar;

public class ViewTask extends ActionBarActivity {

    private static final String tag = "ViewTask";
    private Task task;
    private int id;
    private Context context;
    private Activity activity;
    private RelativeLayout view_task;
    private TextView task_name;
    private LayoutInflater inflater;
    private LinearLayout view_task_fields_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);
        context = this;
        activity = this;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            id = b.getInt("id");
            task = TimeRank.getTask(id);
            Log.d(tag, "fetched task");
        } else {
            //there is no task to view
            finish();
        }

        view_task = ((RelativeLayout)findViewById(R.id.view_task));
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view_task_fields_container = (LinearLayout)view_task.findViewById(R.id.view_task_fields_container);

        task_name = ((TextView)view_task.findViewById(R.id.title));

        final Button button = ((Button)view_task.findViewById(R.id.new_button));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "new click");
                toggleButtonOptions(button);
            }
        });

        Button all_finished = ((Button)view_task.findViewById(R.id.all_finished));
        all_finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "all_finished clicked");
                toggleButtonOptions(button);
            }
        });

        Button part_finished = ((Button)view_task.findViewById(R.id.part_finished));
        part_finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "part_finished clicked");
                toggleButtonOptions(button);
                Dialog dialog = new WorkFinishedDialog(activity, task, 35);

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.d(tag, "Arbeitseinheit eingetragen, fertig! :)");
                        finish();
                    }
                });

                dialog.show();
            }
        });

        FloatingActionButton edit_button = ((FloatingActionButton)view_task.findViewById(R.id.edit_button));
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "edit_button clicked");
                Intent intent = new Intent(context, AddTask.class);
                Bundle b = new Bundle();
                b.putInt("id", task.getId()); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TimeRank.getTask(id) == null) {
            Log.d(tag, "Task doesn't exist anymore..." + task.description());
            finish();
        } else {
            fillFieldsBecauseOfData();
        }
    }

    private void fillFieldsBecauseOfData() {
        view_task_fields_container.removeAllViewsInLayout();

        task_name.setText(task.getName());
        task_name.setBackgroundColor(0xFF000000 | task.getColor());
        if (Util.isDarkColor(task.getColor())) {
            task_name.setTextColor(0xFFFFFFFF);
        }

        if (!task.getNotice().equals("")) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_task_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Notiz: ");
            ((TextView)row.findViewById(R.id.text_of_item)).setText(task.getNotice());
            view_task_fields_container.addView(row);
        }

        Duration worked_time_till_now = task.getWorkedTimeTillNow();
        if (worked_time_till_now.getDuration() > 0) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_task_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Bisher gearbeitet: ");
            ((TextView)row.findViewById(R.id.text_of_item)).setText(worked_time_till_now.description());
            view_task_fields_container.addView(row);
        }

        if (task.getRemaining_duration() > 0) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_task_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText(Task.task_duration_description);
            ((TextView)row.findViewById(R.id.text_of_item)).setText(Util.getFormattedDuration(task.getRemaining_duration()));
            view_task_fields_container.addView(row);
        }
        if (!Util.earlierDate(task.getEarliestStart(), new GregorianCalendar())) {
            //if it is not earlier... => it is in zukunft
            View row = inflater.inflate(R.layout.text_item_with_description, view_task_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText(Task.earliest_start_description);
            ((TextView)row.findViewById(R.id.text_of_item)).setText(Util.getFormattedDate(task.getEarliestStart()));
            view_task_fields_container.addView(row);
        }
        if (task.getDeadline() != null) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_task_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText(Task.deadline_description);
            ((TextView)row.findViewById(R.id.text_of_item)).setText(Util.getFormattedDateTime(task.getDeadline()));
            view_task_fields_container.addView(row);
        }
        if (task.hasRepeat()) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_task_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText(Task.repeat_description);
            ((TextView)row.findViewById(R.id.text_of_item)).setText("not implemented yet");
            view_task_fields_container.addView(row);
        }
        if (!task.getLabelString().equals("")) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_task_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText(Task.label_description);
            ((TextView)row.findViewById(R.id.text_of_item)).setText(task.getLabelString());
            view_task_fields_container.addView(row);
        }
    }

    private void toggleButtonOptions(Button button) {
        LinearLayout options_view = (LinearLayout) view_task.findViewById(R.id.button_options);
        if (options_view.getVisibility() == View.VISIBLE) {
            options_view.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_button));
            }
            button.setText("✓");
        } else {
            options_view.setVisibility(View.VISIBLE);
            //tooo new...
            // ((Button)findViewById(R.id.calender_day_new_button)).setBackground(getDrawable(R.drawable.rounded_button_inactive));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_button_inactive));
            }
            button.setText("-");
        }
    }

}
