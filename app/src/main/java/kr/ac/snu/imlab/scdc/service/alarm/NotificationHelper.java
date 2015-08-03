package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Alarm;

/**
 * @author Kilho Kim
 * @description Creates notifications using NotificationCompat to allow for
 *  compatibility though different API levels
 * @reference https://github.com/CS-Worcester/TaskButler/blob/master/src/edu/
 *              worcester/cs499summer2012/service/NotificationHelper.java
 */
public class NotificationHelper {

  /**
   * @description Basic text notification for Task Butler,
   * using NotificationCompat
   */
  public void sendBasicNotification(Context context,
                                    String labelName, int labelId) {
    SharedPreferences prefs =
           context.getSharedPreferences(Config.SCDC_LABEL_PREFS,
                                        Context.MODE_PRIVATE);
    boolean vibrate = prefs.getBoolean(Alarm.VIBRATE_ON_ALARM, true);
    int alarmInterval;
    int alarmUnits;

    alarmInterval =
      Integer.parseInt(prefs.getString(Alarm.ALARM_TIME,
                                       Alarm.DEFAULT_ALARM_TIME));
    alarmUnits = Calendar.MINUTE;

    Calendar nextReminder = GregorianCalendar.getInstance();
    nextReminder.add(alarmUnits, alarmInterval);

    NotificationCompat.Builder builder =
      new NotificationCompat.Builder(context)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context, labelName, labelId))
            .setContentTitle(labelName)
            .setContentText(DateFormat.format("'Next reminder at: h:mmaa",
                                        nextReminder))
            .setDefaults(vibrate ?  Notification.DEFAULT_ALL :
                    Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
            .setTicker(labelName)
            .setWhen(System.currentTimeMillis());

    @SuppressWarnings("deprecation")
    Notification notification = builder.getNotification();
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.notify(labelId, notification);
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, String name, int id) {
    Intent intent = new Intent(context, LaunchActivity.class)
                          .putExtra(Alarm.EXTRA_LABEL_NAME, name)
                          .putExtra(Alarm.EXTRA_LABEL_ID, id);
    return PendingIntent.getActivity(context, id, intent, 0);
  }

  // get a NotificationManager
  NotificationManager getNotificationManager(Context context) {
    return (NotificationManager)context
              .getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public void cancelNotification(Context context, int labelId) {
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.cancel(labelId);
  }
}
