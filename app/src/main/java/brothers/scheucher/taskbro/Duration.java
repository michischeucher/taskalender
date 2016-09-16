package brothers.scheucher.taskbro;

public class Duration {
    private int hours;
    private int minutes;

    public Duration(int minutes) {
        this.hours = minutes / 60;
        this.minutes = minutes % 60;
    }

    public Duration(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    public void setDuration(int minutes) {
        if (minutes < 0) {
            this.hours = 0;
            this.minutes = 0;
            return;
        }
        this.hours = minutes / 60;
        this.minutes = minutes % 60;
    }
    public int getDuration() {
        return this.hours * 60 + this.minutes;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String description() {
        return Util.getFormattedDuration(getDuration());
    }

    public void incrementHours(int value_to_increment) {
        this.hours += value_to_increment;
    }

    //returns true if other fields (hour) have changed...
    public boolean incrementMinutes(int value_to_increment) {
        boolean ret = false;
        this.minutes += value_to_increment;
        while (this.minutes >= 60) {
            this.minutes -= 60;
            incrementHours(1);
            ret = true;
        }
        while (this.minutes < 0) {
            if (hours > 0) {
                incrementHours(-1);
                this.minutes += 60;
                ret = true;
            } else {
                this.minutes = 0;
            }
        }
        return ret;
    }
}
