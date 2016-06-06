package brothers.scheucher.taskalender;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Calender extends Fragment {
    private static final String tag = "Calender";

    private static final int MAX_SWIPES_LEFT_RIGHT = 50;

    private static CalenderPagerAdapter calender_pager_adapter;
    private static int scroll_pos;
    private static ViewPager view_pager;
    public static RelativeLayout ll;
    protected static Activity fa;
    private static ArrayList<ScrollViewScalable> scroll_positions;

    public Calender() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scroll_pos = 0;
        scroll_positions = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "onCreateView");
        super.onCreate(savedInstanceState);
        fa = super.getActivity();
        ll = (RelativeLayout) inflater.inflate(R.layout.activity_calender, container, false);

        calender_pager_adapter = new CalenderPagerAdapter(super.getFragmentManager());
        view_pager = (ViewPager) ll.findViewById(R.id.day_pager);
        view_pager.setAdapter(calender_pager_adapter);
        view_pager.setCurrentItem(MAX_SWIPES_LEFT_RIGHT);

        return ll;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static void notifyChanges() {
        //Log.d(tag, "Calender: changeSomeValues");
        if (calender_pager_adapter != null) {
            calender_pager_adapter.notifyDataSetChanged();
        }
    }

    public static void setScrollPosition(int t) {
        Calender.scroll_pos = t;
    }

    public static void notifyScalingOrScrollingChanged() {
        for (ScrollViewScalable s : scroll_positions) {
            s.setScrollY(Calender.scroll_pos);
            s.scalingChanged();
        }
    }

    public static int getScrollPosition() {
        return Calender.scroll_pos;
    }

    public static void goToNow() {
        view_pager.setCurrentItem(MAX_SWIPES_LEFT_RIGHT);
    }


    private class CalenderPagerAdapter extends FragmentStatePagerAdapter {
        private CalenderDayFragment fragment;

        public CalenderPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            //Log.d(tag, "Calender: CalenderPagerAdapter: getItem");
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
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(state, loader);
            Log.d(tag, "restored");
        }


        @Override
        public int getItemPosition(Object object) {
            //Log.d(tag, "Calender: CalenderPagerAdapter: getItemPosition");
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
        private LinearLayout height_container;
        private ScrollViewScalable day_view_scrolling;

        public CalenderDayFragment() {
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
            day_view_scrolling = (ScrollViewScalable)(calender_day.findViewById(R.id.day_view_scrolling));

            day_view_scrolling.scalingChanged();
            if (!scroll_positions.contains(day_view_scrolling)) {
                scroll_positions.add(day_view_scrolling);
            }
//            day_view_scrolling.scrollTo(0, Calender.scroll_pos);

            day_view_scrolling.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
//                view.scalingChanged();
                    GregorianCalendar now = new GregorianCalendar();
                    if (Util.isSameDate(current_date, now)) {
                        int scroll_minute = Util.getMinuteOfDay(now) - 80;
                        int scroll_position = (int) ((scroll_minute / 1440.0f) * height_container.getLayoutParams().height);
                        Calender.setScrollPosition(scroll_position);
                        day_view_scrolling.scrollTo(0, scroll_position);
                    } else {
                        day_view_scrolling.scrollTo(0, Calender.getScrollPosition());
                    }
                    Log.d(tag, "on pre draw with scroll: " + Calender.getScrollPosition());
                    day_view_scrolling.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }

            });
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
            day.drawWholeDayEvents(height_container, top_container_events, inflater);

            Log.d(tag, "Finished drawing events for " + Util.getFormattedDate(date) + "");
        }

    }
}
