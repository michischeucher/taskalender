package brothers.scheucher.taskbro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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

    public static String DateToString(GregorianCalendar date) {
        if (date == null) {
            return "-";
        }
        int year = date.get(GregorianCalendar.YEAR);
        int month = date.get(GregorianCalendar.MONTH);
        int day = date.get(GregorianCalendar.DAY_OF_MONTH);
        int hour = date.get(GregorianCalendar.HOUR_OF_DAY);
        int minute = date.get(GregorianCalendar.MINUTE);
        String retval = year + "-" + month + "-" + day + "-" + hour + "-" + minute;
        return retval;
    }

    public static GregorianCalendar StringToDate(String date_string) {
        if (date_string.equals("-")) {
            return null;
        }
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
            year = 0;
            month = 0;
            day = 0;
            hour = 0;
            minute = 0;
        }
        GregorianCalendar d = new GregorianCalendar(year, month, day, hour, minute);
        return d;
    }

    public static String getFormattedDate(GregorianCalendar date) {
        if (date == null) {
            return "Kein Datum";
        }

        String dateFormatted = "";
        dateFormatted = getDateShort(date);
        if (dateFormatted.equals("")) {
            DateFormat df = new SimpleDateFormat("ccc, dd.MM.yyyy");
            dateFormatted = df.format(date.getTime());
        }

        return dateFormatted;
    }

    private static String getDateShort(GregorianCalendar date) {
        GregorianCalendar c = new GregorianCalendar();
        c.add(GregorianCalendar.DAY_OF_YEAR, -2);
        if (isSameDate(date,c)) {
            return "Vorgestern";
        }
        c.add(GregorianCalendar.DAY_OF_YEAR, 1);
        if (isSameDate(date, c)) { //is yesterday
            return "Gestern";
        }
        c.add(GregorianCalendar.DAY_OF_YEAR, 1);
        if (isSameDate(date, c)) {//is today
            return "Heute";
        }
        c.add(GregorianCalendar.DAY_OF_YEAR, 1);
        if (isSameDate(date, c)) { //is tomorrow
            return "Morgen";
        }
        c.add(GregorianCalendar.DAY_OF_YEAR, 1);
        if (isSameDate(date, c)) {
            return "Übermorgen";
        }
        return "";
    }

    public static String getFormattedTimeInner(GregorianCalendar date) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        String timeFormatted = df.format(date.getTime());
        return timeFormatted;
    }

    public static String getFormattedTime(GregorianCalendar date) {
        if (date == null) {
            return "-:- Uhr";
        }
        return getFormattedTimeInner(date) + " Uhr";
    }

    public static String getFormattedDateTime(GregorianCalendar date) {
        if (date == null) {
            return "Kein Datum";
        }
        if (Util.isNearlyNow(date)) {
            return "Jetzt";
        } else {
            return getFormattedDate(date) + ", " + getFormattedTime(date);
        }
    }

    public static String getFormattedDateTimeNotExact(GregorianCalendar date) {
        String ret = getDateShort(date);
        if (!ret.equals("")) {
            return ret + ", " + getFormattedTime(date);
        }

        GregorianCalendar now = new GregorianCalendar();
        if (Util.earlierDate(date, now)) {
            Util.setTime(now, 23,59);
            ret += "Vor ";
        } else {
            Util.setTime(now, 0, 0);
            ret += "In ";
        }


        int difference = getMinutesBetweenDates(now, date);
        int months = (int)(difference / (60 * 24 * 30));
        int weeks = (int)(difference / (60 * 24 * 7));
        int days = (int)(difference / (60 * 24));

        if (months == 1) {
            return ret + "einem Monat";
        }
        if (months != 0) {
            return ret + months + " Monaten";
        }
        if (weeks == 1) {
            return ret + "einer Woche";
        }
        if (weeks != 0) {
            return ret + weeks + " Wochen";
        }
        if (days == 1) {
            return ret + "einem Tag";
        }
        if (days != 0) {
            return ret + days + " Tagen";
        }

        return "FEHLER";
    }


    private static boolean isNearlyNow(GregorianCalendar date) {
        GregorianCalendar now = new GregorianCalendar();
        if (Math.abs(date.getTimeInMillis() - now.getTimeInMillis()) / 1000 / 60 < Settings.THRESHOLD_MINUTES_FOR_NOW) {
            return true;
        } else {
            return false;
        }
    }

    public static String getFormattedDateTimeToTime(GregorianCalendar first_date, GregorianCalendar second_date) {
        if (!isSameDate(first_date, second_date)) {
            Log.e(tag, "getFormattedDateTimeToTime... wtf");
        }
        GregorianCalendar now = new GregorianCalendar();
        if (isNearlyNow(first_date)) {
            return "Jetzt - " + getFormattedTime(second_date);
        } else if (isNearlyNow(second_date)) {
            return getFormattedTime(first_date) + " - Jetzt";
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
        if (date == null) {
            date = new GregorianCalendar();
            date.set(GregorianCalendar.HOUR_OF_DAY, 23);
            date.set(GregorianCalendar.MINUTE, 0);
        }
        date.set(GregorianCalendar.YEAR, year);
        date.set(GregorianCalendar.MONTH, month);
        date.set(GregorianCalendar.DAY_OF_MONTH, day);
    }

    public static void setTime(GregorianCalendar date, int hourOfDay, int minute) {
        date.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay);
        date.set(GregorianCalendar.MINUTE, minute);
    }

    public static boolean isSameDate(GregorianCalendar date, GregorianCalendar other_date) {
        if (date == null || other_date == null) {
            return false;
        }
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

    public static int getHour(GregorianCalendar date_with_time) {
        return date_with_time.get(GregorianCalendar.HOUR_OF_DAY);
    }
    public static int getMinute(GregorianCalendar date_with_time) {
        return date_with_time.get(GregorianCalendar.MINUTE);
    }

    public static String getFormattedDuration(int minutes) {
        String ret = "";
        if (minutes < 0) {
            ret += "-";
            minutes *= (-1);
        }

        int duration_hours = minutes / 60;
        int duration_minutes = minutes % 60;

        if (duration_hours != 0) {
            ret += duration_hours + " Std. ";
        }

        if (duration_minutes != 0) {
            ret += duration_minutes + " Min.";
        }

        if (duration_hours == 0 && duration_minutes == 0) {
            ret = "Keine Dauer";
        }
        return ret;
    }
    public static String getFormattedPotential(int minutes) {
        String ret = "";
        boolean negative = false;
        if (minutes == 0) {
            return "Kein Potential";
        }
        if (minutes < 0) {
            negative = true;
            ret += "ACHTUNG: ";
            minutes *= (-1);
        }
        int duration_hours = minutes / 60;
        int duration_minutes = minutes % 60;

        if (duration_hours > 0) {
            ret += duration_hours + " Std.";
        }

        if (duration_hours > 0 && duration_minutes > 0) {
            ret += " ";
        }

        if (duration_minutes > 0) {
            ret += duration_minutes + " Min.";
        }

        if (negative) {
            ret += " in Verzug";
        } else {
            ret += " Potential";
        }
        return ret;
    }

    public static String getFormattedPotentialShort(int minutes) {
        String ret = "";
        boolean is_negative = false;
        if (minutes == 0) {
            return "0 Min";
        }
        if (minutes < 0) {
            ret += "-";
            minutes *= (-1);
            is_negative = true;
        }
        int duration_weeks = minutes / (60 * 24 * 7);
        int duration_days = minutes / (60 * 24);
        int duration_hours = minutes / 60;

        if (duration_weeks != 0) {
            if (is_negative && (duration_days > 0 || duration_hours > 0)) {
                duration_weeks++;
            }
            ret += duration_weeks + " Wo.";
        } else if (duration_days != 0) {
            if (is_negative && duration_hours > 0) {
                duration_days++;
            }
            ret += duration_days + " T";
        } else if (duration_hours != 0) {
            if (is_negative && (minutes % 60) > 0) {
                duration_hours++;
            }
            ret += duration_hours + " Std";
        } else {
            ret += minutes + " Min";
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
        if (earlier_date == null) {
            return false;
        }
        if (date == null) {
            return true;
        }
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
        if ((r + g + b) < 370) {
            return true;
        } else {
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
        if (start_date == null || end_date == null) {
            return dates;
        }
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
        int end_minute = (int)(end.getTimeInMillis() / 1000 / 60);
        int start_minute = (int)(start.getTimeInMillis() / 1000 / 60);
        if (Util.earlierDate(start, end)) {
            return end_minute - start_minute;
        } else {
            return start_minute - end_minute;
        }
    }

    public static TimeObj calculateOverlappingTime(TimeObj first_time, TimeObj second_time) {
        TimeObj overlapping = new TimeObj(first_time.start, first_time.end);
        overlapping.start = (GregorianCalendar)Util.getLaterDate(first_time.start, second_time.start).clone();
        overlapping.end = (GregorianCalendar)Util.getEarlierDate(first_time.end, second_time.end).clone();

        if (Util.earlierDate(overlapping.end, overlapping.start) ||
                (Util.isSameDate(overlapping.start, overlapping.end) && Util.isSameTime(overlapping.start, overlapping.end))) {
            return null; //there is no overlapping...
        } else {
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
        DisplayMetrics metrics = TaskBroContainer.getContext().getResources().getDisplayMetrics();
        float fpixels = metrics.density * dp;
        return (int) (fpixels + 0.5f);
    }


    public static void setColorOfDrawable(View background, int color) {
        GradientDrawable sh = (GradientDrawable)background.getBackground();
        sh.setColor(color);

    }

    private static int dpToPixels(int dp) {
        DisplayMetrics displayMetrics = TaskBroContainer.getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int calculateWorkingDay(GregorianCalendar date) {
        return (date.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7;
    }

    public static void toggleNewButtonOptions(View new_button_option_view, Button new_button, Activity activity) {
        if (new_button_option_view.getVisibility() == View.VISIBLE) {
            new_button_option_view.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                new_button.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_button));
            }
            new_button.setText("+");
        } else {
            new_button_option_view.setVisibility(View.VISIBLE);
            //tooo new...
            // ((Button)findViewById(R.id.calender_day_new_button)).setBackground(getDrawable(R.drawable.rounded_button_inactive));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                new_button.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_button_inactive));
            }
            new_button.setText("-");
        }

    }

    public static void setNewButtonListeners(RelativeLayout ll, final Activity activity, final ViewPager view_pager, final int parent_label_id) {
        final Button new_button = (Button)ll.findViewById(R.id.new_button);
        final LinearLayout new_button_option_view = (LinearLayout) ll.findViewById(R.id.new_button_options);

        new_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "new click");

                Util.toggleNewButtonOptions(new_button_option_view, new_button, activity);
            }
        });

        Button new_event = ((Button)ll.findViewById(R.id.new_event_button));
        new_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "new event");
                int current_date_offset = 0;
                if (view_pager != null) {
                    current_date_offset = view_pager.getCurrentItem() - Calender.MAX_SWIPES_LEFT_RIGHT;
                }
                Log.d(tag, "current date offset = " + current_date_offset);

                Bundle bundle = new Bundle();
                bundle.putInt("current_date_offset", current_date_offset);
                bundle.putInt("id", -1);

                Intent intent = new Intent(activity, AddEvent.class);
                intent.putExtras(bundle);
                activity.startActivity(intent);
                Util.toggleNewButtonOptions(new_button_option_view, new_button, activity);
            }
        });

        Button new_task = ((Button)ll.findViewById(R.id.new_task_button));
        new_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "new task");
                Intent intent = new Intent(activity, AddTask.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", -1);
                bundle.putInt("parent_label_id", parent_label_id);
                intent.putExtras(bundle);
                activity.startActivity(intent);
                Util.toggleNewButtonOptions(new_button_option_view, new_button, activity);
            }
        });

        Button new_label = ((Button)ll.findViewById(R.id.new_label_button));
        new_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "new label");
                Intent intent = new Intent(activity, AddLabel.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", -1);
                bundle.putInt("parent_label_id", parent_label_id);
                intent.putExtras(bundle);
                activity.startActivity(intent);

                Util.toggleNewButtonOptions(new_button_option_view, new_button, activity);
            }
        });
    }

    public static String removeSpaces(String text) {
        return text.trim();
    }


    public static String getFormattedRepeat(int repeat_minutes) {
        if (repeat_minutes % AddTask.MONTLY_REPEAT_MINUTES == 0) {
            int months = repeat_minutes / AddTask.MONTLY_REPEAT_MINUTES;
            if (months == 1) {
                return "Monatlich";
            } else {
                return "Alle " + months + " Monate";
            }
        } else if (repeat_minutes % (60 * 24 * 7) == 0) {
            int weeks = repeat_minutes / (60 * 24 * 7);
            if (weeks == 1) {
                return "Wöchentlich";
            } else {
                return "Alle " + weeks + " Wochen";
            }
        } else if (repeat_minutes % (24 * 60) == 0) {
            int days = repeat_minutes / (60 * 24);
            if (days == 1) {
                return "Täglich";
            } else {
                return "Alle " + days + " Tage";
            }
        } else {
            return "Alle " + repeat_minutes + " Minuten";
        }

    }
}
