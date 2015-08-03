package kr.ac.snu.imlab.scdc.service.alarm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
  * @author Kilho Kim
  * @description Acquires a partial WakeLock,
  *   allows TaskButlerService to keep the CPU alive
  * @reference https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/
  */
public class WakefulIntentService extends IntentService {
  public static final String LOCK_NAME_STATIC =
    "kr.ac.snu.imlab.scdc.service.alarm.TaskButlerService.Static";
  public static final String LOCK_NAME_LOCAL =
    "kr.ac.snu.imlab.scdc.service.alarm.TaskButlerService.Local";
  private static PowerManager.WakeLock lockStatic = null;
  private PowerManager.WakeLock lockLocal = null;

  public WakefulIntentService(String name) {
    super(name);
  }

  /**
   * @description Acquire a partial static WakeLock,
   *   needs to call this within the class that calls startService()
   * @param context
   */
  public static void acquireStaticLock(Context context) {
    getLock(context).acquire();
  }

  synchronized private static PowerManager.WakeLock getLock(Context context) {
    if (lockStatic == null) {
      PowerManager mgr =
        (PowerManager)context.getSystemService(Context.POWER_SERVICE);
      lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                   LOCK_NAME_STATIC);
      lockStatic.setReferenceCounted(true);
    }
    return lockStatic;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    PowerManager mgr =
      (PowerManager)getSystemService(Context.POWER_SERVICE);
    lockLocal = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                LOCK_NAME_LOCAL);
    lockLocal.setReferenceCounted(true);
  }

  @Override
  public void onStart(Intent intent, final int startId) {
    lockLocal.acquire();
    super.onStart(intent, startId);
    getLock(this).release();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    lockLocal.release();
  }

}
