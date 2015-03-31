package kr.ac.snu.imlab.ohpclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.mit.media.funf.probe.Probe;

public class ProbeEntry {
  private Probe.Base probe;
  private Class probeClass;
  private boolean isEnabled;

  public ProbeEntry(Class probeClass) {
    this.probe = null;
    this.probeClass = probeClass;
    this.isEnabled = false;
  }

  public void setProbe(Gson gson) {
    this.probe = (Probe.Base)gson.fromJson(new JsonObject(), this.probeClass);
  }

  public Probe.Base getProbe() {
    return this.probe;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public boolean isEnabled() {
    return this.isEnabled;
  }
}
