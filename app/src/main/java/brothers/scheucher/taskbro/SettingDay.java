package brothers.scheucher.taskbro;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class SettingDay extends ActionBarActivity {
    private static final String tag = "SettingDay";
    private Context context;
    private PieChart setting_day_chart;
    private TextView duration_view;
    private TextView earliest_start_view;
    private TextView latest_end_view;
    private TextView working_days_view;

    private GregorianCalendar earliest_start;
    private GregorianCalendar latest_end;

    private DaySettingObject day_setting;
    private Duration rest_duration;
    private Duration sum_distributed_duration;
    private TextView working_days;
    public final static int NUM_DAYS_OF_WEEK = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_settings);
        this.context = this;
        this.day_setting = TaskBroContainer.getDaySettingObject(new GregorianCalendar());

        //GETTING VIEWS
        working_days_view = ((TextView)findViewById(R.id.working_days));
        earliest_start_view = ((TextView)findViewById(R.id.earliest_start_time));
        latest_end_view = ((TextView)findViewById(R.id.latest_end_time));
        duration_view = ((TextView)findViewById(R.id.setting_day_duration));
        working_days = ((TextView)findViewById(R.id.working_days));

        LinearLayout setting_day_content = ((LinearLayout)findViewById(R.id.setting_day_content));

        earliest_start = new GregorianCalendar();
        latest_end = new GregorianCalendar();
        Util.setTime(earliest_start, day_setting.getEarliest_minute() / 60, day_setting.getEarliest_minute() % 60);
        Util.setTime(latest_end, day_setting.getLatest_minute() / 60, day_setting.getLatest_minute() % 60);

        //SETTING VALUES
        duration_view.setText(Util.getFormattedDuration(day_setting.getTotalDurationInMinutes()));
        earliest_start_view.setText(Util.getFormattedTime(earliest_start));
        latest_end_view.setText(Util.getFormattedTime(latest_end));
        sum_distributed_duration = new Duration(0);
        working_days.setText(day_setting.getWorkingDaysString());

        //SETTING LISTENERS
        duration_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DurationPickerDialog duration_dialog = new DurationPickerDialog(SettingDay.this, duration_view, day_setting.getTotal_duration());
                duration_dialog.setMaxValue(24, 0);
                duration_dialog.setDuration_min(sum_distributed_duration);
                duration_dialog.setTitle("Zeit Pro Tag");
                duration_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        settingPieChartBecauseOfData();
                    }
                });
                duration_dialog.show();
            }
        });

        earliest_start_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(context, new onTimeSetListener(earliest_start_view, earliest_start), Util.getHour(earliest_start), Util.getMinute(earliest_start), true);
                dialog.show();
            }
        });
        latest_end_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(context, new onTimeSetListener(latest_end_view, latest_end), Util.getHour(latest_end), Util.getMinute(latest_end), true);
                dialog.show();
            }
        });

        working_days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "Click On Label Selection...");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] working_days_strings = getResources().getStringArray(R.array.week_names);
                final boolean[] selected_working_days = day_setting.getWorkingDaysBoolean();
                final ArrayList<Integer> selected_item_index_list = new ArrayList<>();
                for (int i = 0; i < selected_working_days.length; i++) {
                    if (day_setting.isWorkingDay(i)) {
                        selected_working_days[i++] = true;
                    } else {
                        selected_working_days[i++] = false;
                    }
                }
                builder.setMultiChoiceItems(working_days_strings, selected_working_days, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selected_working_days[which] = true;
                        } else {
                            selected_working_days[which] = false;
                            boolean nothing_checked = true;
                            for (int i = 0; i < selected_working_days.length; i++) {
                                if (selected_working_days[i]) {
                                    nothing_checked = false;
                                }
                            }
                            if (nothing_checked) {
                                MyNotifications.createToastShort("You must select at least one day");
                                AlertDialog d = (AlertDialog) dialog;
                                ListView v = d.getListView();
                                v.setItemChecked(which, true);
                                selected_working_days[which] = true;
                            }
                        }
                    }
                })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                day_setting.setWorkingDays(selected_working_days);
                                working_days.setText(day_setting.getWorkingDaysString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(tag, "cancelled");
                            }
                        });
                builder.show();
            }
        });



        //ADDING ALL ROOT LABELS
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (final Label l : TaskBroContainer.getRootLabels()) {
            final Duration label_duration = day_setting.addLabelDurationIfNotExist(l);

            //LABEL WITH DURATION
            View rowView = inflater.inflate(R.layout.label_in_duration_list, setting_day_content, false);

            TextView name_view = (TextView) rowView.findViewById(R.id.label_name);
            View color_view = (View) rowView.findViewById(R.id.label_color);
            final TextView duration_value_view = ((TextView)rowView.findViewById(R.id.duration_value));
            duration_value_view.setText(Util.getFormattedDuration(label_duration.getDuration()));

            name_view.setText(l.getName());
            color_view.setBackgroundColor(0xFF000000 | l.getColor());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DurationPickerDialog duration_picker = new DurationPickerDialog(SettingDay.this, duration_value_view, label_duration);
                    duration_picker.setMaxValue(rest_duration.getDuration() + duration_picker.getDuration());
                    duration_picker.setTitle("Dauer");
                    duration_picker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            settingPieChartBecauseOfData();
                            setting_day_chart.notifyChanges();
                        }
                    });

                    duration_picker.show();

                }
            });

            setting_day_content.addView(rowView);

        }

        rest_duration = day_setting.addLabelDurationIfNotExist(new Label("REST", 0xFFFFFF));

        setting_day_chart = ((PieChart)findViewById(R.id.setting_day_chart));

        settingPieChartBecauseOfData();

    }

    private void settingPieChartBecauseOfData() {
        ArrayList<PieChartElement> pie_elements = new ArrayList<PieChartElement>();

        int sum = 0;
        for (Pair<Label, Duration> label_duration : day_setting.getLabels_durations()) {
            if (!label_duration.first.getName().equals("REST")) {
                sum += label_duration.second.getDuration();
            }
        }
        this.sum_distributed_duration.setDuration(sum);
        if (this.rest_duration == null) {
            Log.d(tag, "ERRROROROROROR");
        }
        this.rest_duration.setDuration(day_setting.getTotalDurationInMinutes() - sum);

        for (Pair<Label, Duration> label_duration : day_setting.getLabels_durations()) {
            PieChartElement pe = new PieChartElement(label_duration.first.getName(), label_duration.first.getColor(), label_duration.second);
            pie_elements.add(pe);
        }
        setting_day_chart.setElements(pie_elements);
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
            MyConstraint constraint = new MyConstraint();
            day_setting.addConstraint(constraint);
            day_setting.setEarliest_minute(Util.getMinuteOfDay(earliest_start));
            day_setting.setLatest_minute(Util.getMinuteOfDay(latest_end));
            day_setting.save(this);
            TaskBroContainer.addDaySettingObject(day_setting);
            TaskBroContainer.calculateDays();
//            TaskBroContainer.createCalculatingJob();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class onTimeSetListener implements TimePickerDialog.OnTimeSetListener {
        private TextView text_view;
        private GregorianCalendar time;

        public onTimeSetListener(TextView view, GregorianCalendar time) {
            this.text_view = view;
            this.time = time;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Util.setTime(time, hourOfDay, minute);
            this.text_view.setText(Util.getFormattedTime(time));
        }
    }
}
