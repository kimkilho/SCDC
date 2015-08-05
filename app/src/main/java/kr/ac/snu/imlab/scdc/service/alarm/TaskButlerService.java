package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.SharedPrefs;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.LabelKeys;
import kr.ac.snu.imlab.scdc.service.probe.LabelProbe;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
  * Created by kilho on 15. 8. 3.
  */
 public class TaskButlerService extends WakefulIntentService {

  private ArrayList<LabelEntry> labelEntries;

  public TaskButlerService() {
    super("TaskButlerService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    SharedPrefsHandler spHandler =
            SharedPrefsHandler.getInstance(this, Config.SCDC_PREFS,
                                           Context.MODE_PRIVATE);
    // Total number of labels
    int numLabels = spHandler.getNumLabels();

    labelEntries = new ArrayList<LabelEntry>(numLabels);
    for (int labelId = 0; labelId < labelEntries.size(); labelId++) {
      labelEntries.add(new LabelEntry(labelId, null,
                              LabelProbe.class, null, true,
                              this, Config.SCDC_PREFS));
    }

    LabelAlarm alarm = new LabelAlarm();

    for (int labelId = 0; labelId < labelEntries.size(); labelId++) {
      LabelEntry labelEntry = labelEntries.get(labelId);

      if (!labelEntry.isLogged()) {
//      Log.d(SCDCKeys.LogKeys.DEBUG, "OnAlarmReceiver.onReceive()/ received " +
//                     "data=" + labelName + ", " + labelId);
        Log.d(SCDCKeys.LogKeys.DEBUG, "TaskButlerService.onHandleIntent()/ " +
                "labelId=" + labelId + ", dateDue=" + spHandler.getDateDue
                (labelId) + ", System.currentTimeMillis()=" +
                System.currentTimeMillis());

        // Cancel existing alarm
        alarm.cancelAlarm(this, labelId);

        // procrastinator and reminder alarm
        if (labelEntry.isPastDue()) {
          alarm.setReminder(this, labelId);
        }

        // handle repeat alarms
        if (labelEntry.isRepeating() && labelEntry.isCompleted()) {
          labelId = alarm.setRepeatingAlarm(this, labelId);
        }

        // regular alarms
        if (labelEntry.isCompleted() &&
            labelEntry.getDateDue() >= System.currentTimeMillis()) {
          alarm.setAlarm(this, labelId);
        }
      }
    }

    super.onHandleIntent(intent);
  }
 }
