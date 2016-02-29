package kr.ac.snu.imlab.scdc.entry;

import android.content.Context;

import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LabelKeys;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
 * Created by kilho on 16. 2. 26.
 */
public class ConversingStatusLabelEntry {

  protected static final String TAG = "ConversingStatusLabelEntry";

  private int labelId;
  private SharedPrefsHandler spHandler;

  public ConversingStatusLabelEntry(int labelId, String name,
                                    Context context, String prefsName) {
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

  public int getLoggedStatus() {
    return spHandler.getConversingStatus(getId());
  }

  public boolean isLogged() {
    if (getLoggedStatus() == LabelKeys.CONVERSING_STATUS_NONE)
      return false;
    else
      return true;
  }

  public void startLog(int conversingStatusId) {
    spHandler.setStartLoggingTime(getId(), System.currentTimeMillis());
    spHandler.setConversingStatus(getId(), conversingStatusId);
  }

  public void startLog(int conversingStatusId, long startLoggingTime) {
    spHandler.setStartLoggingTime(getId(), startLoggingTime);
    spHandler.setConversingStatus(getId(), conversingStatusId);
  }

  public void endLog() {
    spHandler.setStartLoggingTime(getId(), -1L);
    spHandler.setConversingStatus(getId(), LabelKeys.CONVERSING_STATUS_NONE);
  }

  public long getStartLoggingTime() {
    return spHandler.getStartLoggingTime(getId());
  }

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
}
