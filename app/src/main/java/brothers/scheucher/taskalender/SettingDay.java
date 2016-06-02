package brothers.scheucher.taskalender;


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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class SettingDay extends ActionBarActivity {
    private static final String tag = "SettingDay";
    private Context context;
    private PieChart setting_day_chart;
    private TextView duration_view;

    private DaySettingObject day_setting;
    private Duration rest_duration;
    private Duration sum_distributed_duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_day);
        this.context = this;
        this.day_setting = TimeRank.getDaySettingObject(new GregorianCalendar());

        //GETTING VIEWS
        duration_view = ((TextView)findViewById(R.id.setting_day_duration));

        LinearLayout setting_day_content = ((LinearLayout)findViewById(R.id.setting_day_content));


        //SETTING VALUES
        duration_view.setText(Util.getFormattedDuration(day_setting.getTotalDurationInMinutes()));
        sum_distributed_duration = new Duration(0);

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


        //ADDING ALL ROOT LABELS
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (final Label l : TimeRank.getRootLabels()) {
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

        MyCalendarProvider.googleCalendar();
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
            TimeRank.addDaySettingObject(day_setting);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
