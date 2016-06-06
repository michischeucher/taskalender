package brothers.scheucher.taskalender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewEvent extends ActionBarActivity {

    private static final String tag = "ViewEvent";
    private MyEvent event;
    private Context context;
    private Activity activity;
    private RelativeLayout view_event;
    private LayoutInflater inflater;
    private LinearLayout view_event_fields_container;
    private TextView event_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        context = this;
        activity = this;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            int id = b.getInt("id");
            event = TimeRank.getEvent(id);
            Log.d(tag, "fetched event");
        } else {
            //there is no task to view
            finish();
        }

        view_event = ((RelativeLayout)findViewById(R.id.view_event));
        event_name = ((TextView)view_event.findViewById(R.id.title));
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view_event_fields_container = (LinearLayout)view_event.findViewById(R.id.view_event_fields_container);

        Button edit_button = ((Button)view_event.findViewById(R.id.edit_button));
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
        fillFieldsBecauseOfData();
    }

    private void fillFieldsBecauseOfData() {
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
