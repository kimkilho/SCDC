package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Alarm;

/**
  * @author Kilho Kim
  * @description Wrapper class for setRepeatingAlarm(), cancelAlarm(),
  *   setOnetimeAlarm()
  * @reference https://github.com/CS-Worcester/TaskButler/blob/master/src/edu/worcester/cs499summer2012/service/TaskAlarm.java
  */
public class LabelAlarm {

  public static final String ALARM_EXTRA =
    "kr.ac.snu.imlab.scdc.service.alarm.LabelAlarm";
  public static final int REPEATING_ALARM = 1;
  public static final int PROCRASTINATOR_ALARM = 2;

  /**
   * @description Cancel alarm using the alarm id,
   *   PendingIntent is created using the Alarm id
   * @param context
   * @param id  The ID of the label
   */
  public void cancelAlarm(Context context, String labelName, int labelId) {
    // cancel regular alarms
    PendingIntent pi = getPendingIntent(context, labelName, labelId);
    AlarmManager alarmManager =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pi);
    pi.cancel();

//    // cancel reminder alarm
//    Intent intent =
//      new Intent(context, OnAlarmReceiver.class)
//            .putExtra(Alarm.EXTRA_LABEL_ID, id)
//            .putExtra(LabelAlarm.ALARM_EXTRA, Alarm.REMINDER_TIME);
//    pi = PendingIntent.getBroadcast(context, id, intent,
//            PendingIntent.FLAG_UPDATE_CURRENT);
//    alarmManager.cancel(pi);
//    pi.cancel();
//
//    // cancel procrastinator alarm
//    intent =
//      new Intent(context, OnAlarmReceiver.class)
//            .putExtra(Alarm.EXTRA_LABEL_ID, id)
//            .putExtra(LabelAlarm.ALARM_EXTRA, Alarm.ALARM_TIME);
//    pi = PendingIntent.getBroadcast(context, id, intent,
//            PendingIntent.FLAG_UPDATE_CURRENT);
//    alarmManager.cancel(pi);
//    pi.cancel();
  }

  /**
   * @description Use this call in activity code
   *   to cancel existing notifications
   * @param context
   * @param id  The ID of the label
   */
  public void cancelNotification(Context context, int labelId) {
    NotificationHelper cancel = new NotificationHelper();
    cancel.cancelNotification(context, labelId);
  }

  /**
   * @description Set a one-time alarm using the labelID
   * @param context
   * @param dateDue
   * @param labelId
   */
  public void setAlarm(Context context, long dateDue,
                       String name, int labelId) {
    AlarmManager am =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    Log.d(SCDCKeys.LogKeys.DEBUG, "LabelAlarm.setAlarm()/ alarm set");
    am.set(AlarmManager.RTC_WAKEUP, dateDue,
            getPendingIntent(context, name, labelId));
  }

  /**
   * @description Sets DateDue field to the next repeat cycle,
   *   you still need to call setAlarm()
   * @param context
   * @param id
   */
  public void setRepeatingAlarm(Context context, int id) {
    // TODO
  }

  /**
   * @description Reads preferences, and schedule a procrastinator alarm
   *   for a past due label.
   * @param context
   * @param id
   */
  public void setProcrastinatorAlarm(Context context, int id) {
    // TODO
  }

  /**
   * @description Reads preferences, and schedule a reminder alarm
   *   for a past due label.
   * @param context
   * @param id
   */
  public void setReminder(Context context, int id) {
    // TODO
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, String name, int id) {
    Intent intent = new Intent(context, OnAlarmReceiver.class)
                          .putExtra(Alarm.EXTRA_LABEL_NAME, name)
                          .putExtra(Alarm.EXTRA_LABEL_ID, id);
    return PendingIntent.getBroadcast(context, id, intent,
                                      PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
