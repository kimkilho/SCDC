package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import kr.ac.snu.imlab.scdc.service.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.AlarmKeys;

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

//    Log.d(SCDCKeys.LogKeys.DEBUG, "OnAlarmReceiver.onReceive()/ received " +
//            "intent: " + intent.getDataString());
    // send notification, bundle intent with taskID
    NotificationHelper notification = new NotificationHelper();
    Bundle bundle = intent.getExtras();
    int labelId = bundle.getInt(AlarmKeys.EXTRA_LABEL_ID);
    Log.d(SCDCKeys.LogKeys.DEBUG, "OnAlarmReceiver.onReceive()/ received " +
                                  "labelId=" + labelId);
//    SharedPreferences prefs =
//      context.getSharedPreferences(Config.SCDC_PREFS, Context.MODE_PRIVATE);
    notification.sendBasicNotification(context, labelId); // send basic noti
    context.startService(new Intent(context, TaskButlerService.class));
      // start TaskButlerService
  }
}
