package brothers.scheucher.taskbro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewEvent extends AppCompatActivity {

    private static final String tag = "ViewEvent";
    private MyEvent event;
    private int id;
    private Context context;
    private LayoutInflater inflater;
    private LinearLayout view_event_fields_container;
    private TextView event_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            id = b.getInt("id");
            event = TaskBroContainer.getEvent(id);
        } else {
            //there is no task to view
            finish();
        }

        event_name = ((TextView)findViewById(R.id.title));
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view_event_fields_container = (LinearLayout)findViewById(R.id.view_event_fields_container);

        FloatingActionButton edit_button = ((FloatingActionButton)findViewById(R.id.edit_button));
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "edit_button clicked");
                Intent intent = new Intent(context, AddEvent.class);
                Bundle b = new Bundle();
                b.putInt("id", event.getId()); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });

//        fillFieldsBecauseOfData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TaskBroContainer.getEvent(id) == null) {
            Log.d(tag, "Event doesn't exist anymore...");
            finish();
        } else {
            fillFieldsBecauseOfData();
        }
    }

    private void fillFieldsBecauseOfData() {
//        getSupportActionBar().setTitle(event.getName());
//        Util.setSupportActionBarTitle(this, getSupportActionBar(), event.getName());

        event_name.setText(event.getName());
        event_name.setBackgroundColor(0xFF000000 | event.getColor());
        if (Util.isDarkColor(event.getColor())) {
            event_name.setTextColor(0xFFFFFFFF);
        }


        view_event_fields_container.removeAllViewsInLayout();

        if (!event.getNotice().equals("")) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_event_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Notiz: ");
            ((TextView)row.findViewById(R.id.text_of_item)).setText(event.getNotice());
            view_event_fields_container.addView(row);
        }
        if (!Util.isSameDate(event.getStart(), event.getEnd())) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_event_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Start");
            ((TextView)row.findViewById(R.id.text_of_item)).setText(Util.getFormattedDateTime(event.getStart()));
            view_event_fields_container.addView(row);

            row = inflater.inflate(R.layout.text_item_with_description, view_event_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Ende");
            ((TextView)row.findViewById(R.id.text_of_item)).setText(Util.getFormattedDateTime(event.getEnd()));
            view_event_fields_container.addView(row);
        } else {
            //same date, therefore much more cleaner
            View row = inflater.inflate(R.layout.text_item_with_description, view_event_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Zeit");
            ((TextView)row.findViewById(R.id.text_of_item)).setText(Util.getFormattedDateTimeToTime(event.getStart(), event.getEnd()));
            view_event_fields_container.addView(row);
        }

        if (!event.isBlocking()) {
            View row = inflater.inflate(R.layout.text_item_with_description, view_event_fields_container, false);
            ((TextView)row.findViewById(R.id.description_of_item)).setText("Kein Blockiernder Termin!");
            ((TextView)row.findViewById(R.id.text_of_item)).setText("Während diesem Termin werden Aufgaben vorgeschlagen.");
            view_event_fields_container.addView(row);
        }

        }

}
