package kr.ac.snu.imlab.ohpclient;

import edu.mit.media.funf.Schedule.BasicSchedule;

/**
 * Created by kilho on 15. 6. 25.
 */
public class LabelEntry extends ProbeEntry {
  private String name;
  private boolean isLogged;

  public LabelEntry(String name, Class probeClass, BasicSchedule schedule, boolean isEnabled) {
    super(probeClass, schedule, isEnabled);
    this.name = name;
    this.isLogged = false;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {

    this.name = name;
  }

  public boolean isLogged() {
    return this.isLogged;
  }

  public void setLogged(boolean isLogged) {
    this.isLogged = isLogged;
  }
}
