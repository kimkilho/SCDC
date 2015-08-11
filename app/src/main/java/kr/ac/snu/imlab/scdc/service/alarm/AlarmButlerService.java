package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.AlarmKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.probe.LabelProbe;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
  * Created by kilho on 15. 8. 3.
  */
 public class AlarmButlerService extends WakefulIntentService {

  private ArrayList<LabelEntry> labelEntries;

  public AlarmButlerService() {
    super("AlarmButlerService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    SharedPrefsHandler spHandler =
            SharedPrefsHandler.getInstance(this, Config.SCDC_PREFS,
                    Context.MODE_PRIVATE);

    boolean isReminderRunning = spHandler.isReminderRunning();
    if (isReminderRunning) {
      Log.d(SCDCKeys.LogKeys.DEBUG, "AlarmButlerService.onHandleIntent()/ " +
              "AlarmButlerService running...");
      // Total number of labels
      int numLabels = spHandler.getNumLabels();

      labelEntries = new ArrayList<LabelEntry>();
      for (int labelId = 0; labelId < numLabels; labelId++) {
        labelEntries.add(new LabelEntry(labelId, null,
                LabelProbe.class, null, true,
                this, Config.SCDC_PREFS));
      }

      boolean isNotLogged = true;

      for (int labelId = 0; labelId < labelEntries.size(); labelId++) {
        LabelEntry labelEntry = labelEntries.get(labelId);

        LabelAlarm alarm = new LabelAlarm();

//      Log.d(SCDCKeys.LogKeys.DEBUG, "OnAlarmReceiver.onReceive()/ received " +
//                     "data=" + labelName + ", " + labelId);

        // Cancel existing alarm
        alarm.cancelAlarm(this, labelId);

        // procrastinator and reminder alarm
//        if (labelEntry.isPastDue()) {
//          alarm.setReminder(this, labelId);
//          Log.d(SCDCKeys.LogKeys.DEBUG, "AlarmButlerService.onHandleIntent()/
// " +
//                 "alarm.setReminder(" + labelId + ")");
//        }

        // handle repeat alarms
        if (labelEntry.isLogged() && labelEntry.isRepeating()) {
          isNotLogged = false;
          labelId = alarm.setRepeatingAlarm(this, labelId);
          Log.d(SCDCKeys.LogKeys.DEBUG, "AlarmButlerService.onHandleIntent()/ " +
                  "alarm.setRepeatingAlarm(" + labelId + ")");
        }

        // regular alarms
//        if (labelEntry.isCompleted() &&
//            labelEntry.getDateDue() >= System.currentTimeMillis()) {
//          alarm.setAlarm(this, labelId);
//          Log.d(SCDCKeys.LogKeys.DEBUG, "AlarmButlerService.onHandleIntent()/
// " +
//                  "alarm.setAlarm(" + labelId + ")");
//        }
      }

      if (isNotLogged) {
        LabelAlarm alarm = new LabelAlarm();
        int alarmId = Integer.parseInt(AlarmKeys.DEFAULT_GENERAL_ALARM_ID);
        alarm.cancelAlarm(this, alarmId);
        alarmId = alarm.setRepeatingAlarm(this, alarmId);
        Log.d(SCDCKeys.LogKeys.DEBUG, "AlarmButlerService.onHandleIntent()/ " +
                "alarm.setRepeatingAlarm(" + alarmId + ")");
      }
    }

    super.onHandleIntent(intent);
  }
 }
