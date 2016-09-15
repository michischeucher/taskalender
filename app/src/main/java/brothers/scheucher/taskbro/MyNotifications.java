package brothers.scheucher.taskbro;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.GregorianCalendar;

/**
 * Created by michi on 12.07.2016.
 */
public class MyNotifications {

    private static final String tag = "MyNotification";

    public static void createNotification(Context context) {
        //NOTIFICATION testing:


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.notification_template_icon_bg);
        builder.setContentTitle("TaskBro");
        builder.setContentText("Zeit wird knapp... :(");
        builder.setCategory(context.ALARM_SERVICE);
        builder.setAutoCancel(true);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Bitte komm zu mir... Es geht sich nicht mehr aus! Arbeite endlich!!!!!!!"));
        Intent new_event = new Intent(context, AddEvent.class);
        PendingIntent pending_new_event = PendingIntent.getActivity(context, 0, new_event, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.abc_btn_check_to_on_mtrl_015, "Neuer Termin", pending_new_event);
        Intent new_task = new Intent(context, AddTask.class);
        PendingIntent pending_new_task = PendingIntent.getActivity(context, 0, new_task, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.abc_btn_check_to_on_mtrl_000, "Neue Aufgabe", pending_new_task);

        Intent intent_on_click = new Intent(context, SettingDay.class);
        PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent_on_click, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pending_intent);
        NotificationManager notification_manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        int notification_id = 10;
        notification_manager.notify(notification_id, builder.build());
    }

    public static void setAlarmNotification(Context context) {
        Log.d(tag, "setAlarmNotification");
        AlarmManager alarm_manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReciever.class);
        PendingIntent alarm_intent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarm_manager.set(AlarmManager.RTC, (new GregorianCalendar().getTimeInMillis()) + (1000 * 10), alarm_intent);
    }

    public static void createToastShort(String message) {
        Toast toast = Toast.makeText(TaskBroContainer.getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
