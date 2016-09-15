package brothers.scheucher.taskbro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.GregorianCalendar;

public class PotentialActivity extends AppCompatActivity {
    static LayoutInflater inflater;
    static LinearLayout task_block_container;
    private static final String tag = "PotentialActivity";
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;


        setContentView(R.layout.activity_potential);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle("Potential");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SettingDay.class);
                startActivity(intent);

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        inflater = (LayoutInflater) getLayoutInflater();
        task_block_container = (LinearLayout)findViewById(R.id.task_block_container);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fillFieldsBecauseOfData();

    }

    private static void fillFieldsBecauseOfData() {

        if (task_block_container == null || inflater == null) {
            return;
        }
        task_block_container.removeAllViewsInLayout();


        for (int j = (TaskBroContainer.getTaskBlocks().size() - 1); j >= 0; j--) {
            TaskBlock tb = TaskBroContainer.getTaskBlocks().get(j);
            View row = inflater.inflate(R.layout.text_item_with_description, task_block_container, false);
            if (Util.earlierDate(tb.getEnd(), new GregorianCalendar())) {
                ((TextView) row.findViewById(R.id.description_of_item)).setText("Vergangene Deadlines");
            } else if (tb.getEnd() == null && tb.getStart() == null) {
                ((TextView)row.findViewById(R.id.description_of_item)).setText("Aufgaben ohne Deadlines");
                ((TextView)row.findViewById(R.id.text_of_item)).setVisibility(View.GONE);
            } else {
                ((TextView)row.findViewById(R.id.description_of_item)).setText(Util.getFormattedDateTimeToDateTime(tb.getStart(), tb.getEnd()));
            }
            ((TextView)row.findViewById(R.id.text_of_item)).setText(Util.getFormattedPotential(tb.getPotential()));
            if (tb.getPotential() < 0) {
                ((TextView)row.findViewById(R.id.text_of_item)).setTextColor(Settings.TEXT_COLOR_ATTENTION);
            } else if (tb.getPotential() > 0){
                ((TextView)row.findViewById(R.id.text_of_item)).setTextColor(Settings.TEXT_COLOR_PERFECT);
            }

            task_block_container.addView(row);

            for (int i = (tb.getTasks().size() - 1); i >=0; i--) {
                Task t = tb.getTasks().get(i);
                View rowView = t.createTaskInListView(inflater, task_block_container);
                task_block_container.addView(rowView);
            }
        }
    }

    public static void notifyChanges() {
        fillFieldsBecauseOfData();
    }
}
