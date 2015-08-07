package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import kr.ac.snu.imlab.scdc.util.TimeUtil;

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
  public void sendBasicNotification(Context context, int alarmId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
                                     Context.MODE_PRIVATE);

    boolean vibrate = spHandler.getVibrateOnAlarm();
    int alarmInterval;
    int alarmUnits;

    if (spHandler.getHasFinalDateDue(alarmId)) {
      alarmInterval = Integer.parseInt(spHandler.getAlarmTime());
      alarmUnits = Calendar.MINUTE;
    } else {
      alarmInterval = Integer.parseInt(spHandler.getReminderTime());
      alarmUnits = Calendar.HOUR_OF_DAY;
    }

    Calendar nextReminder = GregorianCalendar.getInstance();
    nextReminder.add(alarmUnits, alarmInterval);
    Resources res = context.getResources();

    NotificationCompat.Builder builder;
    if (alarmId == Integer.parseInt(AlarmKeys.DEFAULT_GENERAL_ALARM_ID)) {
      builder = new NotificationCompat.Builder(context)
                      .setAutoCancel(true)
                      .setContentIntent(getPendingIntent(context, alarmId))
                      .setContentInfo("Urgent")
                      .setContentTitle(res.getString(R.string.app_title))
                      .setContentText(res.getString(R.string
                              .general_alarm_message))
                      .setDefaults(vibrate ? Notification.DEFAULT_ALL :
                        Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
                      .setSmallIcon(R.mipmap.ic_launcher)
                      .setTicker(spHandler.getLabelName(alarmId))
                      .setWhen(System.currentTimeMillis());
    } else {
      String message = String.format(res.getString(R.string
                          .label_alarm_message),
                          spHandler.getLabelName(alarmId),
                          TimeUtil.getElapsedTimeUntilNow(
                            spHandler.getStartLoggingTime(alarmId)));
      builder = new NotificationCompat.Builder(context)
                      .setAutoCancel(true)
                      .setContentIntent(getPendingIntent(context, alarmId))
                      .setContentInfo("Urgent")
                      .setContentTitle(res.getString(R.string.app_title))
                      .setContentText(message)
                      .setDefaults(vibrate ? Notification.DEFAULT_ALL :
                              Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
                      .setSmallIcon(R.mipmap.ic_launcher)
                      .setTicker(spHandler.getLabelName(alarmId))
                      .setWhen(System.currentTimeMillis());
    }

    @SuppressWarnings("deprecation")
    Notification notification = builder.getNotification();
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.notify(alarmId, notification);
  }

  /**
   * @description Basic Text Notification with Ongoing flag enabled for
   *   Task Butler, using NotificationCompat
   * @param context
   * @param alarmId
   * @return
   */
  public void sendPersistentNotification(Context context, int alarmId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
        Context.MODE_PRIVATE);
    NotificationCompat.Builder builder =
      new NotificationCompat.Builder(context)
        .setContentText(spHandler.getLabelName(alarmId))
        .setContentTitle(spHandler.getLabelName(alarmId))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setAutoCancel(true)
        .setContentIntent(getPendingIntent(context, alarmId))
        .setWhen(System.currentTimeMillis())
        .setOngoing(true)
        .setDefaults(Notification.DEFAULT_ALL);

    @SuppressWarnings("deprecation")
    Notification notification = builder.getNotification();
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.notify(alarmId, notification);
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, int id) {
    Intent intent = new Intent(context, LaunchActivity.class)
                          .putExtra(AlarmKeys.EXTRA_ALARM_ID, id);
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
   * @param alarmId
   */
  public void cancelNotification(Context context, int alarmId) {
    NotificationManager notificationMgr = getNotificationManager(context);
    notificationMgr.cancel(alarmId);
  }
}
