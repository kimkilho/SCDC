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
import kr.ac.snu.imlab.scdc.service.SCDCKeys.AlarmKeys;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

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
  public void sendBasicNotification(Context context, int labelId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
                                     Context.MODE_PRIVATE);
    boolean vibrate = spHandler.getVibrateOnAlarm();
    int alarmInterval;
    int alarmUnits;

    if (spHandler.getHasFinalDateDue(labelId)) {
      alarmInterval = Integer.parseInt(spHandler.getAlarmTime());
      alarmUnits = Calendar.MINUTE;
    } else {
      alarmInterval = Integer.parseInt(spHandler.getReminderTime());
      alarmUnits = Calendar.HOUR_OF_DAY;
    }

    Calendar nextReminder = GregorianCalendar.getInstance();
    nextReminder.add(alarmUnits, alarmInterval);

    NotificationCompat.Builder builder =
      new NotificationCompat.Builder(context)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context, labelId))
            .setContentInfo("Urgent")
            .setContentTitle(spHandler.getLabelName(labelId))
            .setContentText(DateFormat.format("'Next reminder at: h:mmaa",
                              nextReminder))
            .setDefaults(vibrate ? Notification.DEFAULT_ALL :
                      Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(spHandler.getLabelName(labelId))
            .setWhen(System.currentTimeMillis());

    @SuppressWarnings("deprecation")
    Notification notification = builder.getNotification();
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.notify(labelId, notification);
  }

  /**
   * @description Basic Text Notification with Ongoing flag enabled for
   *   Task Butler, using NotificationCompat
   * @param context
   * @param labelId
   * @return
   */
  public void sendPersistentNotification(Context context, int labelId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
        Context.MODE_PRIVATE);
    NotificationCompat.Builder builder =
      new NotificationCompat.Builder(context)
        .setContentText(spHandler.getLabelName(labelId))
        .setContentTitle(spHandler.getLabelName(labelId))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setAutoCancel(true)
        .setContentIntent(getPendingIntent(context, labelId))
        .setWhen(System.currentTimeMillis())
        .setOngoing(true)
        .setDefaults(Notification.DEFAULT_ALL);

    @SuppressWarnings("deprecation")
    Notification notification = builder.getNotification();
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.notify(labelId, notification);
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, int id) {
    Intent intent = new Intent(context, LaunchActivity.class)
                          .putExtra(AlarmKeys.EXTRA_LABEL_ID, id);
    return PendingIntent.getActivity(context, id, intent, 0);
  }

  // get a NotificationManager
  NotificationManager getNotificationManager(Context context) {
    return (NotificationManager)context
              .getSystemService(Context.NOTIFICATION_SERVICE);
  }

  /**
   * @description Cancels an existing notification, if user modified the task.
   *   Make the actual call from LabelAlarm.cancelNotification(Context, int)
   * @param context
   * @param labelId
   */
  public void cancelNotification(Context context, int labelId) {
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.cancel(labelId);
  }
}
