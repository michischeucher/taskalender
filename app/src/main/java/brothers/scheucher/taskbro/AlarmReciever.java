package brothers.scheucher.taskbro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by michi on 22.07.2016.
 */
public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("onReceive", "ladskjflsakjdflskjdflskjdfslkjdflasdf");
        Toast.makeText(context, "OnReceive alarm test", Toast.LENGTH_SHORT).show();
        MyNotifications.createNotification(TaskBroContainer.getContext());
    }
}
