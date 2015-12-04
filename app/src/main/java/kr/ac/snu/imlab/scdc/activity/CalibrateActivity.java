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

  private static final int WAITING_TIME_IN_MILLIS = 5000;
  private static final int PERIOD_IN_MILLIS = 20;
  private static final int CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS = 5000;
  private static final int CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS = 5000;
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
      new CountDownTimer(WAITING_TIME_IN_MILLIS +
                         CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS +
                         CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS,
                         PERIOD_IN_MILLIS)
        // millisInFuture: The number of millis in the future from the call to start()
        //                 until the countdown is done and onFinish() is called.
        // countDownInterval: The interval along the way to receive
        //                    onTick(long) callbacks.
      {
      @Override
      public void onTick(long millisUntilFinished) {
//        Log.d(LogKeys.DEBUG, TAG + ".setUpCountDownTimer().onTick()/ millisUntilFinished=" +
//                millisUntilFinished);
        // 1. Waiting period before calibration
        if (millisUntilFinished > WAITING_TIME_IN_MILLIS +
                                  CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS) {
          // TODO: Implement task while calibrating
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.table_back);
          calibrateMessageTv.setText(R.string.waiting_to_calibrate);
          calibrateTimeRemainedTv.setText(
                  Long.toString((millisUntilFinished -
                                 WAITING_TIME_IN_MILLIS -
                                 CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS)
                  / 1000 + 1)
          );
        // 2. First calibration by putting the device
        //    on a table facing the rear side
        } else if (millisUntilFinished > WAITING_TIME_IN_MILLIS) {
          // TODO: Implement task while calibrating
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.table_front);
          calibrateMessageTv.setText(R.string.calibrating_table_front);
          calibrateTimeRemainedTv.setText(
                  Long.toString((millisUntilFinished -
                                WAITING_TIME_IN_MILLIS)
                  / 1000 + 1)
          );
        // 3. Second calibration by putting the device
        //    on a table facing the front side
        } else {
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.waiting);
          calibrateMessageTv.setText(R.string.calibrating_table_back);
          calibrateTimeRemainedTv.setText(
                  Long.toString(millisUntilFinished
                  / 1000 + 1)
          );
        }
      }

      @Override
      public void onFinish() {
        calibrateTimeRemainedTv.setText("0");
        // TODO: Implement task after finished calibrating
      }
    };
  }

  @Override
  protected void onDestroy() {
    countDownTimerCalibrating.cancel();
    super.onDestroy();
  }
  
}
