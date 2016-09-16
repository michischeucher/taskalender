package brothers.scheucher.taskbro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String tag = "MainActivity";
    private DrawerLayout drawer_layout;
    private static TextView potential_text_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        potential_text_view = (TextView)(findViewById(R.id.potential));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TaskBroContainer.startApplication(this);

        drawer_layout = ((DrawerLayout)findViewById(R.id.drawer_layout));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) drawer_layout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //first fragment
        displayView(R.id.nav_day);
        navigationView.setCheckedItem(R.id.nav_day);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyChanges();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingDay.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleClickOnDoneToggle(View view) {
        int id = (int) ((View)(view.getParent())).getTag(R.string.id);
        MyEvent event = TaskBroContainer.getEvent(id);
        Task task = event.getTask();
        ImageView event_done = (ImageView)view.findViewById(R.id.event_done);
        ImageView event_not_done = (ImageView)view.findViewById(R.id.event_not_done);
        if (event.isNot_created_by_user()) {
            event_done.setVisibility(ImageView.VISIBLE);
            event_not_done.setVisibility(ImageView.GONE);
            task.setRemaining_duration(task.getRemaining_duration() - event.getDurationInMinutes());
            task.save(this);

            event.setNot_created_by_user(false);
            event.save(this);
        } else {
            event_done.setVisibility(ImageView.GONE);
            event_not_done.setVisibility(ImageView.VISIBLE);
            task.setRemaining_duration(task.getRemaining_duration() + event.getDurationInMinutes());
            task.save(this);

            event.setNot_created_by_user(true);
            event.delete(this);
            TaskBroContainer.addEventToList(event); //because it should be visible anyway while there is no new calculation
        }
    }

    public void handleClickOnEvent(View view) {
        int id = (int) view.getTag(R.string.id);
        boolean task_event = (boolean) view.getTag(R.string.task_event);
        Log.d(tag, "handleClickOnEvent id = " + id + " task_event = " + task_event);

        Intent intent;
        Bundle b = new Bundle();
        if (task_event) {
            MyEvent event = TaskBroContainer.getEvent(id);
            id = event.getTask().getId();
            intent = new Intent(this, ViewTask.class);
        } else {
            intent = new Intent(this, ViewEvent.class);
        }
        //fa = super.getActivity();
        //Intent intent = fa.getIntent();
        b.putInt("id", id); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    public void handleClickOnPotential(View view) {
        Intent intent = new Intent(this, PotentialActivity.class);
        startActivity(intent);
    }

    private void displayView(int view_id) {
        drawer_layout.closeDrawer(GravityCompat.START);

        if (view_id == R.id.nav_day) {
            Calender fragment = new Calender();
            fragment.setArguments(getIntent().getExtras());
            getSupportActionBar().setTitle("Kalender");

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //transaction.addToBackStack("Kalender shown");
            transaction.replace(R.id.fragment_container, fragment).commit();
        } else if (view_id == R.id.nav_task) {
            LabelFragment fragment = LabelFragment.newInstance(-1);
//            fragment.setArguments(getIntent().getExtras());
            getSupportActionBar().setTitle("Labels");

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //transaction.addToBackStack("Labels shown");
            transaction.replace(R.id.fragment_container, fragment).commit();
        } else if (view_id == R.id.nav_day_settings) {
            Intent intent = new Intent(this, SettingDay.class);
            startActivity(intent);
        } else if (view_id == R.id.nav_settings) {
            Log.d(tag, "Settings clicked");
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(tag, "click item..." + id);
        displayView(id);
        return true;
    }

    public static void notifyChanges() {
        if (potential_text_view != null) {
            int potential = TaskBroContainer.getPotential();
            potential_text_view.setText(Util.getFormattedPotentialShort(potential));
            Log.d(tag, "PotentialShortVersion= " + Util.getFormattedPotentialShort(potential));
            if (potential < 0) {
                potential_text_view.setTextColor(Settings.TEXT_COLOR_ATTENTION);
            } else if(potential > 0) {
                potential_text_view.setTextColor(0xFFFFFFFF);
            }
        }
    }
}
