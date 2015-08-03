package kr.ac.snu.imlab.scdc.service.core;

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
