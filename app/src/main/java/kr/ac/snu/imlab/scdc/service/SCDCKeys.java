package kr.ac.snu.imlab.scdc.service;

/**
 * Created by kilho on 15. 7. 28.
 */
public class SCDCKeys {

  public static interface Config {
    public static final String
      PIPELINE_NAME = "scdc",
      SCDC_PREFS = "kr.ac.snu.imlab.scdc",
      DEFAULT_USERNAME = "imlab_user";
  }

  public static interface SharedPrefs {
    public static final String
      USERNAME = "userName",
      IS_FEMALE = "isFemale";
  }

  public static interface Alarm {
    public static final String
      EXTRA_LABEL = "kr.ac.snu.imlab.scdc.service.alarm.LABEL",
      EXTRA_LABEL_ID = "kr.ac.snu.imlab.scdc.service.alarm.LABEL_ID",
      VIBRATE_ON_ALARM = "vibrate_on_alarm",
      ALARM_TIME = "alarm_time",
      REMINDER_TIME = "remider_time",
      DEFAULT_ALARM_TIME = "15",
      DEFAULT_REMINDER_TIME = "6";
  }

  public static interface LabelKeys {
    public static final String
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
