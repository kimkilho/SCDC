package kr.ac.snu.imlab.scdc.util;

import android.content.Context;
import android.content.SharedPreferences;

import kr.ac.snu.imlab.scdc.service.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.SharedPrefs;
import kr.ac.snu.imlab.scdc.service.SCDCKeys.AlarmKeys;

/**
 * Created by kilho on 15. 8. 5.
 */
public class SharedPrefsHandler {

  private Context context;
  private SharedPreferences prefs;
  private static SharedPrefsHandler instance;

  private SharedPrefsHandler() {
  }

  private SharedPrefsHandler(Context context, String name, int mode) {
    this.context = context;
    this.prefs = context.getSharedPreferences(name, mode);
  }

  public static synchronized SharedPrefsHandler
    getInstance(Context context, String name, int mode) {
    instance = new SharedPrefsHandler(context, name, mode);
    return instance;
  }

  // User info
  public String getUsername() {
    return prefs.getString(SharedPrefs.USERNAME, Config.DEFAULT_USERNAME);
  }

  public void setUsername(String username) {
    prefs.edit().putString(SharedPrefs.USERNAME, username).apply();
  }

  public boolean getIsFemale() {
    return prefs.getBoolean(SharedPrefs.IS_FEMALE, Config.DEFAULT_IS_FEMALE);
  }

  public void setIsFemale(boolean isFemale) {
    prefs.edit().putBoolean(SharedPrefs.IS_FEMALE, isFemale).apply();
  }


  // Labels
  public int getNumLabels() {
    return prefs.getInt(SharedPrefs.NUM_LABELS, 1);
  }

  public void setNumLabels(int size) {
    prefs.edit().putInt(SharedPrefs.NUM_LABELS, size).apply();
  }

  public String getLabelName(int labelId) {
    return prefs.getString(SharedPrefs.LABEL_NAME_PREFIX +
                           String.valueOf(labelId), null);
  }

  public void setLabelName(int labelId, String labelName) {
    prefs.edit().putString(SharedPrefs.LABEL_NAME_PREFIX +
      String.valueOf(labelId), labelName).apply();
  }

  public long getStartLoggingTime(int labelId) {
    return prefs.getLong(SharedPrefs.LABEL_START_LOGGING_TIME_PREFIX +
                         String.valueOf(labelId), -1);
  }

  public void setStartLoggingTime(int labelId, long startLoggingTime) {
    prefs.edit().putLong(SharedPrefs.LABEL_START_LOGGING_TIME_PREFIX +
                         String.valueOf(labelId), startLoggingTime).apply();
  }

  public boolean getIsLogged(int labelId) {
    if (getStartLoggingTime(labelId) == -1) {
      return false;
    } else {
      return true;
    }
  }

  // Labels - Alarm
  public boolean getIsCompleted(int labelId) {
    return prefs.getBoolean(SharedPrefs.LABEL_IS_COMPLETED_PREFIX +
                            String.valueOf(labelId), true);
  }

  public void setIsCompleted(int labelId, boolean isCompleted) {
    prefs.edit().putBoolean(SharedPrefs.LABEL_IS_COMPLETED_PREFIX +
                            String.valueOf(labelId), isCompleted).apply();
  }

  public void toggleIsCompleted(int labelId) {
    boolean isCompleted = getIsCompleted(labelId) ? false : true;
    setIsCompleted(labelId, isCompleted);
  }

  public boolean getHasDateDue(int labelId) {
    return prefs.getBoolean(SharedPrefs.LABEL_HAS_DATE_DUE_PREFIX +
                            String.valueOf(labelId), false);
  }

  public void setHasDateDue(int labelId, boolean hasDateDue) {
    prefs.edit().putBoolean(SharedPrefs.LABEL_HAS_DATE_DUE_PREFIX +
                            String.valueOf(labelId), hasDateDue).apply();
  }

  public boolean getHasFinalDateDue(int labelId) {
    return prefs.getBoolean(SharedPrefs.LABEL_HAS_FINAL_DATE_DUE +
            String.valueOf(labelId), false);
  }

  public void setHasFinalDateDue(int labelId, boolean hasFinalDateDue) {
    prefs.edit().putBoolean(SharedPrefs.LABEL_HAS_FINAL_DATE_DUE +
            String.valueOf(labelId), hasFinalDateDue).apply();
  }

  // for repeating alarm
  public boolean getIsRepeating(int labelId) {
    return prefs.getBoolean(SharedPrefs.LABEL_IS_REPEATING +
                            String.valueOf(labelId), false);
  }

  public void setIsRepeating(int labelId, boolean isRepeating) {
    prefs.edit().putBoolean(SharedPrefs.LABEL_IS_REPEATING +
                            String.valueOf(labelId), isRepeating).apply();
  }

  public int getRepeatType(int labelId) {
    return prefs.getInt(SharedPrefs.LABEL_REPEAT_TYPE_PREFIX +
            String.valueOf(labelId), -1);
  }

  public void setRepeatType(int labelId, int repeatType) {
    if (repeatType >= 0 && repeatType <= 5) {
      setIsRepeating(labelId, true);
      prefs.edit().putInt(SharedPrefs.LABEL_REPEAT_TYPE_PREFIX +
              String.valueOf(labelId), repeatType).apply();
    }
  }

  public int getRepeatInterval(int labelId) {
    return prefs.getInt(SharedPrefs.LABEL_REPEAT_INTERVAL_PREFIX +
            String.valueOf(labelId), -1);
  }

  public void setRepeatInterval(int labelId, int repeatInterval) {
    prefs.edit().putInt(SharedPrefs.LABEL_REPEAT_INTERVAL_PREFIX +
            String.valueOf(labelId), repeatInterval).apply();
  }

  public long getDateDue(int labelId) {
    return prefs.getLong(SharedPrefs.LABEL_DATE_DUE_PREFIX +
                         String.valueOf(labelId), -1L);
  }

  public void setDateDue(int labelId, long dateDue) {
    prefs.edit().putLong(SharedPrefs.LABEL_DATE_DUE_PREFIX +
                         String.valueOf(labelId), dateDue).apply();
  }

  public boolean getIsPastDue(int labelId) {
    if (!getHasDateDue(labelId) || getIsCompleted(labelId))
      return false;

    return getDateDue(labelId) - System.currentTimeMillis() < 0;
  }

  // for procrastinator alarm
  public String getAlarmTime() {
    return prefs.getString(SharedPrefs.ALARM_TIME,
                           AlarmKeys.DEFAULT_ALARM_TIME);
  }

  // for reminder
  public String getReminderTime() {
    return prefs.getString(SharedPrefs.REMINDER_TIME,
                           AlarmKeys.DEFAULT_REMINDER_TIME);
  }

  // for notification
  public boolean getVibrateOnAlarm() {
    return prefs.getBoolean(SharedPrefs.VIBRATE_ON_ALARM, true);
  }

  // for time settings
  public String getDefaultHour() {
    return prefs.getString(SharedPrefs.DEFAULT_HOUR,
                           AlarmKeys.DEFAULT_HOUR_VALUE);
  }
}
