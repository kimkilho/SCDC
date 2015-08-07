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
import kr.ac.snu.imlab.scdc.service.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.SharedPrefs;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.LogKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.AlarmKeys;
import kr.ac.snu.imlab.scdc.service.probe.LabelProbe;
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
   * @param alarmId  The ID of the alarm
   */
  public void cancelAlarm(Context context, int alarmId) {
    // cancel regular alarms
    PendingIntent pi = getPendingIntent(context, alarmId);
    AlarmManager alarmManager =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pi);
    pi.cancel();

    // cancel reminder alarm
    Intent intent =
      new Intent(context, OnAlarmReceiver.class)
            .putExtra(AlarmKeys.EXTRA_ALARM_ID, alarmId)
            .putExtra(LabelAlarm.ALARM_EXTRA, SharedPrefs.REMINDER_TIME);
    pi = PendingIntent.getBroadcast(context, alarmId, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pi);
    pi.cancel();

    // cancel procrastinator alarm
    intent =
      new Intent(context, OnAlarmReceiver.class)
            .putExtra(AlarmKeys.EXTRA_ALARM_ID, alarmId)
            .putExtra(LabelAlarm.ALARM_EXTRA, SharedPrefs.ALARM_TIME);
    pi = PendingIntent.getBroadcast(context, alarmId, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pi);
    pi.cancel();
  }

  /**
   * @description Use this call in activity code
   *   to cancel existing notifications
   * @param context
   * @param alarmId  The ID of the alarm
   */
  public void cancelNotification(Context context, int alarmId) {
    NotificationHelper cancel = new NotificationHelper();
    cancel.cancelNotification(context, alarmId);
  }

  /**
   * @description Sets DateDue field to the next repeat cycle,
   *   you still need to call setAlarm()
   * @param context
   * @param alarmId
   */
  public int setRepeatingAlarm(Context context, int alarmId) {
    int repeatType, alarmRepeatType, alarmRepeatInterval;
    long dateDue;

    if (alarmId == Integer.parseInt(AlarmKeys.DEFAULT_GENERAL_ALARM_ID)) {
      alarmRepeatType =
        Integer.parseInt(AlarmKeys.DEFAULT_GENERAL_ALARM_REPEAT_TYPE);
      dateDue = System.currentTimeMillis();
      alarmRepeatInterval =
        Integer.parseInt(AlarmKeys.DEFAULT_GENERAL_ALARM_REPEAT_INTERVAL);
    } else {
      LabelEntry labelEntry =
              new LabelEntry(alarmId, null, LabelProbe.class, null, true,
                      context, Config.SCDC_PREFS);
      alarmRepeatType = labelEntry.getRepeatType();
      dateDue = System.currentTimeMillis();
      alarmRepeatInterval = labelEntry.getRepeatInterval();
    }

    switch (alarmRepeatType) {
      case AlarmKeys.MINUTES:
        repeatType = Calendar.MINUTE;
        break;
      case AlarmKeys.HOURS:
        repeatType = Calendar.HOUR_OF_DAY;
        break;
      case AlarmKeys.DAYS:
        repeatType = Calendar.DAY_OF_YEAR;
        break;
      case AlarmKeys.WEEKS:
        repeatType = Calendar.WEEK_OF_YEAR;
        break;
      case AlarmKeys.MONTHS:
        repeatType = Calendar.MONTH;
        break;
      case AlarmKeys.YEARS:
        repeatType = Calendar.YEAR;
        break;
      default:
        repeatType = Calendar.DAY_OF_YEAR;
        break;
    }

    Calendar newDateDue = new GregorianCalendar();
    newDateDue.setTimeInMillis(dateDue);
    newDateDue.add(repeatType, alarmRepeatInterval);
      // set newDateDue by repeatInterval after current time

    AlarmManager am =
      (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, newDateDue.getTimeInMillis(),
           getPendingIntent(context, alarmId));

    Log.d(LogKeys.DEBUG, "LabelAlarm.setRepeatingAlarm()/ alarm set - " +
            "alarmId=" + alarmId + ", dateDue=" +
            newDateDue.getTime().toString());
    return alarmId;
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, int id) {
    Intent intent = new Intent(context, OnAlarmReceiver.class)
                          .putExtra(AlarmKeys.EXTRA_ALARM_ID, id);
    return PendingIntent.getBroadcast(context, id, intent,
                                      PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
