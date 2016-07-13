package brothers.scheucher.taskalender;

import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Created by Michael on 10.07.2015.
 */
public class Util {
    private static final String tag = "Util";
    private static final long TIME_IN_MINUTES = 3600000L; //mill * sec * min = 1000 * 60 * 60 =
    private static DateFormat date_format;
    private static DateFormat time_format;


    public static String DateToString(GregorianCalendar date) {
        if (date == null) {
            return "";
        }
        int year = date.get(GregorianCalendar.YEAR);
        int month = date.get(GregorianCalendar.MONTH);
        int day = date.get(GregorianCalendar.DAY_OF_MONTH);
        int hour = date.get(GregorianCalendar.HOUR_OF_DAY);
        int minute = date.get(GregorianCalendar.MINUTE);
        String retval = year + "-" + month + "-" + day + "-" + hour + "-" + minute;
//        Log.d("Util", "date to convert = " + getFormattedDate(date) + " time = " + getFormattedTime(date));
//        Log.d("Util", "converted to = " + retval);
        return retval;
    }

    public static GregorianCalendar StringToDate(String date_string) {
        String[] parts = date_string.split("-");
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        try {
            year = Integer.valueOf(parts[0]);
            month = Integer.valueOf(parts[1]);
            day = Integer.valueOf(parts[2]);
            hour = Integer.valueOf(parts[3]);
            minute = Integer.valueOf(parts[4]);
        } catch (NumberFormatException e){
//            Log.d(tag, "String to Date... exeption");
            year = 0;
            month = 0;
            day = 0;
            hour = 0;
            minute = 0;
        }
        GregorianCalendar d = new GregorianCalendar(year, month, day, hour, minute);
//        Log.d("Util", "converted date = " + getFormattedDate(d) + " time = " + getFormattedTime(d));
        return d;
    }

    public static String getFormattedDate(GregorianCalendar date) {
        DateFormat df = new SimpleDateFormat("ccc, dd.MM.yyyy");
        String dateFormatted = df.format(date.getTime());
        GregorianCalendar c = new GregorianCalendar();
        if (isSameDate(date, c)) {//is today
            dateFormatted = "Heute";
        }
        c.add(GregorianCalendar.DAY_OF_YEAR, 1);
        if (isSameDate(date, c)) { //is tomorrow
            dateFormatted = "Morgen";
        }
        c.add(GregorianCalendar.DAY_OF_YEAR, -2);
        if (isSameDate(date, c)) { //is yesterday
            dateFormatted = "Gestern";
        }
        return dateFormatted;
    }

