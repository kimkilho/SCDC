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
}
