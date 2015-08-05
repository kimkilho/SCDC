package kr.ac.snu.imlab.scdc.service;

/**
 * Created by kilho on 15. 7. 28.
 */
public class SCDCKeys {

  public static interface Config {
    public static final String
      PIPELINE_NAME = "scdc",
      SCDC_PREFS = "kr.ac.snu.imlab.scdc.Label",
      DEFAULT_USERNAME = "imlab_user";

    public static final boolean
      DEFAULT_IS_FEMALE = false;
  }

  public static interface SharedPrefs {
    public static final String
      USERNAME = "userName",
      IS_FEMALE = "isFemale",
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
      DEFAULT_HOUR = "default_hour";
  }

  public static interface AlarmKeys {
    public static final String
      EXTRA_LABEL_NAME = "kr.ac.snu.imlab.scdc.service.alarm.LABEL_NAME",
      EXTRA_LABEL_ID = "kr.ac.snu.imlab.scdc.service.alarm.LABEL_ID",
      EXTRA_DATE_DUE = "kr.ac.snu.imlab.scdc.service.alarm.DATE_DUE",
      ALARM_EXTRA = "kr.ac.snu.imlab.scdc.service.alarm.LabelAlarm";

    public static final String
      DEFAULT_REMINDER_TIME = "6",
      DEFAULT_ALARM_TIME = "5",
      DEFAULT_HOUR_VALUE = "12";

    // Repeat constants
    public static final int
      MINUTES = 0,
      HOURS = 1,
      DAYS = 2,
      WEEKS = 3,
      MONTHS = 4,
      YEARS = 5;
  }

  public static interface LabelKeys {
    public static final String
      DEFAULT_NUM_LABELS = "5",
      ACTION_LABEL_LOG = "kr.ac.snu.imlab.scdc.Broadcasting.action" +
      ".LABEL_LOG",
      SLEEP_LABEL = "sleeping",
      IN_CLASS_LABEL = "in class",
      EATING_LABEL = "having a meal",
      STUDYING_LABEL = "studying",
      DRINKING_LABEL = "drinking";
    // LABEL_TYPE = "label_type",
    // IS_LABELLED = "is_labelled";
  }

  public static interface LogKeys {
    public static final String
      DEBUG = "DEBUG";
  }
}
