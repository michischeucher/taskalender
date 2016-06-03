package brothers.scheucher.taskalender;

import android.os.AsyncTask;
import android.util.Log;

public class CalculateAsync extends AsyncTask<Void, Void, Void> {


    private static final String tag = "CalculateAsync";

    @Override
    protected Void doInBackground(Void... params) {
        TimeRank.calculateDays();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(tag, "CalculateAsync finished!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Calender.notifyChanges();
    }
}
