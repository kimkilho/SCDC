package kr.ac.snu.imlab.scdc.service.ar;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import kr.ac.snu.imlab.scdc.service.core.SCDCKeys;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DetectedActivitiesIntentService extends IntentService implements SCDCKeys.ActivityRecognitionKeys {

  protected static final String TAG = "DetectedActivitiesIS";

  /**
   * This constructor is required, and calls the super IntentService(String)
   * constructor with the name for a worker thread.
   */
  public DetectedActivitiesIntentService() {
    // Use the TAG to name the worker thread.
    super(TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  /**
   * Handles incoming intents.
   * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
   *               is called.
   */
  @Override
  protected void onHandleIntent(Intent intent) {
    ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
    Intent localIntent = new Intent(ACTION_AR_LOG);


    // Get the list of the probable activities associated with the current state of the
    // device. Each activity is associated with a confidence level, which is an int between
    // 0 and 100.
    ArrayList<DetectedActivity> detectedActivities =
            (ArrayList)result.getProbableActivities();

    // Log each activity
    Log.i(TAG, "activities detected");
    for (DetectedActivity da : detectedActivities) {
      Log.i(TAG, Constants.getActivityString(
                      getApplicationContext(),
                      da.getType()) + " " + da.getConfidence() + "%"
      );
    }

    // Broadcast the list of detected activities
    localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
  }
}
