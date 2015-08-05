package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.SharedPrefs;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.LogKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Alarm;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

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
   * @param labelId  The ID of the label
   */
  public void cancelAlarm(Context context, int labelId) {
    // cancel regular alarms
    PendingIntent pi = getPendingIntent(context, labelId);
    AlarmManager alarmManager =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pi);
    pi.cancel();

    // cancel reminder alarm
    Intent intent =
      new Intent(context, OnAlarmReceiver.class)
            .putExtra(Alarm.EXTRA_LABEL_ID, labelId)
            .putExtra(LabelAlarm.ALARM_EXTRA, SharedPrefs.REMINDER_TIME);
    pi = PendingIntent.getBroadcast(context, labelId, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pi);
    pi.cancel();

    // cancel procrastinator alarm
    intent =
      new Intent(context, OnAlarmReceiver.class)
            .putExtra(Alarm.EXTRA_LABEL_ID, labelId)
            .putExtra(LabelAlarm.ALARM_EXTRA, SharedPrefs.ALARM_TIME);
    pi = PendingIntent.getBroadcast(context, labelId, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pi);
    pi.cancel();
  }

  /**
   * @description Use this call in activity code
   *   to cancel existing notifications
   * @param context
   * @param labelId  The ID of the label
   */
  public void cancelNotification(Context context, int labelId) {
    NotificationHelper cancel = new NotificationHelper();
    cancel.cancelNotification(context, labelId);
  }

  /**
   * @description Set a one-time alarm using the labelID
   * @param context
   * @param labelId
   */
  public void setAlarm(Context context, int labelId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
              Context.MODE_PRIVATE);
    long dateDue = spHandler.getDateDue(labelId);
    AlarmManager am =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, dateDue,
            getPendingIntent(context, labelId));
    Log.d(LogKeys.DEBUG, "LabelAlarm.setAlarm()/ alarm set - " +
            "labelId=" + labelId + ", dateDue=" + dateDue);
  }

  /**
   * @description Sets DateDue field to the next repeat cycle,
   *   you still need to call setAlarm()
   * @param context
   * @param labelId
   */
  public int setRepeatingAlarm(Context context, int labelId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
                                     Context.MODE_PRIVATE);
    long dateDue = spHandler.getDateDue(labelId);
    Calendar newDateDue = new GregorianCalendar();
    newDateDue.setTimeInMillis(dateDue);
    int repeatType;

    switch (spHandler.getRepeatType(labelId)) {
      case Alarm.MINUTES:
        repeatType = Calendar.MINUTE;
        break;
      case Alarm.HOURS:
        repeatType = Calendar.HOUR_OF_DAY;
        break;
      case Alarm.DAYS:
        repeatType = Calendar.DAY_OF_YEAR;
        break;
      case Alarm.WEEKS:
        repeatType = Calendar.WEEK_OF_YEAR;
        break;
      case Alarm.MONTHS:
        repeatType = Calendar.MONTH;
        break;
      case Alarm.YEARS:
        repeatType = Calendar.YEAR;
        break;
      default:
        repeatType = Calendar.DAY_OF_YEAR;
        break;
    }

    // Due date is behind current time, label alarm was finished late
    if (newDateDue.getTimeInMillis() <= System.currentTimeMillis()) {
      while (newDateDue.getTimeInMillis() <= System.currentTimeMillis()) {
        newDateDue.add(repeatType, spHandler.getRepeatInterval(labelId));
      }
    } else {
      // Due date was ahead of current time, label alarm was finished early
      newDateDue.add(repeatType, spHandler.getRepeatInterval(labelId));

      AlarmManager am =
        (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
      am.set(AlarmManager.RTC_WAKEUP, newDateDue.getTimeInMillis(),
             getPendingIntent(context, labelId));

      Log.d(LogKeys.DEBUG, "LabelAlarm.setAlarm()/ alarm set - " +
              "labelId=" + labelId + ", dateDue=" + dateDue);
      return labelId;
    }

    spHandler.setDateDue(labelId, newDateDue.getTimeInMillis());
    spHandler.setIsCompleted(labelId, false);

    return labelId;
  }

  /**
   * @description Reads preferences, and schedule a procrastinator alarm
   *   for a past due label.
   * @param context
   * @param labelId
   */
  public void setProcrastinatorAlarm(Context context, int labelId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
        Context.MODE_PRIVATE);
    String strAlarm = spHandler.getAlarmTime();
    Calendar cal = Calendar.getInstance();
    int iAlarm = Integer.parseInt(strAlarm);
    cal.add(Calendar.MINUTE, iAlarm);
    long lAlarm = cal.getTimeInMillis();

    Intent intent = new Intent(context, OnAlarmReceiver.class)
                          .putExtra(Alarm.EXTRA_LABEL_ID, labelId)
                          .putExtra(Alarm.ALARM_EXTRA, SharedPrefs.ALARM_TIME);

    AlarmManager am =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, lAlarm,
           PendingIntent.getBroadcast(context, labelId, intent,
             PendingIntent.FLAG_UPDATE_CURRENT));
  }

  /**
   * @description Reads preferences, and schedule a reminder alarm
   *   for a past due label.
   * @param context
   * @param labelId
   */
  public void setReminder(Context context, int labelId) {
    SharedPrefsHandler spHandler =
      SharedPrefsHandler.getInstance(context, Config.SCDC_PREFS,
        Context.MODE_PRIVATE);

    long dateDue = spHandler.getDateDue(labelId);
    Calendar dueCal = new GregorianCalendar();
    dueCal.setTimeInMillis(dateDue);
    boolean isProcrastinator = spHandler.getHasFinalDateDue(labelId);

    String strReminder;
    int iInterval;

    if (isProcrastinator) {
      // Procrastinator alarm
      strReminder = spHandler.getAlarmTime();
      iInterval = Calendar.MINUTE;
    } else {
      // Regular alarm
      strReminder = spHandler.getReminderTime();
      iInterval = Calendar.HOUR;
    }

    int iReminder = Integer.parseInt(strReminder);

    do {
      dueCal.add(iInterval, iReminder);
    } while (dueCal.getTimeInMillis() < System.currentTimeMillis());

    Intent intent = new Intent(context, OnAlarmReceiver.class)
                          .putExtra(Alarm.EXTRA_LABEL_ID, labelId)
                          .putExtra(Alarm.ALARM_EXTRA, isProcrastinator ?
                            SharedPrefs.ALARM_TIME : SharedPrefs.REMINDER_TIME);

    AlarmManager am =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, dueCal.getTimeInMillis(),
      PendingIntent.getBroadcast(context, labelId, intent,
                                 PendingIntent.FLAG_UPDATE_CURRENT));
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, int id) {
    Intent intent = new Intent(context, OnAlarmReceiver.class)
                          .putExtra(Alarm.EXTRA_LABEL_ID, id);
    return PendingIntent.getBroadcast(context, id, intent,
                                      PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
