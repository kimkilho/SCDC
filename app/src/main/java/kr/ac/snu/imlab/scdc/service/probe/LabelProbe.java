package kr.ac.snu.imlab.scdc.service.probe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.probe.Probe.Base;
import edu.mit.media.funf.probe.Probe.ContinuousProbe;
import edu.mit.media.funf.probe.Probe.DisplayName;
import edu.mit.media.funf.probe.Probe.Description;
import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LabelKeys;
import edu.mit.media.funf.time.TimeUtil;

/**
 * Created by kilho on 2015. 6. 28..
 */
@DisplayName("Label Log Probe")
@Description("Records label for all time")
@Schedule.DefaultSchedule(interval=0, duration=0, opportunistic=true)
public class LabelProbe extends Base implements ContinuousProbe, LabelKeys {

    private BroadcastReceiver labelReceiver;
    Map<String, Boolean> labels;

    /**
     * Called when the probe switches from the disabled to the enabled
     * state. This is where any passive or opportunistic listeners should be
     * configured. An enabled probe should not keep a wake lock. If you need
     * the device to stay awake consider implementing a StartableProbe, and
     * using the onStart method.
     */
    @Override
    protected void onEnable() {
        labels = new HashMap<String, Boolean>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LABEL_LOG);

        labelReceiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            Log.w("DEBUG", "LabelProbe/ Received broadcast");
            JsonObject data = new JsonObject();

            String[] labelNames = LaunchActivity.labelNames;
            for (int i = 0; i < labelNames.length; i++) {
              labels.put(labelNames[i],
                         intent.getBooleanExtra(labelNames[i], false));
            }
            // Log.w("DEBUG", "SLEEP_LABEL=" + labels.get(LabelKeys.SLEEP_LABEL) + ", IN_CLASS_LABEL=" + labels.get(LabelKeys.IN_CLASS_LABEL));
            for (String key : labels.keySet()) {
              data.addProperty(key, labels.get(key));
            }
            Log.w("DEBUG", "LabelProbe/ JsonObject data=" + data.toString());
            sendData(data);
          }
        };
        getContext().registerReceiver(labelReceiver, filter);

        // super.onEnable();
//        listener = new LabelListener();
//        getGson().fromJson(DEFAULT_CONFIG, LabelProbe.class)
//                .registerPassiveListener
//                (listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        getGson().fromJson(DEFAULT_CONFIG, LabelProbe.class).registerListener
//                (listener);
        // sendData(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        getGson().fromJson(DEFAULT_CONFIG, LabelProbe.class).unregisterListener
//                (listener);
    }

    @Override
    protected void onDisable() {
        // super.onDisable();
        getContext().unregisterReceiver(labelReceiver);
    }

    @Override
    protected void sendData(final JsonObject data) {
      if (data == null || looper == null) {
        return;
      } else if (Thread.currentThread() != looper.getThread()) {
        // Ensure the data send runs on the probe's thread
        if (handler != null) {
          Message dataMessage = handler.obtainMessage(SEND_DATA_MESSAGE, data);
          handler.sendMessageAtFrontOfQueue(dataMessage);
        }
      } else {
        if (!data.has(TIMESTAMP)) {
          data.addProperty(TIMESTAMP, TimeUtil.getTimestamp());
        }
        IJsonObject immutableData = new IJsonObject(data);
        synchronized (dataListeners) {
          for (DataListener listener : dataListeners) {
            listener.onDataReceived(getConfig(), immutableData);
          }
        }
        synchronized (passiveDataListeners) {
          for (DataListener listener : passiveDataListeners) {
            if (!dataListeners.contains(listener)) {
              // Don't send data twice to passive listeners
              listener.onDataReceived(getConfig(), immutableData);
            }
          }
        }
      }

    }

    /*
    private void sendData(boolean isLabelled) {
        JsonObject data = new JsonObject();
        if (isLabelled) {
            data.addProperty(IS_LABELLED, 1);
        } else {
            data.addProperty(IS_LABELLED, 0);
        }
        sendData(data);
    }
    */

}
