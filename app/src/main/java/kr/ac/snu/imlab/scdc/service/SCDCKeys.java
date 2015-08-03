package kr.ac.snu.imlab.scdc.service;

/**
 * Created by kilho on 15. 7. 28.
 */
public class SCDCKeys {

  public static interface Config {
    public static final String
      PIPELINE_NAME = "scdc",
      SCDC_BASIC_PREFS = "kr.ac.snu.imlab.scdc.Basic",
      SCDC_LABEL_PREFS = "kr.ac.snu.imlab.scdc.Label",
      DEFAULT_USERNAME = "imlab_user";
  }

  public static interface SharedPrefs {
    public static final String
      USERNAME = "userName",
      IS_FEMALE = "isFemale",
      NUM_LABELS = "total_num_labels",
      LABEL_NAME_PREFIX = "label_name_",
      START_LOGGING_TIME_PREFIX = "start_logging_time_",
      DATE_DUE_PREFIX = "date_due_";
  }

  public static interface Alarm {
    public static final String
      EXTRA_LABEL_NAME = "kr.ac.snu.imlab.scdc.service.alarm.LABEL_NAME",
      EXTRA_LABEL_ID = "kr.ac.snu.imlab.scdc.service.alarm.LABEL_ID",
      VIBRATE_ON_ALARM = "vibrate_on_alarm",
      ALARM_TIME = "alarm_time",
      REMINDER_TIME = "remider_time",
      DEFAULT_ALARM_TIME = "5",
      DEFAULT_REMINDER_TIME = "6";
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
