package kr.ac.snu.imlab.scdc.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LogKeys;

/**
 * Created by kilho on 15. 12. 4.
 */
public class CalibrateActivity extends ActionBarActivity {

  protected static final String TAG = "CalibrateActivity";

  private final int WAITING_TIME_IN_MILLISECONDS = 5000;
  private final int PERIOD_IN_MILLISECONDS = 20;
  private final int CALIBRATE_DURATION_IN_MILLISECONDS = 5000;
  private CountDownTimer countDownTimerCalibrating;
  private LinearLayout calibrateTimeRemainedLayout;
  private TextView calibrateMessageTv;
  private TextView calibrateTimeRemainedTv;

  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calibrate);
    calibrateTimeRemainedLayout = (LinearLayout)findViewById(R.id.ll_calibrate_time_remained);
    calibrateMessageTv = (TextView)findViewById(R.id.tv_calibrate_message);
    calibrateTimeRemainedTv = (TextView)findViewById(R.id.tv_calibrate_time_remained);
    startCalibrating();
  }

  private void startCalibrating() {
    // TODO: Implement data collection task for defined time duration
    setUpCountDownTimer();
    countDownTimerCalibrating.start();
  }

  private void setUpCountDownTimer() {
    countDownTimerCalibrating =
      new CountDownTimer(WAITING_TIME_IN_MILLISECONDS +
                         CALIBRATE_DURATION_IN_MILLISECONDS,
                         PERIOD_IN_MILLISECONDS)
        // millisInFuture: The number of millis in the future from the call to start()
        //                 until the countdown is done and onFinish() is called.
        // countDownInterval: The interval along the way to receive
        //                    onTick(long) callbacks.
      {
      @Override
      public void onTick(long millisUntilFinished) {
//        Log.d(LogKeys.DEBUG, TAG + ".setUpCountDownTimer().onTick()/ millisUntilFinished=" +
//                millisUntilFinished);
        if (millisUntilFinished > CALIBRATE_DURATION_IN_MILLISECONDS) {
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.waiting);
          calibrateMessageTv.setText(R.string.waiting_to_calibrate);
          calibrateTimeRemainedTv.setText(
                  Long.toString((millisUntilFinished - CALIBRATE_DURATION_IN_MILLISECONDS)
                  / 1000 + 1)
          );
        } else {

          // TODO: Implement task after finished calibrating
        }
      }

      @Override
      public void onFinish() {
        calibrateTimeRemainedTv.setText("0");
        // TODO:
      }
    };
  }

  @Override
  protected void onDestroy() {
    countDownTimerCalibrating.cancel();
    super.onDestroy();
  }
  
}
