package kr.ac.snu.imlab.scdc.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LogKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.SharedPrefs;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.AlarmKeys;
import kr.ac.snu.imlab.scdc.R;

/**
 * Created by kilho on 15. 8. 5.
 */
public class SharedPrefsHandler {

  private Context context;
  private SharedPreferences prefs;
  private static SharedPrefsHandler instance;
  private String deviceId;
  private String userinfoUrl;
  private static boolean firstrun = true;

  private SharedPrefsHandler() {
  }

  private SharedPrefsHandler(Context context, String name, int mode) {
    this.context = context;
    this.prefs = context.getSharedPreferences(name, mode);
    this.deviceId = Secure.getString(this.context.getContentResolver(),
            Secure.ANDROID_ID);
    this.userinfoUrl = Config.DEFAULT_USERINFO_URL;
    Log.d(LogKeys.DEBUG,
      "SharedPrefsHandler.SharedPrefsHandler(): deviceId=" + this.deviceId);
    firstrun = prefs.getBoolean("firstrun", true);
    if (firstrun && context instanceof LaunchActivity) {
      try {
        new GetPrefsFromServerTask().execute(userinfoUrl + deviceId + "/");
      } catch (Exception e) {
        Log.e(LogKeys.DEBUG, "SharedPrefsHandler.SharedPrefsHandler(): error=", e);
      }
    }
  }

  public static synchronized SharedPrefsHandler
    getInstance(Context context, String name, int mode) {
    instance = new SharedPrefsHandler(context, name, mode);
    return instance;
  }

  // Funf sensor
  public boolean isSensorOn() {
    Log.d(LogKeys.DEBUG, "SharedPrefsHandler.isSensorOn(): called");
    return prefs.getBoolean(SharedPrefs.SENSOR_ON, Config.DEFAULT_SENSOR_ON);
  }

  public boolean setSensorOn(boolean isSensorOn) {
    prefs.edit().putBoolean(SharedPrefs.SENSOR_ON, isSensorOn).apply();
    return true;
  }


  // User info
  public String getUsername() {
//    try {
      // DEFAULT_USERINFO_URL = "http://imlab-ws2.snu.ac.kr:8888/userinfo/",
//      String response = HttpUtil.sendGet(userinfoUrl + deviceId + "/");
//      Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getUsername(): response=" + response);
//      JsonObject userInfo = new JsonParser().parse(response).getAsJsonObject();
//      // prefs.edit().putString(SharedPrefs.USERNAME,
//              // userInfo.get(SharedPrefs.USERNAME).toString()).apply();
//      return userInfo.get(SharedPrefs.USERNAME).toString();
//    } catch (Exception e) {
//      Log.e(LogKeys.DEBUG, "SharedPrefsHandler.getUsername(): error=", e);
//      return prefs.getString(SharedPrefs.USERNAME, Config.DEFAULT_USERNAME);
////    } finally {
////      return prefs.getString(SharedPrefs.USERNAME, Config.DEFAULT_USERNAME);
//    }
    // Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getUsername(): updated=" + updated);
      Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getUsername(): firstrun=" + firstrun);
      return prefs.getString(SharedPrefs.USERNAME, Config.DEFAULT_USERNAME);
//    try {
//      // Wait until the SharedPrefs is synchronized with the server side
//      while (firstrun) {
////        Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getUsername(): sleeping");
//        Thread.sleep(100);
//      }
//      return prefs.getString(SharedPrefs.USERNAME, Config.DEFAULT_USERNAME);
//    } catch (InterruptedException e) {
//      Log.e(LogKeys.DEBUG, "SharedPrefsHandler.getUsername(): error=", e);
//      return prefs.getString(SharedPrefs.USERNAME, Config.DEFAULT_USERNAME);
//    }
  }

  public void setUsername(String username) {
//    try {
//      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//      nameValuePairs.add(new BasicNameValuePair(SharedPrefs.USERNAME, username));
//      String response =
//        HttpUtil.sendPost(userinfoUrl + deviceId + "/", nameValuePairs);
//      Log.d(LogKeys.DEBUG, "SharedPrefsHandler.setUsername(): response=" + response);
//    } catch (Exception e) {
//      prefs.edit().putString(SharedPrefs.USERNAME, username).apply();
//    }
    prefs.edit().putString(SharedPrefs.USERNAME, username).apply();
//    new SetPrefsToServerTask().execute(userinfoUrl + deviceId + "/");
  }

  public boolean getIsFemale() {
//    Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getIsFemale(): updated=" + updated);
      Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getIsFemale(): firstrun=" + firstrun);
      return prefs.getBoolean(SharedPrefs.IS_FEMALE, Config.DEFAULT_IS_FEMALE);
    // Wait until the SharedPrefs is synchronized with the server side
//    try {
//      while (firstrun) {
////        Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getIsFemale(): sleeping");
//        Thread.sleep(100);
//      }
//      return prefs.getBoolean(SharedPrefs.IS_FEMALE, Config.DEFAULT_IS_FEMALE);
//    } catch (InterruptedException e) {
//      Log.e(LogKeys.DEBUG, "SharedPrefsHandler.getIsFemale(): error=", e);
//      return prefs.getBoolean(SharedPrefs.IS_FEMALE, Config.DEFAULT_IS_FEMALE);
//    }
  }

