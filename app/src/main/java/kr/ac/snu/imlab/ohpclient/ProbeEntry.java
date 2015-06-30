package kr.ac.snu.imlab.ohpclient;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.mit.media.funf.Schedule.BasicSchedule;
import edu.mit.media.funf.probe.Probe;

public class ProbeEntry {
  private JsonElement probeConfig;
  private Class probeClass;
  private boolean isEnabled;
  private BasicSchedule schedule;

  public ProbeEntry(Class probeClass, BasicSchedule schedule, boolean isEnabled) {
    this.probeConfig = new JsonParser()
                      .parse("{\"@type\": \"" +
                             probeClass.getName() +
                             "\"}").getAsJsonObject().get("@type");
    this.probeClass = probeClass;
    this.schedule = schedule;
    this.isEnabled = isEnabled;
  }

//  public void setProbe(Gson gson) {
//    this.probe = (Probe.Base)gson.fromJson(new JsonObject(), this.probeClass);
//  }

//  public Probe.Base getProbe() {
//    return this.probe;
//  }

  public JsonElement getProbeConfig() {
    return this.probeConfig;
  }

  public void setSchedule(BasicSchedule schedule) {
    this.schedule = schedule;
  }

  public BasicSchedule getSchedule() {
    return this.schedule;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public boolean isEnabled() {
    return this.isEnabled;
  }

  public Class<Probe.Base> getProbeClass() {
    return this.probeClass;
  }
}
