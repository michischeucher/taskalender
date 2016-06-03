package brothers.scheucher.taskalender;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ScrollViewScalable extends ScrollView {
    private static final String tag = "ScrollViewScalable";
    private LinearLayout height_container;
    private ScaleGestureDetector scale_detector;
    private static final float MAX_SCALING_FACTOR = 5.0f;
    private static final float MIN_SCALING_FACTOR = 0.3f;
    private int initial_height;

    public ScrollViewScalable(Context context, AttributeSet attrs) {
        super(context, attrs);
        scale_detector =  new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        super.dispatchTouchEvent(ev);
        return scale_detector.onTouchEvent(ev);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            TimeRank.scale_factor *= detector.getScaleFactor();
            TimeRank.scale_factor = Math.max(MIN_SCALING_FACTOR, Math.min(TimeRank.scale_factor, MAX_SCALING_FACTOR));
            scalingChanged();
            return true;
        }
    }

    private void scalingChanged() {
        Log.d(tag, "scalingChanging to " + TimeRank.scale_factor);
        if (this.height_container == null) {
            height_container = (LinearLayout)findViewById(R.id.height_container);
            initial_height = height_container.getLayoutParams().height;
            this.height_container.getLayoutParams().height = (int)(initial_height * TimeRank.scale_factor);
        }
        this.height_container.getLayoutParams().height = (int) (initial_height * TimeRank.scale_factor);
        this.height_container.requestLayout();
    }
}