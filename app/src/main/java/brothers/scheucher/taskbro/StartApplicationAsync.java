package brothers.scheucher.taskbro;

import android.app.Activity;
import android.os.AsyncTask;

public class StartApplicationAsync extends AsyncTask<Object, Void, Void> {

    private static final String tag = "StartApplicationAsync";

    @Override
    protected Void doInBackground(Object... params) {
        Activity activity = (Activity) params[0];
        TaskBroContainer.startApplication(activity);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