    public static String getFormattedTimeInner(GregorianCalendar date) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        String timeFormatted = df.format(date.getTime());
        return timeFormatted;
    }

    public static String getFormattedTime(GregorianCalendar date) {
        return getFormattedTimeInner(date) + " Uhr";
    }

    public static String getFormattedDateTime(GregorianCalendar date) {
        return getFormattedDate(date) + ", " + getFormattedTime(date);
    }

    public static String getFormattedDateTimeToTime(GregorianCalendar first_date, GregorianCalendar second_date) {
        if (!isSameDate(first_date, second_date)) {
            Log.e(tag, "getFormattedDateTimeToTime... wtf");
        }
        return getFormattedDate(first_date) + ", " + getFormattedTimeInner(first_date) + "-" + getFormattedTime(second_date);
    }

    public static String getDayOfWeekShort(GregorianCalendar date) {
        if (date != null) {
            DateFormat df = new SimpleDateFormat("ccc");
            return df.format(date.getTime());
        } else {
            return "";
        }
    }

    public static void setDate(GregorianCalendar date, int year, int month, int day) {
        date.set(GregorianCalendar.YEAR, year);
        date.set(GregorianCalendar.MONTH, month);
        date.set(GregorianCalendar.DAY_OF_MONTH, day);
    }

    public static void setTime(GregorianCalendar date, int hourOfDay, int minute) {
        date.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay);
        date.set(GregorianCalendar.MINUTE, minute);
    }

    public static boolean isSameDate(GregorianCalendar date, GregorianCalendar other_date) {
        if (date.get(GregorianCalendar.YEAR) == other_date.get(GregorianCalendar.YEAR)
                && date.get(GregorianCalendar.DAY_OF_YEAR) == other_date.get(GregorianCalendar.DAY_OF_YEAR)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSameTime(GregorianCalendar first_date, GregorianCalendar second_date) {
        if (first_date.get(GregorianCalendar.HOUR_OF_DAY) == second_date.get(GregorianCalendar.HOUR_OF_DAY)
                && first_date.get(GregorianCalendar.MINUTE) == second_date.get(GregorianCalendar.MINUTE)) {
            return true;
        } else {
            return false;
        }
    }


    public static float calculateRemainingHours(GregorianCalendar current_date, GregorianCalendar deadline) {
        Log.d(tag, "deadline = " + deadline.getTimeInMillis() + " current date = " + current_date.getTimeInMillis());

        float remaining_hours = (float) ((deadline.getTimeInMillis() - current_date.getTimeInMillis()) / 1000 / 60.0 / 60.0);
        Log.d(tag, "remaining hours = " + remaining_hours);
        return remaining_hours;
    }

    public static int getColorCode(float remaining_time_to_deadline, int color) {
        Log.d(tag, "getColorCode: remaining time = " + remaining_time_to_deadline + " color " + color);
        if (remaining_time_to_deadline >= Settings.hours_before_deadline_no_problem) {
            Log.d(tag, "really much time left...");
            return (Settings.transparent_factor_no_problem << 24) | color;
        }
        if (remaining_time_to_deadline <= Settings.hours_before_deadline_urgent) {
            Log.d(tag, "NO TIME LEFT");
            return (Settings.transparent_factor_urgent << 24) | color;
        }

        int difference_hours = Settings.hours_before_deadline_no_problem - Settings.hours_before_deadline_urgent;
        float percentage = (remaining_time_to_deadline - Settings.hours_before_deadline_urgent) / difference_hours;

        //urgent = 0xFF no problem = 0x40
        int diff = Settings.transparent_factor_urgent - Settings.transparent_factor_no_problem;

        int alpha_value = (int)(diff * (1 - percentage) + Settings.transparent_factor_no_problem);
        alpha_value = alpha_value << 24;
        int ret = alpha_value | color;
        Log.d(tag, "getColorCode: ret val = " + ret);
        return ret;
    }

    public static int getRandomColor() {
        Random random_generator = new Random();
        int color = random_generator.nextInt(0xFFFFFF);
        return color;
    }
    public static int getRandomColorComponent() {
        Random random_generator = new Random();
        int color = random_generator.nextInt(0xFF);
        return color;
    }

    public static int getMinuteOfDay(GregorianCalendar date_with_time) {
        int hour = date_with_time.get(GregorianCalendar.HOUR_OF_DAY);
        int minute = date_with_time.get(GregorianCalendar.MINUTE);
        return hour * 60 + minute;
    }

    public static int getHourOfDay(GregorianCalendar date_with_time) {
        return date_with_time.get(GregorianCalendar.HOUR_OF_DAY);
    }
    public static int getMinute(GregorianCalendar date_with_time) {
        return date_with_time.get(GregorianCalendar.MINUTE);
    }


    public static String getFormattedDuration(int minutes) {
        String ret = "";
        int duration_hours = minutes / 60;
        int duration_minutes = minutes % 60;

        if (duration_hours > 0) {
            ret += duration_hours + " Std. ";
        }

        if (duration_minutes > 0) {
            ret += duration_minutes + " Min.";
        }

        if (duration_hours <= 0 && duration_minutes <= 0) {
            ret = "Keine Dauer";
        }
        return ret;
    }
    public static String getFormattedPotential(int minutes) {
        String ret = "";
        boolean negative = false;
        if (minutes < 0) {
            negative = true;
            ret += "ACHTUNG: ";
            minutes *= (-1);
        }
        int duration_hours = minutes / 60;
        int duration_minutes = minutes % 60;

        if (duration_hours > 0) {
            ret += duration_hours + " Std. ";
        }

        if (duration_minutes > 0) {
            ret += duration_minutes + " Min.";
        }

        if (duration_hours == 0 && duration_minutes == 0) {
            ret += "0 Min.";
        }
        if (negative) {
            ret += " in Verzug";
        } else {
            ret += " Potential";
        }
        return ret;
    }

    //TOUCHING
    public static double getDegreesFromTouchEvent(View v, float x, float y){
        double delta_x = x - v.getWidth() /2;
        double delta_y = v.getHeight() /2 - y;
        double radians = Math.atan2(delta_y, delta_x);
        return Math.toDegrees(radians);
    }

    public static String getDayOfMonthFormatted(GregorianCalendar date) {
        String ret = "";
        ret += date.get(GregorianCalendar.DAY_OF_MONTH);
        return ret;
    }

    public static boolean earlierDate(GregorianCalendar earlier_date, GregorianCalendar date) {
        if (earlier_date.compareTo(date) == -1) { //earlier date is earlier...
            return true;
        } else {
            return false;
        }
    }

    public static boolean earlierDateOrSame(GregorianCalendar earlier_date, GregorianCalendar date) {
        if (earlier_date.compareTo(date) < 1) { //earlier date is earlier...
            return true;
        } else {
            return false;
        }
    }


    public static boolean isDarkColor(int color) {
        int r = 0xFF0000 & color;
        r = r >> 16;
        int g = 0x00FF00 & color;
        g = g >> 8;
        int b = 0x0000FF & color;
//        Log.d(tag, "color = " + color + " is r = " + r + " g = " + g + " b = " + b);
        if ((r + g + b) < 430) {
//            Log.d(tag, "is dark");
            return true;
        } else {
//            Log.d(tag, "is not dark");
            return false;
        }
    }

    public static void setTime(GregorianCalendar date_to_modify, GregorianCalendar date) {
        int hour = date.get(GregorianCalendar.HOUR_OF_DAY);
        int minute = date.get(GregorianCalendar.MINUTE);
        Util.setTime(date_to_modify, hour, minute);
    }

    public static ArrayList<GregorianCalendar> getListOfDates(GregorianCalendar start_date, GregorianCalendar end_date) {
        ArrayList<GregorianCalendar> dates = new ArrayList<>();
        GregorianCalendar current_date = (GregorianCalendar)start_date.clone();
        GregorianCalendar end = (GregorianCalendar)end_date.clone();

        if (Util.earlierDate(end, current_date)) {
            end = current_date;
            current_date = (GregorianCalendar)end_date.clone();
        }
        dates.add((GregorianCalendar) current_date.clone());
        while (!Util.isSameDate(current_date, end)) {
            current_date.add(GregorianCalendar.DAY_OF_YEAR, 1);
            dates.add((GregorianCalendar) current_date.clone());
        }
        return dates;
    }

    public static int getMinutesBetweenDates(GregorianCalendar start, GregorianCalendar end) {
        if (Util.earlierDate(start, end)) {
            return (int)((end.getTimeInMillis() - start.getTimeInMillis()) / 1000 / 60);
        } else {
            return (int)((start.getTimeInMillis() - end.getTimeInMillis()) / 1000 / 60);
        }
    }

    public static TimeObj calculateOverlappingTime(TimeObj first_time, TimeObj second_time) {
        TimeObj overlapping = new TimeObj(first_time.start, first_time.end);
        overlapping.start = (GregorianCalendar)Util.getLaterDate(first_time.start, second_time.start).clone();
        overlapping.end = (GregorianCalendar)Util.getEarlierDate(first_time.end, second_time.end).clone();

//        Log.d(tag, "calculateOverlappingTime: " + Util.getFormattedDateTime(first_time.start) + "-" + Util.getFormattedDateTime(first_time.end));
//        Log.d(tag, "   " + Util.getFormattedDateTime(second_time.start) + "-" + Util.getFormattedDateTime(second_time.end));
        if (Util.earlierDate(overlapping.end, overlapping.start) ||
                (Util.isSameDate(overlapping.start, overlapping.end) && Util.isSameTime(overlapping.start, overlapping.end))) {
            //there is no overlapping...
//            Log.d(tag, "No overlapping...");
            return null;
        } else {
//            Log.d(tag, "=>overlapping: " + Util.getFormattedDateTime(overlapping.start) + "-" + Util.getFormattedDateTime(overlapping.end));
            return overlapping;
        }
    }

    private static GregorianCalendar getLaterDate(GregorianCalendar first_date, GregorianCalendar second_date) {
        if (Util.earlierDate(first_date, second_date)) {
            return second_date;
        } else {
            return first_date;
        }
    }

    private static GregorianCalendar getEarlierDate(GregorianCalendar first_date, GregorianCalendar second_date) {
        if (Util.earlierDate(first_date, second_date)) {
            return first_date;
        } else {
            return second_date;
        }
    }

    public static String getFormattedDateTimeToDateTime(GregorianCalendar start, GregorianCalendar end) {
        if (isSameDate(start, end)) {
            return getFormattedDateTimeToTime(start, end);
        } else {
            return getFormattedDateTime(start) + " - " + getFormattedDateTime(end);
        }
    }

    public static void setWeight(View block, int weight) {
        LinearLayout.LayoutParams params;
        params = (LinearLayout.LayoutParams) block.getLayoutParams();
        if (params != null) {
            params.weight = weight;
            block.setLayoutParams(params);
        }
    }

    public static String getFormattedTimeToTime(GregorianCalendar start, GregorianCalendar end) {
        return Util.getFormattedTimeInner(start) + " - " + Util.getFormattedTime(end);
    }

    public static int calculatePixelFromDB(int dp) {
        DisplayMetrics metrics = TimeRank.getContext().getResources().getDisplayMetrics();
        float fpixels = metrics.density * dp;
        return (int) (fpixels + 0.5f);
    }


    public static void setColorOfDrawable(LinearLayout background, int color) {
        GradientDrawable sh = (GradientDrawable)background.getBackground();
        sh.setColor(color);

    }

    public static void setSupportActionBarTitle(ViewEvent mActivity, ActionBar supportActionBar, String name) {
        final FrameLayout frameLayout = new FrameLayout(mActivity);
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        frameLayout.setLayoutParams(frameLayoutParams);


        // Create new LinearLayout
        final LinearLayout linearLayout = new LinearLayout(mActivity);
        frameLayoutParams =new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dpToPixels(78));
        frameLayoutParams.gravity = Gravity.LEFT;
        linearLayout.setLayoutParams(frameLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        // Add textviews
        final TextView textView1 = new TextView(mActivity);
        LinearLayout.LayoutParams linearLayoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        frameLayoutParams.gravity = Gravity.BOTTOM;
        textView1.setLayoutParams(linearLayoutParams);
        textView1.setText("Title");
        textView1.setTextColor(ContextCompat.getColor(mActivity, R.color.materialcolorpicker__white));
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        linearLayout.addView(textView1);


        final TextView textView2 = new TextView(mActivity);
        linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView2.setLayoutParams(linearLayoutParams);
        textView2.setText("Subtitle");
        textView2.setTextColor(ContextCompat.getColor(mActivity, R.color.materialcolorpicker__white));
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        linearLayout.addView(textView2);

        frameLayout.addView(linearLayout);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)mActivity.findViewById(R.id.toolbar_layout);
        collapsingToolbar.addView(frameLayout);
        final float SCALE_MIN=0.4f;
        AppBarLayout appBarLayout = (AppBarLayout) mActivity.findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offSet) {
                float collapsedRatio = (float) offSet / appBarLayout.getTotalScrollRange();
                linearLayout.setScaleX(1 + (collapsedRatio * SCALE_MIN));
                linearLayout.setScaleY(1 + (collapsedRatio * SCALE_MIN));
                FrameLayout.LayoutParams frameLayoutParams =new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dpToPixels(78));
                frameLayoutParams.gravity = Gravity.BOTTOM;
                frameLayoutParams.setMargins(Math.round(dpToPixels(48) * (1+collapsedRatio)), 0, 0, Math.round(dpToPixels(15) * collapsedRatio));
                linearLayout.setLayoutParams(frameLayoutParams);
                // You can also setTransitionY/X, setAlpha, setColor etc.
            }
        });
    }

    private static int dpToPixels(int dp) {
        DisplayMetrics displayMetrics = TimeRank.getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
