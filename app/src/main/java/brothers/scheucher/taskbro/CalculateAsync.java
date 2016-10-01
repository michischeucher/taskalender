package brothers.scheucher.taskbro;

import android.app.Activity;
import android.os.AsyncTask;

public class CalculateAsync extends AsyncTask<Object, Void, Void> {


    private static final String tag = "CalculateAsync";

    @Override
    protected Void doInBackground(Object... params) {
        Activity activity = (Activity)params[0];
        TaskBroContainer.calculateDays(activity);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Calender.notifyChanges();
        MainActivity.notifyChanges();
        PotentialActivity.notifyChanges();
    }
}
