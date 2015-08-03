package kr.ac.snu.imlab.scdc.entry;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.mit.media.funf.Schedule.BasicSchedule;
import kr.ac.snu.imlab.scdc.service.alarm.LabelAlarm;

/**
 * Created by kilho on 15. 6. 25.
 */
public class LabelEntry extends ProbeEntry {
  private String name;
  private long startLoggingTime;
  private int labelId;
  private long dateDue;
  private Calendar dateDueCal;

  public LabelEntry(int labelId, String name, Class probeClass,
                    BasicSchedule schedule, boolean isEnabled) {
    super(probeClass, schedule, isEnabled);
    this.labelId = labelId;
    this.name = name;
    this.startLoggingTime = -1;
  }

  public int getId() {
    return this.labelId;
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
    // this.dateDue =
  }

  public void endLog() {
    if (!isLogged()) return;
    this.startLoggingTime = -1;
  }

  public long getStartLoggingTime() {
    return this.startLoggingTime;
  }

  public boolean hasDateDue() {
    if (this.dateDue == -1) {
      return false;
    } else {
      return true;
    }
  }

  public long getDateDue() {
    return this.dateDue;
  }

  public void setDateDue(long dateDue) {
    this.dateDue = dateDue;
  }

  private void updateDateDueCal() {
    if (!hasDateDue()) {
      dateDueCal = null;
      return;
    }

    if (dateDueCal == null) {
      dateDueCal = new GregorianCalendar();
    }
    dateDueCal.setTimeInMillis(dateDue);
  }

//  public void setLogged(boolean isLogged) {
//    this.isLogged = isLogged;
//  }
}
