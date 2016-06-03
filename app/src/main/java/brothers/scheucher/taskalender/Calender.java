package brothers.scheucher.taskalender;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.GregorianCalendar;

public class Calender extends Fragment {
    private static final String tag = "Calender";

    private static final int MAX_SWIPES_LEFT_RIGHT = 50;

    private static CalenderPagerAdapter calender_pager_adapter;
    private ViewPager view_pager;
    public static RelativeLayout ll;
    protected static Activity fa;

    public static LinearLayout height_container;


    public Calender() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(tag, "onCreateView");
        super.onCreate(savedInstanceState);
        fa = super.getActivity();
        ll = (RelativeLayout) inflater.inflate(R.layout.activity_calender, container, false);

        calender_pager_adapter = new CalenderPagerAdapter(super.getFragmentManager());
        view_pager = (ViewPager) ll.findViewById(R.id.day_pager);
        view_pager.setAdapter(calender_pager_adapter);
        view_pager.setCurrentItem(MAX_SWIPES_LEFT_RIGHT);

        return ll;
    }

    public static void notifyChanges() {
        //Log.d(tag, "Calender: changeSomeValues");
        if (calender_pager_adapter != null) {
            calender_pager_adapter.notifyDataSetChanged();
        }
    }


    private class CalenderPagerAdapter extends FragmentStatePagerAdapter {
        private CalenderDayFragment fragment;

        public CalenderPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(tag, "Calender: CalenderPagerAdapter: getItem");
            fragment = new CalenderDayFragment();
            Bundle args = new Bundle();
            args.putInt("number", i);
            args.putInt(CalenderDayFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return MAX_SWIPES_LEFT_RIGHT * 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + (position + 1);
        }

        @Override
        public int getItemPosition(Object object) {
            Log.d(tag, "Calender: CalenderPagerAdapter: getItemPosition");
            return POSITION_NONE; //for updating all fragments, when notifyDatasetcahnge is called... :)
        }
    }

    public static class CalenderDayFragment extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";

        private Button new_button;
        private Button new_event;
        private Button new_task;
        private Button new_label;
        private View calender_day;
        private LinearLayout calender_day_events_tasks;
        private LinearLayout top_container_events;
        private TextView potential_text_view;
        protected LayoutInflater inflater;
        private GregorianCalendar current_date;

        private ScaleGestureDetector scale_dedector;

        public CalenderDayFragment() {
        }

        public CalenderDayFragment(ScaleGestureDetector scale_dedector) {
            this.scale_dedector = scale_dedector;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            this.inflater = inflater;
            calender_day = inflater.inflate(R.layout.calender_day, container, false);
            calender_day_events_tasks = (LinearLayout)(calender_day.findViewById(R.id.calender_day_events_tasks));
            top_container_events = (LinearLayout)(calender_day.findViewById(R.id.top_container_events));
            potential_text_view = (TextView)(calender_day.findViewById(R.id.potential));
            height_container = (LinearLayout)(calender_day.findViewById(R.id.height_container));

            Bundle args = getArguments();
            int number = args.getInt("number");
            int current_date_offset = number - MAX_SWIPES_LEFT_RIGHT;

            current_date = new GregorianCalendar();
            current_date.set(GregorianCalendar.HOUR_OF_DAY, 0);
            current_date.set(GregorianCalendar.MINUTE, 0);
            current_date.add(GregorianCalendar.DAY_OF_YEAR, current_date_offset);

            //Log.d(tag, "number " + number + " + current_date_offset " + current_date_offset + " => " + Util.getFormattedDate(current_date));

            TextView calender_day_date_view = ((TextView) calender_day.findViewById(R.id.calender_day_date));
            TextView calender_day_of_week_view = ((TextView) calender_day.findViewById(R.id.calender_day_of_week));
            calender_day_date_view.setText(Util.getDayOfMonthFormatted(current_date));
            calender_day_of_week_view.setText(Util.getDayOfWeekShort(current_date));
            GregorianCalendar now = new GregorianCalendar();
            if (Util.isSameDate(current_date, now)) {
                calender_day_date_view.setTextColor(0xFF0000FF);
                calender_day_of_week_view.setTextColor(0xFF0000FF);
            }

            new_button = (Button)calender_day.findViewById(R.id.calender_day_new_button);
            new_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(tag, "new click");
                    LinearLayout new_button_option_view = (LinearLayout) calender_day.findViewById(R.id.calender_day_new_button_options);

                    if (new_button_option_view.getVisibility() == View.VISIBLE) {
                        new_button_option_view.setVisibility(View.GONE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            new_button.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_button));
                        }
                        new_button.setText("+");
                    } else {
                        new_button_option_view.setVisibility(View.VISIBLE);
                        //tooo new...
                        // ((Button)findViewById(R.id.calender_day_new_button)).setBackground(getDrawable(R.drawable.rounded_button_inactive));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            new_button.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_button_inactive));
                        }
                        new_button.setText("-");
                    }
                }
            });

            new_event = ((Button)calender_day.findViewById(R.id.calender_day_new_event));
            new_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(tag, "new event");
                    Intent intent = new Intent(getActivity(), AddEvent.class);
                    startActivity(intent);
                }
            });
            new_task = ((Button)calender_day.findViewById(R.id.calender_day_new_task));
            new_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(tag, "new task");
                    Intent intent = new Intent(getActivity(), AddTask.class);
                    startActivity(intent);
                }
            });

            new_label = ((Button)calender_day.findViewById(R.id.calender_day_new_label));
            new_label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(tag, "new label");
                    Intent intent = new Intent(getActivity(), AddLabel.class);
                    startActivity(intent);
                }
            });

            return calender_day;
        }

        @Override
        public void onResume() {
            super.onResume();
            //draw events (and calculated tasks for that day)
            drawEvents(current_date);
        }

        private void drawEvents(GregorianCalendar date) {
            Day day = TimeRank.getDay(date);
            if (day == null) {
                day = TimeRank.createDay(date);
            }
            Log.d(tag, "Start drawing events for " + Util.getFormattedDate(date) + " #events today = " + day.getEvents().size());

            potential_text_view.setText(day.getPotential());
            day.calculateBlocksAndColoumns();
            day.drawEvents(calender_day_events_tasks, inflater);
            day.drawWholeDayEvents(top_container_events, inflater);
            Log.d(tag, "Finished drawing events for " + Util.getFormattedDate(date) + "");
        }

    }
}
