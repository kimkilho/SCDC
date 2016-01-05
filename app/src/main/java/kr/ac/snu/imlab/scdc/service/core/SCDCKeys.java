package kr.ac.snu.imlab.scdc.service.core;

/**
 * Created by kilho on 15. 7. 28.
 */
public class SCDCKeys {

  public static interface Config {
    public static final String
      PIPELINE_NAME = "scdc",
      SCDC_PREFS = "kr.ac.snu.imlab.scdc.prefs",
      DEFAULT_USERNAME = "imlab_user",
      // Upload URL for publish
      DEFAULT_UPLOAD_URL = "http://imlab-ws2.snu.ac.kr:7777/data/",
      // Upload URL for debug
      DEFAULT_UPLOAD_URL_DEBUG = "http://imlab-ws2.snu.ac.kr:7000/data",
      // Update URL for publish
      DEFAULT_USERINFO_URL = "http://imlab-ws2.snu.ac.kr:7777/userinfo/",
      DEFAULT_UPDATE_URL_IDLE = "http://imlab-ws2.snu.ac.kr:7777/conf/idle/",
      DEFAULT_UPDATE_URL_ACTIVE = "http://imlab-ws2.snu.ac.kr:7777/conf/active/",
      // Update URL for debug
      DEFAULT_UPDATE_URL_DEBUG = "http://imlab-ws2.snu.ac.kr:7000/config";

    public static final int
      PIPELINE_VERSION = 1;

    public static final boolean
      DEFAULT_SENSOR_ON = false,
      DEFAULT_IS_FEMALE = false;
  }

  public static interface SharedPrefs {
    public static final String
      SENSOR_ON = "sensor_on",
      USERNAME = "username",
      IS_FEMALE = "isFemale",
      DEVICE_ID = "deviceId",
      NUM_LABELS = "total_num_labels",
      LABEL_NAME_PREFIX = "label_name_",
      LABEL_IS_COMPLETED_PREFIX = "label_is_completed_",
      LABEL_HAS_DATE_DUE_PREFIX = "label_has_date_due_",
      LABEL_IS_REPEATING = "label_is_repeating_",
      LABEL_START_LOGGING_TIME_PREFIX = "start_logging_time_",
      LABEL_DATE_DUE_PREFIX = "date_due_",
      LABEL_REPEAT_TYPE_PREFIX = "repeat_type_",
      LABEL_REPEAT_INTERVAL_PREFIX = "repeat_interval_",
      LABEL_HAS_FINAL_DATE_DUE = "has_final_date_due_",
      ALARM_TIME = "alarm_time",
      REMINDER_TIME = "reminder_time",
      VIBRATE_ON_ALARM = "vibrate_on_alarm",
      DEFAULT_HOUR = "default_hour",
      IS_REMINDER_RUNNING = "is_reminder_running",
      GENERAL_REPEAT_TYPE = "general_repeat_type",
      GENERAL_REPEAT_INTERVAL = "general_repeat_interval",
      LABEL_EXP_ID_PREFIX = "label_expId_",
      KEY_EXP_ID = "expId",
      KEY_SENSOR_ID = "sensorId",
      ACTIVE_CONFIG = "activeConfig",
      IDLE_CONFIG = "idleConfig";

    public static final int
      DEFAULT_SENSOR_ID = 0;
  }

  public static interface AlarmKeys {
    public static final String
      EXTRA_ALARM_NAME = "kr.ac.snu.imlab.scdc.service.alarm.ALARM_NAME",
      EXTRA_ALARM_ID = "kr.ac.snu.imlab.scdc.service.alarm.ALARM_ID",
      EXTRA_DATE_DUE = "kr.ac.snu.imlab.scdc.service.alarm.DATE_DUE",
      ALARM_EXTRA = "kr.ac.snu.imlab.scdc.service.alarm.LabelAlarm";

    // Repeat constants
    public static final int
            MINUTES = 0,
            HOURS = 1,
            DAYS = 2,
            WEEKS = 3,
            MONTHS = 4,
            YEARS = 5;

