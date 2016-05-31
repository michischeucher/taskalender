package brothers.scheucher.taskalender;

/**
 * Created by michi on 04.11.2015.
 */
public class PieChartElement {
    private String name;
    private int color;
    private Duration value;

    public PieChartElement(String name, int color, Duration value) {
        this.name = name;
        this.color = color;
        this.value = value;
    }

    public PieChartElement(int color, Duration value) {
        this.name = "";
        this.color = color;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getValue() {
        return value.getDuration();
    }

    public void setValue(Duration value) {
        this.value = value;
    }
}
