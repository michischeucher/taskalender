package brothers.scheucher.taskalender;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    private static final String tag = "MainActivity";
    private static FragmentManager fragment_manager;

    private String[] left_drawer_titles;
    private DrawerLayout drawer_layout;
    private ListView drawer_list;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        drawer_layout = ((DrawerLayout)findViewById(R.id.drawer_layout));

        TimeRank.startApplication(this);

        if (findViewById(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            Calender calender_fragment = new Calender();
            calender_fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, calender_fragment).commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //setting the left drawer
        TextView day_ansicht = ((TextView) findViewById(R.id.day_ansicht));
        day_ansicht.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "click on day ansicht...");
                Calender calender_fragment = new Calender();
                calender_fragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, calender_fragment).commit();
                drawer_layout.closeDrawers();
            }
        });
        TextView label_ansicht = ((TextView) findViewById(R.id.label_ansicht));
        label_ansicht.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "click on label ansicht...");
                LabelFragment label_fragment = LabelFragment.newInstance(-1);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, label_fragment).commit();
                drawer_layout.closeDrawers();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calender, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingDay.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_add_task) {
            Intent intent = new Intent(this, AddTask.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_add_event) {
            Intent intent = new Intent(this, AddEvent.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleClickOnEvent(View view) {
        int id = (int) view.getTag(R.string.id);
        boolean task_event = (boolean) view.getTag(R.string.task_event);
        Log.d(tag, "handleClickOnEvent id = " + id + " task_event = " + task_event);

        Intent intent;
        if (task_event) {
            intent = new Intent(this, ViewTask.class);
        } else {
            intent = new Intent(this, ViewEvent.class);
        }
        //fa = super.getActivity();
        //Intent intent = fa.getIntent();
        Bundle b = new Bundle();
        b.putInt("id", id); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    public void handleClickOnTask(View view) {
        int id = (int) view.getTag(R.string.id);
        Log.d(tag, "handleClickOnTask id = " + id);

        Intent intent = new Intent(this, AddTask.class);
        //fa = super.getActivity();
        //Intent intent = fa.getIntent();
        Bundle b = new Bundle();
        b.putInt("id", id); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }


}