    public static final String
      DEFAULT_REMINDER_TIME = "6",
      DEFAULT_ALARM_TIME = "5",
      DEFAULT_HOUR_VALUE = "12",
      DEFAULT_REPEAT_TYPE = String.valueOf(MINUTES),
      DEFAULT_REPEAT_INTERVAL = "2",
      DEFAULT_DATE_DUE = "3155760000";  // 2070.01.01 00:00:00

    public static final String
      DEFAULT_GENERAL_ALARM_ID = "100",
      DEFAULT_GENERAL_ALARM_REPEAT_TYPE = String.valueOf(MINUTES),
      DEFAULT_GENERAL_ALARM_REPEAT_INTERVAL = "5";
  }

  public static interface LabelKeys {
    public static final String
      DEFAULT_NUM_LABELS = "5",
      ACTION_LABEL_LOG = "kr.ac.snu.imlab.scdc.Broadcasting.action.LABEL_LOG",
        // deprecated
      ACTION_LABEL_STATUS_LOG = "kr.ac.snu.imlab.scdc.Broadcasting.action" +
                                  ".LABEL_STATUS_LOG",
      EXTRA_IS_ACTIVE_LABEL = "kr.ac.snu.imlab.scdc.entry.LabelEntry.IS_ACTIVE_LABEL",
      PIPELINE_KEY = "isPipelineEnabled",   // deprecated
      SLEEP_LABEL = "sleeping",
      IN_CLASS_LABEL = "in_class",
      EATING_LABEL = "having_a_meal",
      STUDYING_LABEL = "studying",
      DRINKING_LABEL = "drinking",
      ACCOMPANYING_LABEL = "accompanying",
      CONVERSING_LABEL = "conversing";
    // LABEL_TYPE = "label_type",
    // IS_LABELLED = "is_labelled";
  }

  public static interface LogKeys {
    public static final String
      DEBUG = "DEBUG";
  }

  public interface SystemSettingsKeys {

    String SCREEN_BRIGHTNESS = "screenBrightness";
    String ACCELEROMETER_ROTATION = "accelerometerRotation";
    String VOLUME_ALARM = "volumeAlarm";
    String VOLUME_MUSIC = "volumeMusic";
    String VOLUME_NOTIFICATION = "volumeNotification";
    String VOLUME_RING = "volumeRing";
    String VOLUME_SYSTEM = "volumeSystem";
    String VOLUME_VOICE = "volumeVoice";
  }

  public interface NetworkSettingsKeys {
    String AIR_PLANE_MODE_ON = "airPlaneModeOn";
    String MOBILE_DATA_ON = "mobileDataOn";
    String WIFI_ON = "wifiOn";
  }

  public interface ActivityRecognitionKeys {
    public static final String
      ACTION_AR_LOG = "kr.ac.snu.imlab.scdc.Broadcasting.action.AR_LOG",
      IN_VEHICLE_ACTIVITY = "in_a_vehicle",
      ON_BICYCLE_ACTIVITY = "on_a_bicycle",
      ON_FOOT_ACTIVITY = "on_foot",
      RUNNING_ACTIVITY = "running",
      STILL_ACTIVITY = "still",
      TILTING_ACTIVITY = "tilting",
      UNKNOWN_ACTIVITY = "unknown",
      WALKING_ACTIVITY = "walking",
      UNIDENTIFIABLE_ACTIVITY = "unidentifiable";
  }

  public interface AlertKeys {
    public static final String
      ACTION_ALERT = "kr.ac.snu.imlab.scdc.Broadcasting.action.ALERT",
      EXTRA_ALERT_ERROR_MESSAGE = "kr.ac.snu.imlab.scdc.Broadcasting.extra.ALERT_ERROR_MESSAGE",
      EXTRA_ALERT_ERROR_CODE = "kr.ac.snu.imlab.scdc.Broadcasting.extra.ALERT_ERROR_CODE";
  }

  public interface CalibrationKeys {
    public static final int
      CALIBRATION_STATUS_DEFAULT = 0,
      CALIBRATION_STATUS_TABLE_FRONT = 1,
      CALIBRATION_STATUS_TABLE_BACK = 2;
  }
}
