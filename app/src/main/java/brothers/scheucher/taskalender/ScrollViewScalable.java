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
    private final ScrollViewScalable view;
    private LinearLayout height_container;
    private ScaleGestureDetector scale_detector;
    private static final float MAX_SCALING_FACTOR = 5.0f;
    private static final float MIN_SCALING_FACTOR = 0.3f;
    private int initial_height;


    public ScrollViewScalable(Context context, AttributeSet attrs) {
        super(context, attrs);
        scale_detector =  new ScaleGestureDetector(context, new ScaleListener());

        view = this;


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        super.dispatchTouchEvent(ev);
        return scale_detector.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        Log.d(tag, "onscrollchanged l=" + l + " t=" + t);
        Calender.setScrollPosition(t);
        Calender.notifyScalingOrScrollingChanged();
    }




    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {


        private int scroll_pos_begin;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scroll_pos_begin = Calender.getScrollPosition();
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            TimeRank.scale_factor *= detector.getScaleFactor();
            TimeRank.scale_factor = Math.max(MIN_SCALING_FACTOR, Math.min(TimeRank.scale_factor, MAX_SCALING_FACTOR));
//Calender.setScrollPosition((int)(scroll_pos_begin * TimeRank.scale_factor));
//            Calender.notifyScalingOrScrollingChanged();

            int y = (int)detector.getFocusY();

            scrollTo(0,(int)(scroll_pos_begin * TimeRank.scale_factor));
            scalingChanged();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            TimeRank.saveScaleFactor();
        }
    }

    public void scalingChanged() {
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