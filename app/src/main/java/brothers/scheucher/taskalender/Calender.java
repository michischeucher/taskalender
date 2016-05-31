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


public class Calender extends Fragment implements ScaleGestureDetector.OnScaleGestureListener{
    private static final String tag = "Calender";
    private static final int LINE_HEIGHT = 8;
    private static final int MARGIN_LEFT = 100;
    private static float scale_factor = 1.0f;
    private float last_factor_updated = 1.0f;

    private static CalenderPagerAdapter calender_pager_adapter;
    ViewPager view_pager;

    private static final int MAX_SWIPES_LEFT_RIGHT = 50;
    private static int start_day_offset = 0;

    public static RelativeLayout ll;
    protected static Activity fa;
    private ScaleGestureDetector scale_dedector;

    public Calender() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "Calender: onCreateView");
        super.onCreate(savedInstanceState);
        fa = super.getActivity();
        ll = (RelativeLayout) inflater.inflate(R.layout.activity_calender, container, false);

        calender_pager_adapter = new CalenderPagerAdapter(super.getFragmentManager());
        view_pager = (ViewPager) ll.findViewById(R.id.day_pager);
        view_pager.setAdapter(calender_pager_adapter);
        view_pager.setCurrentItem(MAX_SWIPES_LEFT_RIGHT);

        scale_dedector = new ScaleGestureDetector(getActivity(), this);

        return ll;
    }

    @Override
    public void onResume() {
        Log.d(tag, "Calender: onResume");
        super.onResume();
    }

    public static void notifyChanges() {
        Log.d(tag, "Calender: changeSomeValues");
        if (calender_pager_adapter != null) {
            calender_pager_adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scale_factor *= detector.getScaleFactor();
        Log.d(tag, "scaling = " + scale_factor);
        scale_factor = Math.max(0.5f, Math.min(scale_factor, 5.0f));
        if (Math.abs(scale_factor - last_factor_updated) > 0.01f) { //if difference is bigger than threshold
            Log.d(tag, "scaling SET TO = " + scale_factor);
            last_factor_updated = scale_factor;
            calender_pager_adapter.scalingChanged();
        }

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.d(tag, "OnScaleBegin scale = " + scale_factor);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.d(tag, "OnScaleEnd scale = " + scale_factor);
    }

    private class CalenderPagerAdapter extends FragmentStatePagerAdapter {
        public CalenderPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(tag, "Calender: CalenderPagerAdapter: getItem");
            Fragment fragment = new DummySectionFragment(scale_dedector);
            Bundle args = new Bundle();
            args.putInt("number", i);
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i);
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


        public void scalingChanged() {
            Calender.ll.findViewById(R.id.calender_day_events_tasks).getLayoutParams().height = (int) (1440 * Calender.scale_factor);
            Calender.ll.findViewById(R.id.calender_day_events_tasks).requestLayout();
            Calender.ll.findViewById(R.id.calender_day_time_line).getLayoutParams().height = (int) (1440 * Calender.scale_factor);
            Calender.ll.findViewById(R.id.calender_day_time_line).requestLayout();

        }
    }

    public static class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";
        private Button new_button;
        private Button new_event;
        private Button new_task;
        private Button new_label;

        private View calender_day;
        private ScaleGestureDetector scale_dedector;
        private LinearLayout calender_day_events_tasks;
        private LinearLayout top_container_events;
        private TextView potential_text_view;
        protected LayoutInflater inflater;

        public DummySectionFragment() {
        }

        public DummySectionFragment(ScaleGestureDetector scale_dedector) {
            this.scale_dedector = scale_dedector;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            this.inflater = inflater;
            Log.d(tag, "Calender: DummySectionFragment: onCreateView");
            calender_day = inflater.inflate(R.layout.calender_day, container, false);
            calender_day_events_tasks = (LinearLayout)(calender_day.findViewById(R.id.calender_day_events_tasks));
            top_container_events = (LinearLayout)(calender_day.findViewById(R.id.top_container_events));
            potential_text_view = (TextView)(calender_day.findViewById(R.id.potential));
//            calender_day_events_tasks.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    Log.d(tag, "onTouch");
//                    if (scale_dedector.onTouchEvent(event)) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            });
//
            Bundle args = getArguments();
            int number = args.getInt("number");
            int current_date_offset = number - MAX_SWIPES_LEFT_RIGHT;

            GregorianCalendar current_date = new GregorianCalendar();
            current_date.set(GregorianCalendar.HOUR_OF_DAY, 0);
            current_date.set(GregorianCalendar.MINUTE, 0);
            current_date.add(GregorianCalendar.DAY_OF_YEAR, current_date_offset);

            Log.d(tag, "number " + number + " + current_date_offset " + current_date_offset + " => " + Util.getFormattedDate(current_date));



            TextView calender_day_date_view = ((TextView) calender_day.findViewById(R.id.calender_day_date));
            TextView calender_day_of_week_view = ((TextView) calender_day.findViewById(R.id.calender_day_of_week));
            calender_day_date_view.setText(Util.getDayOfMonthFormatted(current_date));
            calender_day_of_week_view.setText(Util.getDayOfWeekShort(current_date));
            GregorianCalendar now = new GregorianCalendar();
            if (Util.isSameDate(current_date, now)) {
                calender_day_date_view.setTextColor(0xFF0000FF);
                calender_day_of_week_view.setTextColor(0xFF0000FF);
            }

            //draw events (and calculated tasks for that day)
            drawEvents(current_date);

            //scaling:
            //calender_day_events_tasks.getLayoutParams().height = (int) (1440 * scale_factor);
            //time_line.getLayoutParams().height = (int) (1440 * scale_factor);

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
//                handleClickOnNewButton(v);
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

        private void drawEvents(GregorianCalendar date) {
            Day day = TimeRank.getDay(date);
            if (day == null) {
                day = TimeRank.createDay(date);
            }
            Log.d(tag, "### START DRAWING EVENTS... for " + Util.getFormattedDate(date) + " #events today = " + day.getEvents().size());

//            potential_text_view.setText(day.getPotential());
//            day.sortEvents();
            day.calculateBlocksAndColoumns();
            day.drawEvents(calender_day_events_tasks, inflater);
            day.drawWholeDayEvents(top_container_events, inflater);
            Log.d(tag, "### END DRAWING EVENTS! (for: " + Util.getFormattedDate(date) + ")");
        }

    }
}
