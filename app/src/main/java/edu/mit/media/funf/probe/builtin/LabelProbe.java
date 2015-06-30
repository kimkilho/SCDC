package edu.mit.media.funf.probe.builtin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.json.IJsonArray;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.probe.Probe.Base;
import edu.mit.media.funf.probe.Probe.ContinuousProbe;
import edu.mit.media.funf.probe.Probe.PassiveProbe;
import edu.mit.media.funf.probe.Probe.DisplayName;
import edu.mit.media.funf.probe.Probe.Description;
import edu.mit.media.funf.probe.builtin.ProbeKeys.LabelKeys;

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
            JsonObject data = new JsonObject();
            // FIXME: Add some more labels
            labels.put(LabelKeys.SLEEP_LABEL, intent.getBooleanExtra(LabelKeys.SLEEP_LABEL, false));
            labels.put(LabelKeys.IN_CLASS_LABEL, intent.getBooleanExtra(LabelKeys.IN_CLASS_LABEL, false));
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
