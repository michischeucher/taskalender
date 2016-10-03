package brothers.scheucher.taskbro;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DurationPickerDialog extends android.support.v7.app.AppCompatDialog {

    private static final String tag = "DurationPickerDialog";
    private static final int HOURS = 12;
    private static final int MINUTES = 60;
    private static final int DEGREE_HOURS = 360 / HOURS;
    private static final int DEGREE_MINUTES = 360 / MINUTES;

    private TextView hour_view;
    private TextView minute_view;
    private ImageView hour_rotateable;
    private ImageView minute_rotateable;

    private TextView duration_view;

    private Duration duration_result;
    private Duration duration_current;
    private Duration duration_max;
    private boolean duration_max_set = false;
    private Duration duration_min;
    private boolean duration_min_set = false;

    private double hour_touch_difference = -1000;
    private double minute_touch_difference = -1000;

    public DurationPickerDialog(Activity a, TextView duration_view, Duration duration_result) {
        super(a);
        this.duration_view = duration_view;
        this.duration_result = duration_result;
        this.duration_current = new Duration(duration_result.getDuration());
        this.duration_max_set = false;
        this.duration_min_set = false;

    }

    public void setMaxValue(int max_value_hours, int max_value_minutes) {
        if (!duration_max_set) {
            this.duration_max_set = true;
            this.duration_max = new Duration(0);
        }
        this.duration_max.setHours(max_value_hours);
        this.duration_max.setMinutes(max_value_minutes);
    }

    public void setMaxValue(int max_duration_in_minutes) {
        if (!duration_max_set) {
            this.duration_max_set = true;
            this.duration_max = new Duration(max_duration_in_minutes);
        } else {
            this.duration_max.setDuration(max_duration_in_minutes);
        }
    }
    public int getMaxValue() {
        if (duration_max_set) {
            return this.duration_max.getDuration();
        } else {
            return -1;
        }
    }

    public void removeMaxValue() {
        this.duration_max_set = false;
        this.duration_max.setDuration(0);
    }

    public void setMinValue(int min_duration_in_minutes) {
        if (!duration_min_set) {
            this.duration_min_set = true;
            this.duration_min = new Duration(min_duration_in_minutes);
        } else {
            this.duration_min.setDuration(min_duration_in_minutes);
        }
    }
    public void removeMinValue() {
        this.duration_min_set = false;
        this.duration_min.setDuration(0);
    }

    public void setDuration(int minutes) {
        this.duration_result.setDuration(minutes);
        this.duration_current.setDuration(minutes);
    }
    public int getDuration() {
        return this.duration_result.getDuration();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.duration_picker);

        hour_view = ((TextView)findViewById(R.id.hour));
        minute_view = ((TextView)findViewById(R.id.minute));
        final View hour_rotateable_overlay = ((View) findViewById(R.id.hour_rotateable_overlay));
        hour_rotateable = ((ImageView)findViewById(R.id.hour_rotateable));
        View minute_rotateable_overlay = ((View) findViewById(R.id.minute_rotateable_overlay));
        minute_rotateable = ((ImageView)findViewById(R.id.minute_rotateable));

        settingFieldsBecauseOfData();

        hour_rotateable_overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                double touch_degrees = Util.getDegreesFromTouchEvent(hour_rotateable, event.getX(), event.getY()) * (-1);
                if (touch_degrees < 0) { //make it positive
                    touch_degrees = touch_degrees + 360;
                }

                if (hour_touch_difference == -1000) {
                    hour_touch_difference = (touch_degrees - hour_rotateable.getRotation());
                    Log.d(tag, "touch_difference by pos degree =" + hour_touch_difference);
                } else {
                    //Set the hours
                    double correct_degree = touch_degrees - hour_touch_difference;
                    if (correct_degree < 0) {
                        correct_degree = correct_degree + 360;
                    } else if (correct_degree > 360) {
                        correct_degree = correct_degree % 360;
                    }

                    int current_hour = ((int) correct_degree / DEGREE_HOURS);

                    int diff = current_hour - (duration_current.getHours() % HOURS);
                    int distance = Math.min(HOURS - current_hour, current_hour) + Math.min(HOURS - (duration_current.getHours() % HOURS), duration_current.getHours() % HOURS);
                    if (-3 < diff && diff < 3) {
                        duration_current.incrementHours(diff);
                    } else if (distance < 3 && current_hour < 6) {
                        duration_current.incrementHours(distance);
                    } else if (distance < 3 && current_hour > 6) {
                        duration_current.incrementHours(distance * (-1));
                    }

                    if (duration_current.getHours() < 0) {
                        duration_current.setDuration(0);
                        settingFieldsBecauseOfData();
                    } else if (duration_min_set && duration_current.getDuration() < duration_min.getDuration()) {
                        duration_current.setDuration(duration_min.getDuration());
                        settingFieldsBecauseOfData();
                    } else if (duration_max_set && duration_current.getDuration() > duration_max.getDuration()) {
                        duration_current.setDuration(duration_max.getDuration());
                        settingFieldsBecauseOfData();
                    } else {
                        hour_view.setText(duration_current.getHours() + " Stunden");

                        //Set the rotation
                        if (correct_degree > 180) { //make it negative
                            correct_degree = correct_degree - 360;
                        }
                        hour_rotateable.setRotation((int) correct_degree);
                    }
                }
                return true;
            }
        });
        minute_rotateable_overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.d(tag, "x = " + event.getX() + " y = " + event.getY());
                double touch_degrees = Util.getDegreesFromTouchEvent(minute_rotateable, event.getX(), event.getY()) * (-1);
                if (touch_degrees < 0) { //make it positive
                    touch_degrees = touch_degrees + 360;
                }

                if (minute_touch_difference == -1000) {
                    minute_touch_difference = (touch_degrees - minute_rotateable.getRotation());
                    Log.d(tag, "minute_touch_difference =" + minute_touch_difference);
                } else {
                    double correct_degree = touch_degrees - minute_touch_difference;
                    if (correct_degree < 0) {
                        correct_degree = correct_degree + 360;
                    } else if (correct_degree > 360) {
                        correct_degree = correct_degree % 360;
                    }

                    int current_minute = ((int) correct_degree / DEGREE_MINUTES);

                    int diff = current_minute - (duration_current.getMinutes() % MINUTES);
                    int distance = Math.min(MINUTES - current_minute, current_minute) + Math.min(MINUTES - (duration_current.getMinutes() % MINUTES), duration_current.getMinutes() % MINUTES);
                    boolean changed_hours = false;
                    if (-15 < diff && diff < 15) {
                        changed_hours = duration_current.incrementMinutes(diff);
                    } else if (distance < 15 && current_minute < 30) {
                        changed_hours = duration_current.incrementMinutes(distance);
                    } else if (distance < 15 && current_minute > 30) {
                        changed_hours = duration_current.incrementMinutes(distance * (-1));
                    }


                    if (changed_hours) {
                        settingFieldsBecauseOfData();
                    } else {
                        minute_view.setText(duration_current.getMinutes() + " Minuten");
                        //Set the rotation
                        touch_degrees = touch_degrees - minute_touch_difference;
                        touch_degrees = touch_degrees % 360;
                        if (touch_degrees > 180) { //make it negative
                            touch_degrees = touch_degrees - 360;
                        }
                        minute_rotateable.setRotation((int) touch_degrees);
                    }

                    if (duration_max_set && duration_current.getDuration() > duration_max.getDuration()) {
                        duration_current.setDuration(duration_max.getDuration());
                        settingFieldsBecauseOfData();
                    } else if (duration_min_set && duration_current.getDuration() < duration_min.getDuration()) {
                        duration_current.setDuration(duration_min.getDuration());
                        settingFieldsBecauseOfData();
                    }
                }
                return true;
            }
        });

        ((Button)findViewById(R.id.negative_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "abbrechen clicked");
                cancel();
            }
        });
        ((Button)findViewById(R.id.positive_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "ok clicked");
                duration_result.setDuration(duration_current.getDuration());
                duration_view.setText(duration_result.description());
                dismiss();
            }
        });



    }

    private void settingFieldsBecauseOfData() {
        if (hour_rotateable != null && minute_rotateable != null
                && hour_view != null && minute_view != null) {
            hour_rotateable.setRotation((int) duration_current.getHours() * DEGREE_HOURS);
            minute_rotateable.setRotation((int) duration_current.getMinutes() * DEGREE_MINUTES);
            hour_view.setText(duration_current.getHours() + " Stunden");
            minute_view.setText(duration_current.getMinutes() + " Minuten");
        }
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        duration_current.setDuration(duration_result.getDuration());
        settingFieldsBecauseOfData();
        Log.d(tag, "ondismiss");
        super.setOnDismissListener(listener);
    }

    @Override
    public void setOnCancelListener(OnCancelListener listener) {
        duration_current.setDuration(duration_result.getDuration());
        settingFieldsBecauseOfData();
        Log.d(tag, "oncancel");
        super.setOnCancelListener(listener);
    }

    @Override
    protected void onStart() {
        duration_current.setDuration(duration_result.getDuration());
        if (this.duration_current.getDuration() == 0) {
            this.duration_current.setDuration(Settings.getStandardDurationTask().getDuration());
        }
        settingFieldsBecauseOfData();
        hour_touch_difference = -1000;
        minute_touch_difference = -1000;
        super.onStop();
    }

    public Duration getDuration_max() {
        return duration_max;
    }

    public void setDuration_max(Duration duration_max) {
        this.duration_max_set = true;
        this.duration_max = duration_max;
    }

    public Duration getDuration_min() {
        return duration_min;
    }

    public void setDuration_min(Duration duration_min) {
        this.duration_min_set = true;
        this.duration_min = duration_min;
    }


}