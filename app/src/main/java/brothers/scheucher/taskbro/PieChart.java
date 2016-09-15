package brothers.scheucher.taskbro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by michi on 04.11.2015.
 */
public class PieChart extends View {

    private static final String tag = "PieChart";

    private ArrayList<PieChartElement> elements;

    public PieChart(Context context) {
        super(context);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(tag, "onDraw called");

        int centerX = (int)(canvas.getWidth() / 2);
        int centerY = (int)(canvas.getHeight() / 2);

        int margin = canvas.getWidth() / 15;
        int width = canvas.getWidth() - margin;
        int height = canvas.getHeight() - margin;

        //DRAWING PIE_CHART_ELEMENTS
        float sum_of_values = 0;
        for (PieChartElement pe : elements) {
            sum_of_values += pe.getValue();
        }
        float factor_to_normalize = ((float)360) / sum_of_values;

        float last_pie_chart_angle = -90;
        RectF rect_for_pie = new RectF(margin, margin, width, height);

        for (PieChartElement pe : elements) {
            Paint color = new Paint();
            color.setColor(0xFF000000 | pe.getColor());
            color.setStyle(Paint.Style.FILL);
            float pe_degree = pe.getValue() * factor_to_normalize;

            Path path = new Path();
            path.addArc(rect_for_pie, last_pie_chart_angle, pe_degree);
            canvas.drawArc(rect_for_pie, last_pie_chart_angle, pe_degree, true, color);

            last_pie_chart_angle += pe_degree;
            if (last_pie_chart_angle > 180) {
                last_pie_chart_angle -= 360;
            }
            Paint color_text = new Paint();
            color_text.setColor(Color.BLACK);
            int text_size = width / 25;
            color_text.setTextSize(text_size);
            canvas.drawTextOnPath(pe.getName(), path, 5, (int)(-text_size * 0.8), color_text);
        }

        //UMRAHMUNGSLINIE des PieCharts
        Paint color = new Paint();
        color.setColor(Color.GRAY);
        color.setStyle(Paint.Style.STROKE);
        color.setStrokeWidth(1);
        canvas.drawArc(rect_for_pie, 0, 360, false, color);


        //CENTER POINT
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, 8, paint);

        super.onDraw(canvas);
    }

    public ArrayList<PieChartElement> getElements() {
        return elements;
    }

    public void setElements(ArrayList<PieChartElement> elements) {
        Log.d(tag, "setElements: neu gesetzt... ");
        this.elements = elements;
        invalidate();
    }
    public void notifyChanges() {
        Log.d(tag, "notified...");
        invalidate();
    }
}
