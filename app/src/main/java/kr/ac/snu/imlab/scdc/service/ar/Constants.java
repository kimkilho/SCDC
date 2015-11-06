package kr.ac.snu.imlab.scdc.service.ar;

import android.content.Context;
import android.content.res.Resources;
import kr.ac.snu.imlab.scdc.R;

import com.google.android.gms.location.DetectedActivity;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.ActivityRecognitionKeys;

/**
 * Created by kilho on 15. 11. 2.
 */
public class Constants {

  private Constants() {
  }

  public static final String PACKAGE_NAME =
          "com.google.android.gms.location.activityrecognition";

  public static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST_ACTION";

  public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";

  public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES";

  public static final String ACTIVITY_UPDATES_REQUESTED_KEY = PACKAGE_NAME +
          ".ACTIVITY_UPDATES_REQUESTED";

  public static final String DETECTED_ACTIVITIES = PACKAGE_NAME + ".DETECTED_ACTIVITIES";

  /**
   * The desired time between activity detections. Larger values result in fewer activity
   * detections while improving battery life. A value of 0 results in activity detections at the
   * fastest possible rate. Getting frequent updates negatively impact battery life and a real
   * app may prefer to request less frequent updates.
   */
  public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;

  /**
   * List of DetectedActivity types that we monitor in this sample.
   */
  public static final int[] MONITORED_ACTIVITIES = {
          DetectedActivity.STILL,
          DetectedActivity.ON_FOOT,
          DetectedActivity.WALKING,
          DetectedActivity.RUNNING,
          DetectedActivity.ON_BICYCLE,
          DetectedActivity.IN_VEHICLE,
          DetectedActivity.TILTING,
          DetectedActivity.UNKNOWN
  };

  /**
   * Returns a human readable String corresponding to a detected activity type.
   */
  public static String getActivityString(int detectedActivityType) {
    switch(detectedActivityType) {
      case DetectedActivity.IN_VEHICLE:
        return ActivityRecognitionKeys.IN_VEHICLE_ACTIVITY;
      case DetectedActivity.ON_BICYCLE:
        return ActivityRecognitionKeys.ON_BICYCLE_ACTIVITY;
      case DetectedActivity.ON_FOOT:
        return ActivityRecognitionKeys.ON_FOOT_ACTIVITY;
      case DetectedActivity.RUNNING:
        return ActivityRecognitionKeys.RUNNING_ACTIVITY;
      case DetectedActivity.STILL:
        return ActivityRecognitionKeys.STILL_ACTIVITY;
      case DetectedActivity.TILTING:
        return ActivityRecognitionKeys.TILTING_ACTIVITY;
      case DetectedActivity.UNKNOWN:
        return ActivityRecognitionKeys.UNKNOWN_ACTIVITY;
      case DetectedActivity.WALKING:
        return ActivityRecognitionKeys.WALKING_ACTIVITY;
      default:
        return ActivityRecognitionKeys.UNIDENTIFIABLE_ACTIVITY;
    }
  }

}
