package brothers.scheucher.taskbro;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Calender extends Fragment {
    private static final String tag = "Calender";

    public static final int MAX_SWIPES_LEFT_RIGHT = 100;

    private static CalenderPagerAdapter calender_pager_adapter;
    private static int scroll_pos;
    private static ViewPager view_pager;
    public static RelativeLayout ll;
    private static ArrayList<ScrollViewScalable> scroll_positions;
    public static boolean got_to_now;

    public Calender() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scroll_pos = 0;
        scroll_positions = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_calender, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_go_to_now) {
            Log.d(tag, "going to now...");
            Calender.goToNow();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "### ### onCreateView Calender");
        super.onCreate(savedInstanceState);
        ll = (RelativeLayout) inflater.inflate(R.layout.activity_calender, container, false);

        got_to_now = true;

        calender_pager_adapter = new CalenderPagerAdapter(super.getFragmentManager());
        view_pager = (ViewPager) ll.findViewById(R.id.day_pager);
        view_pager.setAdapter(calender_pager_adapter);
        view_pager.setCurrentItem(MAX_SWIPES_LEFT_RIGHT);

        Util.setNewButtonListeners(ll, getActivity(), view_pager, -1);

        return ll;
    }



    @Override
    public void onResume() {
        Log.d(tag, "### ### onResume Calender");
        super.onResume();
    }

    public static void notifyChanges() {
        Log.d(tag, "Calender: changeSomeValues");
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
        Calender.got_to_now = true;
        view_pager.setCurrentItem(MAX_SWIPES_LEFT_RIGHT);
        Calender.setScrollPosition(CalenderDayFragment.calculateScrollposition(new GregorianCalendar()));
        notifyScalingOrScrollingChanged();
//        calender_pager_adapter.notifyDataSetChanged();
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

        private View calender_day;
        private LinearLayout calender_day_events_tasks;
        private LinearLayout top_container_events;
        protected LayoutInflater inflater;
        private GregorianCalendar current_date;
        private static LinearLayout height_container;
        private ScrollViewScalable day_view_scrolling;

        public CalenderDayFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(tag, "### ### onCreateView Adapter");
            this.inflater = inflater;
            calender_day = inflater.inflate(R.layout.calender_day, container, false);
            calender_day_events_tasks = (LinearLayout)(calender_day.findViewById(R.id.calender_day_events_tasks));
            top_container_events = (LinearLayout)(calender_day.findViewById(R.id.top_container_events));
            height_container = (LinearLayout)(calender_day.findViewById(R.id.height_container));
            day_view_scrolling = (ScrollViewScalable)(calender_day.findViewById(R.id.day_view_scrolling));

            day_view_scrolling.scalingChanged();
            if (!scroll_positions.contains(day_view_scrolling)) {
                scroll_positions.add(day_view_scrolling);
            }
//            day_view_scrolling.scrollTo(0, Calender.scroll_pos);

            Bundle args = getArguments();
            int number = args.getInt("number");
            int current_date_offset = number - MAX_SWIPES_LEFT_RIGHT;

            current_date = new GregorianCalendar();
            current_date.set(GregorianCalendar.HOUR_OF_DAY, 0);
            current_date.set(GregorianCalendar.MINUTE, 0);
            current_date.add(GregorianCalendar.DAY_OF_YEAR, current_date_offset);

            day_view_scrolling.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    GregorianCalendar now = new GregorianCalendar();
                    if (got_to_now && Util.isSameDate(current_date, now)) {
                        got_to_now = false;

                        day_view_scrolling.scrollTo(0, calculateScrollposition(now));
                    } else {
                        day_view_scrolling.scrollTo(0, Calender.getScrollPosition());
                    }
                    Log.d(tag, "on pre draw with scroll: " + Calender.getScrollPosition());
                    day_view_scrolling.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }

            });

            //Log.d(tag, "number " + number + " + current_date_offset " + current_date_offset + " => " + Util.getFormattedDate(current_date));

            TextView calender_day_date_view = ((TextView) calender_day.findViewById(R.id.calender_day_date));
            TextView calender_day_of_week_view = ((TextView) calender_day.findViewById(R.id.calender_day_of_week));
            calender_day_date_view.setText(Util.getDayOfMonthFormatted(current_date));
            calender_day_of_week_view.setText(Util.getDayOfWeekShort(current_date));
            GregorianCalendar now = new GregorianCalendar();
            if (Util.isSameDate(current_date, now)) {
                calender_day_date_view.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_color));
                calender_day_of_week_view.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_color));
            }

            return calender_day;
        }

        @Override
        public void onResume() {
            super.onResume();
            //draw events (and calculated tasks for that day)
            drawEvents(current_date);
        }

        private void drawEvents(GregorianCalendar date) {
            Day day = TaskBroContainer.getDay(date);
            if (day == null) {
                day = TaskBroContainer.createDay(date);
            }
            Log.d(tag, "Start drawing events for " + Util.getFormattedDate(date) + " #events today = " + day.getEvents().size());


            TaskBroContainer.distributeTasksFromTaskBlocksTillDate(day.getStart());

            day.calculateBlocksAndColoumns();

            day.drawNowIndicator(calender_day.findViewById(R.id.now_view_container));
            day.drawEarliestStartLatestEndIndicators(calender_day.findViewById(R.id.earliest_start_latest_end_indicators));
            day.drawEvents(calender_day_events_tasks, inflater);
            day.drawWholeDayEvents(height_container, top_container_events, inflater);

            Log.d(tag, "Finished drawing events for " + Util.getFormattedDate(date) + "");
        }

        public static int calculateScrollposition(GregorianCalendar now) {
            int scroll_minute = Util.getMinuteOfDay(now) - 80;
            int scroll_position = (int) ((scroll_minute / 1440.0f) * height_container.getLayoutParams().height);
            return scroll_position;
        }


    }
}
