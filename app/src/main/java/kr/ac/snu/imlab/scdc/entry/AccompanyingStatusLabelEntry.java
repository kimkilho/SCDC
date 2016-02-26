package kr.ac.snu.imlab.scdc.entry;

import android.content.Context;

import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LabelKeys;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
 * Created by kilho on 16. 2. 25.
 */
public class AccompanyingStatusLabelEntry {

  protected static final String TAG = "AccompanyingNumbersEntry";

  private static int labelId;
  private SharedPrefsHandler spHandler;

  public AccompanyingStatusLabelEntry(Context context, String prefsName) {
    this.labelId = LabelKeys.ACCOMPANYING_NUMBERS_LABEL_ID;
    this.spHandler = SharedPrefsHandler.getInstance(context,
                        prefsName, Context.MODE_PRIVATE);
  }

  public int getId() {
    return this.labelId;
  }

  public int getLoggedStatus() {
    return spHandler.getAccompanyingStatus(getId());
  }

  public boolean isLogged() {
    if (getLoggedStatus() == LabelKeys.ACCOMPANYING_STATUS_NONE)
      return false;
    else
      return true;
  }

  public void startLog(int accompanyingStatusId) {
//    if (isLogged()) return;
    spHandler.setStartLoggingTime(getId(), System.currentTimeMillis());
    spHandler.setAccompanyingStatus(getId(), accompanyingStatusId);
  }

  public void startLog(int accompanyingStatusId, long startLoggingTime) {
//    if (isLogged()) return;
    spHandler.setStartLoggingTime(getId(), startLoggingTime);
    spHandler.setAccompanyingStatus(getId(), accompanyingStatusId);
  }

  public void endLog() {
//    if (!isLogged()) return;
    spHandler.setStartLoggingTime(getId(), -1L);
    spHandler.setAccompanyingStatus(getId(), LabelKeys.ACCOMPANYING_STATUS_NONE);
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