  public void setIsFemale(boolean isFemale) {
    prefs.edit().putBoolean(SharedPrefs.IS_FEMALE, isFemale).apply();
//    new SetPrefsToServerTask().execute(userinfoUrl + deviceId + "/");
  }


  // Id's related to data collection
  // Methods to track expId's for each data emission from Probe
  public int getExpId(String probeConfig) {
    return prefs.getInt(SharedPrefs.LABEL_EXP_ID_PREFIX + probeConfig, 0);
  }

  public void setExpId(String componentString, int expId) {
    prefs.edit().putInt(SharedPrefs.LABEL_EXP_ID_PREFIX +
            componentString, expId).apply();
  }

  // Methods to track sensorId's for each sensor switch off-->on
  public int getSensorId() {
    // Wait until the SharedPrefs is synchronized with the server side
//    Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getSensorId(): updated=" + updated);
//      Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getSensorId(): firstrun=" + firstrun);
      return prefs.getInt(SharedPrefs.LABEL_SENSOR_ID, SharedPrefs.DEFAULT_SENSOR_ID);
//    try {
//      while (!updated) {
////        Log.d(LogKeys.DEBUG, "SharedPrefsHandler.getSensorId(): sleeping");
//        Thread.sleep(100);
//      }
//      return prefs.getInt(SharedPrefs.LABEL_SENSOR_ID, 0);
//    } catch (InterruptedException e) {
//      Log.e(LogKeys.DEBUG, "SharedPrefsHandler.getSensorId(): error=", e);
//      return prefs.getInt(SharedPrefs.LABEL_SENSOR_ID, 0);
//    }
  }

  public void setSensorId(int sensorId) {
    prefs.edit().putInt(SharedPrefs.LABEL_SENSOR_ID, sensorId).apply();
//    new SetPrefsToServerTask().execute(userinfoUrl + deviceId + "/");
  }

