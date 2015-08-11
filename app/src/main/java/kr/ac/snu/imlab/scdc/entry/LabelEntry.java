package kr.ac.snu.imlab.scdc.entry;

import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.mit.media.funf.Schedule.BasicSchedule;
import kr.ac.snu.imlab.scdc.service.alarm.LabelAlarm;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
 * Created by kilho on 15. 6. 25.
 */
public class LabelEntry extends ProbeEntry {
  private int labelId;
  private SharedPrefsHandler spHandler;

  public LabelEntry(int labelId, String name, Class probeClass,
                    BasicSchedule schedule, boolean isEnabled,
                    Context context, String prefsName) {
    super(probeClass, schedule, isEnabled);
    this.labelId = labelId;
    this.spHandler = SharedPrefsHandler.getInstance(context,
                        prefsName, Context.MODE_PRIVATE);
    if (name != null) setName(name);
  }

  public int getId() {
    return this.labelId;
  }

  public String getName() {
    return spHandler.getLabelName(getId());
  }

  public void setName(String name) {
    spHandler.setLabelName(getId(), name);
  }

  public boolean isLogged() {
    if (getStartLoggingTime() == -1) {
      return false;
    } else {
      return true;
    }
  }

  public void startLog() {
    if (isLogged()) return;
    spHandler.setStartLoggingTime(getId(), System.currentTimeMillis());
  }

  public void startLog(long startLoggingTime) {
    if (isLogged()) return;
    spHandler.setStartLoggingTime(getId(), startLoggingTime);
    // this.dateDue =
  }

  public void endLog() {
    if (!isLogged()) return;
    spHandler.setStartLoggingTime(getId(), -1L);
  }

  public long getStartLoggingTime() {
    return spHandler.getStartLoggingTime(getId());
  }
//
//  public boolean isCompleted() {
//    return spHandler.getIsCompleted(getId());
//  }
//
//  public void setIsCompleted(boolean isCompleted) {
//    spHandler.setIsCompleted(getId(), isCompleted);
//  }
//
//  public void toggleIsCompleted() {
//    spHandler.toggleIsCompleted(getId());
//  }
//
//  public boolean hasDateDue() {
//    return spHandler.getHasDateDue(getId());
//  }
//
//  public void setHasDateDue(boolean hasDateDue) {
//    spHandler.setHasDateDue(getId(), hasDateDue);
//  }
//
//  public boolean hasFinalDateDue() {
//    return spHandler.getHasFinalDateDue(getId());
//  }
//
//  public void setHasFinalDateDue(boolean hasFinalDateDue) {
//    spHandler.setHasFinalDateDue(getId(), hasFinalDateDue);
//  }

  public boolean isRepeating() {
    return spHandler.getIsRepeating(getId());
  }

  public void setIsRepeating(boolean isRepeating) {
    spHandler.setIsRepeating(getId(), isRepeating);
  }

  public int getRepeatType() {
    return spHandler.getRepeatType(getId());
  }

  public void setRepeatType(int repeatType) {
    spHandler.setRepeatType(getId(), repeatType);
  }

  public int getRepeatInterval() {
    return spHandler.getRepeatInterval(getId());
  }

  public void setRepeatInterval(int repeatInterval) {
    spHandler.setRepeatInterval(getId(), repeatInterval);
  }

  public long getDateDue() {
    return spHandler.getDateDue(getId());
  }

  public void setDateDue(long dateDue) {
    spHandler.setDateDue(getId(), dateDue);
  }

//  public boolean isPastDue() {
//    return spHandler.getIsPastDue(getId());
//  }

//  public void setLogged(boolean isLogged) {
//    this.isLogged = isLogged;
//  }
}
