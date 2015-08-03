package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.SCDCKeys;

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
  public void cancelAlarm(Context context, int id) {
    // cancel regular alarms
    PendingIntent pi = getPendingIntent(context, id);
    AlarmManager alarmManager =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pi);
    pi.cancel();

    // cancel reminder alarm
    Intent intent =
      new Intent(context, OnAlarmReceiver.class)
            .putExtra(SCDCKeys.Alarm.EXTRA_LABEL_ID, id)
            .putExtra(LabelAlarm.ALARM_EXTRA, SCDCKeys.Alarm.REMINDER_TIME);
    pi = PendingIntent.getBroadcast(context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pi);
    pi.cancel();

    // cancel procrastinator alarm
    intent =
      new Intent(context, OnAlarmReceiver.class)
            .putExtra(SCDCKeys.Alarm.EXTRA_LABEL_ID, id)
            .putExtra(LabelAlarm.ALARM_EXTRA, SCDCKeys.Alarm.ALARM_TIME);
    pi = PendingIntent.getBroadcast(context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pi);
    pi.cancel();
  }

  /**
   * @description Use this call in activity code
   *   to cancel existing notifications
   * @param context
   * @param id  The ID of the label
   */
  public void cancelNotification(Context context, int id) {
    NotificationHelper cancel = new NotificationHelper();
    cancel.cancelNotification(context, id);
  }

  /**
   * @description Set a one-time alarm using the labelID
   * @param context
   * @param labelEntry
   */
  public void setAlarm(Context context, LabelEntry labelEntry) {
       AlarmManager am =
         (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
       am.set(AlarmManager.RTC_WAKEUP, labelEntry.getDateDue(),
               getPendingIntent(context, labelEntry.getId()));
       // am.set(int type, long triggerAtMillis, PendingIntent operation)
//        type 	One of ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC,
// or RTC_WAKEUP.
//        triggerAtMillis 	time in milliseconds that the alarm should go off, using the appropriate clock (depending on the alarm type).
//        operation 	Action to perform when the alarm goes off; typically comes from IntentSender.getBroadcast().
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
  PendingIntent getPendingIntent(Context context, int id) {
    Intent intent = new Intent(context, OnAlarmReceiver.class)
                          .putExtra(SCDCKeys.Alarm.EXTRA_LABEL_ID, id);
    return PendingIntent.getBroadcast(context, id, intent,
                                      PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