  // Synchronize preferences with server
  // IMPORTANT: This method is only executed while uploading
  public void setPrefsToServer() {
    new SetPrefsToServerTask().execute(userinfoUrl + deviceId + "/");
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


  // Alarm - General
  public int getGeneralRepeatType() {
    return prefs.getInt(SharedPrefs.GENERAL_REPEAT_TYPE,
            Integer.parseInt(AlarmKeys.DEFAULT_REPEAT_TYPE));
  }

  public void setGeneralRepeatType(int repeatType) {
    if (repeatType >= 0 && repeatType <= 5) {
      prefs.edit().putInt(SharedPrefs.GENERAL_REPEAT_TYPE,
                          repeatType).apply();
    }
  }

  public int getGeneralRepeatInterval() {
    return prefs.getInt(SharedPrefs.GENERAL_REPEAT_INTERVAL,
            Integer.parseInt(AlarmKeys.DEFAULT_GENERAL_ALARM_REPEAT_INTERVAL));
  }

  public void setGeneralRepeatInterval(int repeatInterval) {
    prefs.edit().putInt(SharedPrefs.GENERAL_REPEAT_INTERVAL,
                        repeatInterval).apply();
  }


  // Alarm - Labels
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
                            String.valueOf(labelId), true);
  }

  public void setIsRepeating(int labelId, boolean isRepeating) {
    prefs.edit().putBoolean(SharedPrefs.LABEL_IS_REPEATING +
                            String.valueOf(labelId), isRepeating).apply();
  }

  public int getRepeatType(int labelId) {
    return prefs.getInt(SharedPrefs.LABEL_REPEAT_TYPE_PREFIX +
                    String.valueOf(labelId),
            Integer.parseInt(AlarmKeys.DEFAULT_REPEAT_TYPE));
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
                    String.valueOf(labelId),
            Integer.parseInt(AlarmKeys.DEFAULT_REPEAT_INTERVAL));
  }

  public void setRepeatInterval(int labelId, int repeatInterval) {
    prefs.edit().putInt(SharedPrefs.LABEL_REPEAT_INTERVAL_PREFIX +
            String.valueOf(labelId), repeatInterval).apply();
//    Log.d(SCDCKeys.LogKeys.DEBUG, "SharedPrefsHandler.setRepeatInterval(" +
//            labelId + ", " + repeatInterval + ")");
//    Log.d(SCDCKeys.LogKeys.DEBUG, "SharedPrefsHandler.getRepeatInterval(" +
//            labelId + ")=" + getRepeatInterval(labelId));
  }

  // FIXME:
  public long getDateDue(int labelId) {
    return prefs.getLong(SharedPrefs.LABEL_DATE_DUE_PREFIX +
                    String.valueOf(labelId),
            Long.parseLong(AlarmKeys.DEFAULT_DATE_DUE));
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

  // Save reminderToggleButton state
  public boolean isReminderRunning() {
    return prefs.getBoolean(SharedPrefs.IS_REMINDER_RUNNING, false);
  }

  public void setReminderRunning(boolean isRunning) {
    prefs.edit().putBoolean(SharedPrefs.IS_REMINDER_RUNNING,
                            isRunning).apply();
  }


  private class GetPrefsFromServerTask extends AsyncTask<String, Void, Boolean> {

    private ProgressDialog progressDialog;

    @Override
    protected void onPreExecute() {
      progressDialog = new ProgressDialog(context);
      progressDialog.setMessage(context.getString(R.string.get_prefs_from_server_message));
      progressDialog.setCancelable(false);
      progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... urls) {
      try {
        String response = HttpUtil.sendGet(urls[0]);
        Log.d(LogKeys.DEBUG, "SharedPrefsHandler.GetPrefsFromServerTask" +
                              ".doInBackground(): response=" + response);
        JsonObject userInfo = new JsonParser().parse(response).getAsJsonObject();
        String newUsername = userInfo.get(SharedPrefs.USERNAME).getAsString();
        int newIsFemale = userInfo.get(SharedPrefs.IS_FEMALE).getAsInt();
        int newSensorId = userInfo.get(SharedPrefs.LABEL_SENSOR_ID).getAsInt();

        String currUsername = prefs.getString(SharedPrefs.USERNAME,
                                              Config.DEFAULT_USERNAME);
        boolean currIsFemale =
          prefs.getBoolean(SharedPrefs.IS_FEMALE, Config.DEFAULT_IS_FEMALE);
        int currSensorId = prefs.getInt(SharedPrefs.LABEL_SENSOR_ID, 0);

        if (!currUsername.equals(newUsername))
          prefs.edit().putString(SharedPrefs.USERNAME, newUsername).apply();
        if ((currIsFemale ? 1 : 0) != newIsFemale)
          prefs.edit().putBoolean(SharedPrefs.IS_FEMALE,
                                  (newIsFemale == 1)).apply();
        if (currSensorId != newSensorId)
          prefs.edit().putInt(SharedPrefs.LABEL_SENSOR_ID, newSensorId).apply();

        return true;

      } catch (Exception e) {
        Log.e(LogKeys.DEBUG, "SharedPrefsHandler.GetPrefsFromServerTask" +
                              ".onPostExecute(): error=", e);
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      // updated = result;
      // Change UI of LaunchActivity
      prefs.edit().putBoolean("firstrun", false).apply();

      EditText username;
      RadioButton isMaleRadioButton, isFemaleRadioButton;
      if (context instanceof LaunchActivity) {
        username = (EditText)((LaunchActivity)context)
                .findViewById(R.id.user_name);
        username.setText(getUsername());
        isMaleRadioButton = (RadioButton)((LaunchActivity)context).findViewById(R.id.radio_male);
        isFemaleRadioButton = (RadioButton)((LaunchActivity)context).findViewById(R.id.radio_female);
        isMaleRadioButton.setChecked(!getIsFemale());
        isFemaleRadioButton.setChecked(getIsFemale());
      }

      progressDialog.dismiss();
      Log.d(LogKeys.DEBUG, "SharedPrefsHandler.GetPrefsFromServerTask" +
                            ".onPostExecute(): get prefs from server complete");
    }
  }

  private class SetPrefsToServerTask extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String... urls) {
      try {
        String currUsername = prefs.getString(SharedPrefs.USERNAME,
                Config.DEFAULT_USERNAME);
        boolean currIsFemale =
                prefs.getBoolean(SharedPrefs.IS_FEMALE, Config.DEFAULT_IS_FEMALE);

        int currSensorId = prefs.getInt(SharedPrefs.LABEL_SENSOR_ID, 0);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(SharedPrefs.DEVICE_ID,
                                                  deviceId));
        nameValuePairs.add(new BasicNameValuePair(SharedPrefs.USERNAME,
                                                  currUsername));
        nameValuePairs.add(new BasicNameValuePair(SharedPrefs.IS_FEMALE,
                              String.valueOf((currIsFemale) ? 1 : 0)));
        nameValuePairs.add(new BasicNameValuePair(SharedPrefs.LABEL_SENSOR_ID,
                                              String.valueOf(currSensorId)));
        String response = HttpUtil.sendPost(urls[0], nameValuePairs);
        Log.d(LogKeys.DEBUG, "SharedPrefsHandler.SetPrefsToServerTask" +
                              ".doInBackground(): response=" + response);
        return true;
      } catch (Exception e) {
        Log.e(LogKeys.DEBUG, "SharedPrefsHandler.SetPrefsToServerTask" +
                              ".onPostExecute(): error=", e);
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      Log.d(LogKeys.DEBUG, "SharedPrefsHandler.SetPrefsToServerTask" +
                            ".onPostExecute(): set prefs to server complete");
    }
  }

}
