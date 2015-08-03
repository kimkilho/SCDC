package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import kr.ac.snu.imlab.scdc.service.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;

/**
  * @author Kilho Kim
  * @description BroadcastReceiver for Alarms,
  *   displays notifications as it receives alarm
  *   and then starts TaskButlerService to
  *   update alarm schedule with AlarmManager
  * @reference https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/
  */
public class OnAlarmReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    WakefulIntentService.acquireStaticLock(context);
      // acquire a partial WakeLock

    // send notification, bundle intent with taskID
    NotificationHelper notification = new NotificationHelper();
    Bundle bundle = intent.getExtras();
    int id = bundle.getInt(SCDCKeys.Alarm.EXTRA_LABEL_ID);
    notification.sendBasicNotification(context, );
    context.startService(new Intent(context, TaskButlerService.class));
      // start TaskButlerService
  }
}
