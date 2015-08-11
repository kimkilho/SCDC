package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
  * @author Kilho Kim
  * @description BroadcastReciever for android.intent.action.BOOT_COMPLETED
  *              passes all responsibility to TaskButlerService.
  * @reference https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/
  */
 public class OnBootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    WakefulIntentService.acquireStaticLock(context);
      // acquire a partial WakeLock
    context.startService(new Intent(context, AlarmButlerService.class));
      // start TaskButlerService
  }
 }
