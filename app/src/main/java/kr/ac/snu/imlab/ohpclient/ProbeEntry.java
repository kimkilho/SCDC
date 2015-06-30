package kr.ac.snu.imlab.ohpclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.mit.media.funf.Schedule.BasicSchedule;
import edu.mit.media.funf.probe.Probe;

public class ProbeEntry {
  private Probe.Base probe;
  private Class probeClass;
  private boolean isEnabled;
  private BasicSchedule schedule;

  public ProbeEntry(Class probeClass) {
    this.probe = null;
    this.probeClass = probeClass;
    this.isEnabled = false;
    this.schedule = null;
  }

  public void setProbe(Gson gson) {
    this.probe = (Probe.Base)gson.fromJson(new JsonObject(), this.probeClass);
  }

  public Probe.Base getProbe() {
    return this.probe;
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
