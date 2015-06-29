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

    /**
     * Called when the probe switches from the disabled to the enabled
     * state. This is where any passive or opportunistic listeners should be
     * configured. An enabled probe should not keep a wake lock. If you need
     * the device to stay awake consider implementing a StartableProbe, and
     * using the onStart method.
     */
    @Override
    protected void onEnable() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LABEL_LOG);

        labelReceiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            final String labelType = intent.getStringExtra(LabelKeys
                    .LABEL_TYPE);
            final boolean isLabelled = intent.getBooleanExtra(LabelKeys
                            .IS_LABELLED, false);
            Log.w("DEBUG", "LABEL_TYPE=" + intent.getStringExtra(LabelKeys.LABEL_TYPE) + ", IS_LABELLED=" + intent.getBooleanExtra(LabelKeys.IS_LABELLED, false));
            JsonObject data = new JsonObject();
            data.addProperty(LABEL_TYPE, labelType);
            data.addProperty(IS_LABELLED, isLabelled);
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


  // private LabelListener listener;

  private class LabelListener implements DataListener {

    private Gson gson = getGson();

    /**
     * Called when the probe emits data. Data emitted from probes that
     * extend the Probe class are guaranteed to have the PROBE and TIMESTAMP
     * parameters.
     *
     * @param completeProbeUri
     * @param labelData
     */
    @Override
    public void onDataReceived(IJsonObject completeProbeUri,
                               IJsonObject labelData) {
      int currLabel = labelData.get(LabelProbe.IS_LABELLED).getAsInt();

      JsonObject data = new JsonObject();
      data.addProperty(IS_LABELLED, currLabel);
      sendData(data);
    }

    /**
     * Called when the probe is finished sending a stream of data. This can
     * be used to know when the probe was run, even if it didn't send data.
     * It can also be used to get a checkpoint of far through the data
     * stream the probe ran. Continuable probes can use this checkpoint to
     * start the data stream where it previously left off.
     *
     * @param completeProbeUri
     * @param checkpoint
     */
    @Override
    public void onDataCompleted(IJsonObject completeProbeUri,
                                JsonElement checkpoint) {

    }

  }

}
