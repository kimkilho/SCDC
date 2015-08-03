package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;

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
  public void sendBasicNotification(Context context) {
    SharedPreferences prefs =
      PreferenceManager.getDefaultSharedPreferences(context);
    boolean vibrate = prefs.getBoolean(VIBRATE_ON_ALARM, true);
    int alarmInterval;
    int alarmUnits;

    alarmInterval =
      Integer.parseInt(prefs.getString(ALARM_TIME, DEFAULT_ALARM_TIME))
    alarmUnits = Calendar.MINUTE;

    Calendar nextReminder = GregorianCalendar.getInstance();
    nextReminder.add(alarmUnits, alarmInterval);

    NotificationCompat.Builder builder =
      new NotificationCompat.Builder(context)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context, ));

    @SuppressWarnings("deprecation")
    Notification notification = builder.getNotification();
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.notify();


  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, int id) {
    // Intent intent = new Intent(context, ).putExtra(EXTRA_TASK_ID, id);
    return PendingIntent.getActivity(context, id, intent, 0);
  }

  // get a NotificationManager
  NotificationManager getNotificationManager(Context context) {
    return (NotificationManager)context
              .getSystemService(Context.NOTIFICATION_SERVICE);
  }
}
