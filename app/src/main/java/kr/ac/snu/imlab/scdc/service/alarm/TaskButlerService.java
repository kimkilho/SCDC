package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import kr.ac.snu.imlab.scdc.service.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.SharedPrefs;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.LabelKeys;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
  * Created by kilho on 15. 8. 3.
  */
 public class TaskButlerService extends WakefulIntentService {

  public TaskButlerService() {
    super("TaskButlerService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    SharedPrefsHandler spHandler =
            SharedPrefsHandler.getInstance(this, Config.SCDC_PREFS,
                                           Context.MODE_PRIVATE);
//    Log.d(SCDCKeys.LogKeys.DEBUG, "TaskButlerService.onHandleIntent()/ " +
//            "received intent=" + intent.getDataString());
    LabelAlarm alarm = new LabelAlarm();

    // Total number of labels
    int numLabels = spHandler.getNumLabels();

    for (int labelId = 0; labelId < numLabels; labelId++) {
//      Log.d(SCDCKeys.LogKeys.DEBUG, "OnAlarmReceiver.onReceive()/ received " +
//                     "data=" + labelName + ", " + labelId);
      Log.d(SCDCKeys.LogKeys.DEBUG, "TaskButlerService.onHandleIntent()/ " +
              "labelId=" + labelId + ", dateDue=" + spHandler.getDateDue
              (labelId) + ", System.currentTimeMillis()=" +
              System.currentTimeMillis());

      // Cancel existing alarm
      alarm.cancelAlarm(this, labelId);

      // procrastinator and reminder alarm
      if (spHandler.getIsPastDue(labelId)) {
        alarm.setReminder(this, labelId);
      }

      // handle repeat alarms
      if (spHandler.getIsRepeating(labelId) &&
          spHandler.getIsCompleted(labelId)) {
        labelId = alarm.setRepeatingAlarm(this, labelId);
      }

      // regular alarms
      if (spHandler.getIsCompleted(labelId) &&
          spHandler.getDateDue(labelId) >= System.currentTimeMillis()) {
        alarm.setAlarm(this, labelId);
      }
    }

    super.onHandleIntent(intent);
  }
 }
