package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import kr.ac.snu.imlab.scdc.service.core.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.AlarmKeys;

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
    int alarmId = bundle.getInt(AlarmKeys.EXTRA_ALARM_ID);
    Log.d(SCDCKeys.LogKeys.DEBUG, "OnAlarmReceiver.onReceive()/ received " +
                                  "alarmId=" + alarmId);
    notification.sendBasicNotification(context, alarmId);
      // send basic notification
    context.startService(new Intent(context, AlarmButlerService.class));
      // start TaskButlerService
  }
}
