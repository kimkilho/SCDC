package kr.ac.snu.imlab.scdc.entry;

import edu.mit.media.funf.Schedule.BasicSchedule;

/**
 * Created by kilho on 15. 6. 25.
 */
public class LabelEntry extends ProbeEntry {
  private String name;
  private long startLoggingTime;

  public LabelEntry(String name, Class probeClass, BasicSchedule schedule, boolean isEnabled) {
    super(probeClass, schedule, isEnabled);
    this.name = name;
    this.startLoggingTime = -1;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {

    this.name = name;
  }

  public boolean isLogged() {
    if (this.startLoggingTime == -1) {
      return false;
    } else {
      return true;
    }
  }

  public void startLog() {
    if (isLogged()) return;
    this.startLoggingTime = System.currentTimeMillis();
  }

  public void startLog(long startLoggingTime) {
    if (isLogged()) return;
    this.startLoggingTime = startLoggingTime;
  }

  public void endLog() {
    if (!isLogged()) return;
    this.startLoggingTime = -1;
  }

  public long getStartLoggingTime() {
    return this.startLoggingTime;
  }

//  public void setLogged(boolean isLogged) {
//    this.isLogged = isLogged;
//  }
}
