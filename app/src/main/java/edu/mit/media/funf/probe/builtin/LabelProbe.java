package edu.mit.media.funf.probe.builtin;

import com.google.gson.JsonObject;

import java.math.BigDecimal;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.ContinuousProbe;
import edu.mit.media.funf.probe.Probe.DisplayName;

/**
 * Created by kilho on 2015. 6. 28..
 */
@DisplayName("Label Log Probe")
@Schedule.DefaultSchedule(interval=3600)
public class LabelProbe extends ImpulseProbe implements ContinuousProbe {

    // TODO: Implement ContinuableProbe methods

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendData(true);
    }

    @Override
    protected void onDisable() {

    }

    private void sendData(boolean isLabelled) {
        JsonObject data = new JsonObject();
        data.addProperty("isLabelled", isLabelled);
        sendData(data);
    }
}
