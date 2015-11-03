package kr.ac.snu.imlab.scdc.service.probe;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.probe.Probe.Base;
import edu.mit.media.funf.probe.Probe.ContinuousProbe;
import edu.mit.media.funf.probe.Probe.Description;
import edu.mit.media.funf.probe.Probe.DisplayName;
import edu.mit.media.funf.time.TimeUtil;
import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.service.ar.Constants;
import kr.ac.snu.imlab.scdc.service.ar.DetectedActivitiesIntentService;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LogKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.ActivityRecognitionKeys;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by kilho on 2015. 6. 28..
 */
@DisplayName("Activity Recognition Log Probe")
@Description("Records label for all time")
@Schedule.DefaultSchedule(interval=600, duration=60, opportunistic=true)
public class ActivityRecognitionProbe extends Base
        implements ContinuousProbe, ActivityRecognitionKeys,
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    protected static final String TAG = "ActivityRecognitionProbe";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * A receiver for DetectedActivity objects broadcast by the
     * {@code ActivityDetectionIntentService}.
     */
    private BroadcastReceiver arReceiver;

    /**
     * The DetectedActivities that we track in this sample. We use this for initializing the
     * {@code DetectedActivitiesAdapter}. We also use this for persisting state in
     * {@code onSaveInstanceState()} and restoring it in {@code onCreate()}. This ensures that each
     * activity is displayed with the correct confidence level upon orientation changes.
     */
    private ArrayList<DetectedActivity> mDetectedActivities;


    /**
     * Called when the probe switches from the disabled to the enabled
     * state. This is where any passive or opportunistic listeners should be
     * configured. An enabled probe should not keep a wake lock. If you need
     * the device to stay awake consider implementing a StartableProbe, and
     * using the onStart method.
     */
    @Override
    protected void onEnable() {


        mDetectedActivities = new ArrayList<DetectedActivity>();
        // Set the confidence level of each monitored activity to zero.
        for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
          mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
        }

