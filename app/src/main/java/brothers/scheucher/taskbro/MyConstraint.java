package brothers.scheucher.taskbro;


import java.util.GregorianCalendar;

/**
 * Created by michi on 12.11.2015.
 */
public class MyConstraint {
    private String weekday;
    private GregorianCalendar date;

    public MyConstraint() {
        this.weekday = "";
        this.date = null;
    }
    public MyConstraint(GregorianCalendar date) {
        this.weekday = "";
        this.date = date;
    }
    public MyConstraint(String day_of_week_short) {
        this.weekday = day_of_week_short;
        this.date = null;
    }

    //checks if the constraint is relevant for that day
    // returns -1 if not relevant
    // returns higher value for more relevant constraint...
    public int howRelevant(GregorianCalendar date_to_check) {
        int ret = 1;
        if (this.date != null && Util.isSameDate(date_to_check, this.date)) {
            return 365;
        }
        if (this.weekday.equals(Util.getDayOfWeekShort(date_to_check))) {
            return 365 / 7;
        }

        return ret;
    }
}
