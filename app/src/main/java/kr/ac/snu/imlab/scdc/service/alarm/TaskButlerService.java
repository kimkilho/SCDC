package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import kr.ac.snu.imlab.scdc.service.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.SharedPrefs;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.LabelKeys;

/**
  * Created by kilho on 15. 8. 3.
  */
 public class TaskButlerService extends WakefulIntentService {

  public TaskButlerService() {
    super("TaskButlerService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(SCDCKeys.LogKeys.DEBUG, "TaskButlerService.onHandleIntent()/ " +
            "received intent=" + intent.getDataString());
    LabelAlarm alarm = new LabelAlarm();
    SharedPreferences prefs =
      getSharedPreferences(Config.SCDC_LABEL_PREFS, Context.MODE_PRIVATE);

    // Total number of labels
    int numLabels = prefs.getInt(SharedPrefs.NUM_LABELS,
                               Integer.parseInt(LabelKeys.DEFAULT_NUM_LABELS));

    for (int labelId = 0; labelId < numLabels; labelId++) {
      String labelName =
        prefs.getString(SharedPrefs.LABEL_NAME_PREFIX +
                        String.valueOf(labelId), null);
      long dateDue =
        prefs.getLong(SharedPrefs.DATE_DUE_PREFIX +
                      String.valueOf(labelId), -1L);

//      Log.d(SCDCKeys.LogKeys.DEBUG, "OnAlarmReceiver.onReceive()/ received " +
//                     "data=" + labelName + ", " + labelId);

      // Cancel existing alarm
      alarm.cancelAlarm(this, labelName, labelId);

      // TODO: procrastinator and reminder alarm

      // TODO: handle repeat alarms

      // regular alarms
      if (dateDue >= System.currentTimeMillis()) {
        alarm.setAlarm(this, dateDue, labelName, labelId);
      }
    }

    super.onHandleIntent(intent);
  }
 }