//        labelReceiver = new BroadcastReceiver() {
//          @Override
//          public void onReceive(Context context, Intent intent) {
//            Log.w("DEBUG", "LabelProbe/ Received broadcast");
//            JsonObject data = new JsonObject();
//
//            String[] labelNames = LaunchActivity.labelNames;
//            for (int i = 0; i < labelNames.length; i++) {
//              labels.put(labelNames[i],
//                         intent.getBooleanExtra(labelNames[i], false));
//            }
//            // Log.w("DEBUG", "SLEEP_LABEL=" + labels.get(LabelKeys.SLEEP_LABEL) + ", IN_CLASS_LABEL=" + labels.get(LabelKeys.IN_CLASS_LABEL));
//            for (String key : labels.keySet()) {
//              data.addProperty(key, labels.get(key));
//            }
//            data.addProperty(LabelKeys.PIPELINE_KEY,
//              intent.getBooleanExtra(LabelKeys.PIPELINE_KEY, false));
//
//            Log.w("DEBUG", "LabelProbe/ JsonObject data=" + data.toString());
//            sendData(data);
//          }
//        };
//        getContext().registerReceiver(labelReceiver, filter);

        // Kick off the request to build GoogleApiClient
        buildGoogleApiClient();

        mGoogleApiClient.connect();

        // Get a receiver for broadcasts from ActivityDetectionIntentService.
        arReceiver = new ActivityDetectionBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_AR_LOG);
        LocalBroadcastManager.getInstance(getContext())
            .registerReceiver(arReceiver, filter);

        // Intentionally wait 1 second for Google API Client to be connected
        // then register activity updates handler
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            // Register for activity recognition updates
            requestActivityUpdatesHandler();
          }
        }, 1000L);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDisable() {
        // super.onDisable();
        // Remove activity recognition updates
        removeActivityUpdatesHandler();
        LocalBroadcastManager.getInstance(getContext())
            .unregisterReceiver(arReceiver);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void sendData(final JsonObject data) {
      if (data == null || looper == null) {
        return;
      } else if (Thread.currentThread() != looper.getThread()) {
        // Ensure the data send runs on the probe's thread
        if (handler != null) {
          Message dataMessage = handler.obtainMessage(SEND_DATA_MESSAGE, data);
          handler.sendMessageAtFrontOfQueue(dataMessage);
        }
      } else {
        if (!data.has(TIMESTAMP)) {
          data.addProperty(TIMESTAMP, TimeUtil.getTimestamp());
        }
        IJsonObject immutableData = new IJsonObject(data);
        synchronized (dataListeners) {
          for (DataListener listener : dataListeners) {
            listener.onDataReceived(getConfig(), immutableData);
          }
        }
        synchronized (passiveDataListeners) {
          for (DataListener listener : passiveDataListeners) {
            if (!dataListeners.contains(listener)) {
              // Don't send data twice to passive listeners
              listener.onDataReceived(getConfig(), immutableData);
            }
          }
        }
      }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient() {
      mGoogleApiClient = new GoogleApiClient.Builder(getContext())
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .addApi(ActivityRecognition.API)
              .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
      Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
      // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
      // onConnectionFailed.
      Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
      // The connection to Google Play services was lost for some reason. We call connect() to
      // attempt to re-establish the connection.
      Log.i(TAG, "Connection suspended");
      mGoogleApiClient.connect();
    }

    /**
     * Registers for activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code requestActivityUpdates()} completes
     * successfully, the {@code DetectedActivitiesIntentService} starts receiving callbacks when
     * activities are detected.
     */
    public void requestActivityUpdatesHandler() {
      if (!mGoogleApiClient.isConnected()) {
        Log.d(LogKeys.DEBUG, TAG+"/ " + getContext().getString(R.string.not_connected));
//        Toast.makeText(getContext(), getContext().getString(R.string.not_connected),
//                Toast.LENGTH_SHORT).show();
        return;
      }
      ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
              mGoogleApiClient,
              Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
              getActivityDetectionPendingIntent()
      ).setResultCallback(this);
    }

    /**
     * Removes activity recognition updates using
     * {@link com.google.android.gms.location.ActivityRecognitionApi#removeActivityUpdates} which
     * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
     * implements the PendingResult interface, the activity itself receives the callback, and the
     * code within {@code onResult} executes. Note: once {@code removeActivityUpdates()} completes
     * successfully, the {@code DetectedActivitiesIntentService} stops receiving callbacks about
     * detected activities.
     */
    public void removeActivityUpdatesHandler() {
      if (!mGoogleApiClient.isConnected()) {
        Log.d(LogKeys.DEBUG, TAG+"/ " + getContext().getString(R.string.not_connected));
//        Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
        return;
      }
      // Remove all activity updates for the PendingIntent that was used to request activity
      // updates.
      ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
              mGoogleApiClient,
              getActivityDetectionPendingIntent()
      ).setResultCallback(this);
    }

    /**
     * Runs when the result of calling requestActivityUpdates() and removeActivityUpdates() becomes
     * available. Either method can complete successfully or with an error.
     *
     * @param status The Status returned through a PendingIntent when requestActivityUpdates()
     *               or removeActivityUpdates() are called.
     */
    public void onResult(Status status) {
      if (status.isSuccess()) {
        // Toggle the status of activity updates requested, and save in shared preferences.
        boolean requestingUpdates = !getUpdatesRequestedState();
        setUpdatesRequestedState(requestingUpdates);

        Log.d(LogKeys.DEBUG, TAG+"/ ActivityUpdates status changed");
//        setButtonsEnabledState();

        Log.d(LogKeys.DEBUG, TAG+"/ " +
                getContext().getString(requestingUpdates ?
                                R.string.activity_updates_added :
                                R.string.activity_updates_removed));
      } else {
        Log.e(LogKeys.DEBUG, TAG+"/ Error adding or removing activity detection: "
                              + status.getStatusMessage());
      }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
      Intent intent = new Intent(getContext(), DetectedActivitiesIntentService.class);

      // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
      // requestActivityUpdates() and removeActivityUpdates().
      return PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Retrieves a SharedPreference object used to store or read values in this app. If a
     * preferences file passed as the first argument to {@link #getSharedPreferences}
     * does not exist, it is created when {@link SharedPreferences.Editor} is used to commit
     * data.
     */
    private SharedPreferences getSharedPreferencesInstance() {
      return getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private boolean getUpdatesRequestedState() {
      return getSharedPreferencesInstance()
              .getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private void setUpdatesRequestedState(boolean requestingUpdates) {
      getSharedPreferencesInstance()
              .edit()
              .putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
              .commit();
    }

    /**
     * Process list of recently detected activities and updates the list of {@code DetectedActivity}
     * objects backing this adapter.
     *
     * @param detectedActivities the freshly detected activities
     */
    private void updateDetectedActivities(ArrayList<DetectedActivity> detectedActivities) {
      Log.d(LogKeys.DEBUG, TAG+"/ entered updateDetectedActivities()");
      HashMap<Integer, Integer> detectedActivitiesMap = new HashMap<>();
      for (DetectedActivity activity : detectedActivities) {
        detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
      }
      // Every time we detect new activities, we want to reset the confidence level of ALL
      // activities that we monitor. Since we cannot directly change the confidence
      // of a DetectedActivity, we use a temporary list of DetectedActivity objects. If an
      // activity was freshly detected, we use its confidence level. Otherwise, we set the
      // confidence level to zero.
      ArrayList<DetectedActivity> tempList = new ArrayList<DetectedActivity>();
      for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
        int confidence = detectedActivitiesMap.containsKey(Constants.MONITORED_ACTIVITIES[i]) ?
                detectedActivitiesMap.get(Constants.MONITORED_ACTIVITIES[i]) : 0;

        Log.d(LogKeys.DEBUG, TAG+"/ update activity: " +
                Constants.MONITORED_ACTIVITIES[i] + ": " + confidence);
        tempList.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i],
                confidence));
      }

      mDetectedActivities = tempList;
    }

    /**
     * Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
     * Receives a list of one or more DetectedActivity objects associated with the current state of
     * the device.
     */
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
      protected static final String TAG = "activity-detection-response-receiver";

      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(LogKeys.DEBUG, TAG+"/ Received broadcast");
        ArrayList<DetectedActivity> updatedActivities =
                intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

        ActivityRecognitionProbe.this.updateDetectedActivities(updatedActivities);
      }
    }


}
