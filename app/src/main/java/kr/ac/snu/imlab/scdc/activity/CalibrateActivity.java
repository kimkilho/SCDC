package kr.ac.snu.imlab.scdc.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.CalibrationKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LogKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCManager;
import kr.ac.snu.imlab.scdc.service.core.SCDCPipeline;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
 * Created by kilho on 15. 12. 4.
 */
public class CalibrateActivity extends ActionBarActivity {

  protected static final String TAG = "CalibrateActivity";

  private static final int INITIAL_WAITING_TIME_IN_MILLIS = 7000;
  private static final int MIDDLE_WAITING_TIME_IN_MILLIS = 5000;
  private static final int END_WAITING_TIME_IN_MILLIS = 3000;
  private static final int PERIOD_IN_MILLIS = 100;
  private static final int CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS = 5000;
  private static final int CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS = 5000;
  private CountDownTimer countDownTimerCalibrating;
  private LinearLayout calibrateTimeRemainedLayout;
  private TextView calibrateMessageTv;
  private TextView calibrateTimeRemainedTv;

  private SharedPrefsHandler spHandler;
  private SCDCManager funfManager;
  private SCDCPipeline pipeline;

  private ServiceConnection funfManagerConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      funfManager = ((SCDCManager.LocalBinder)service).getManager();
      pipeline = (SCDCPipeline)funfManager.getRegisteredPipeline(
                                           Config.PIPELINE_NAME);
//      Log.d(LogKeys.DEBUG, TAG+".funfManagerConn.onServiceConnected(): " +
//              "pipeline.getName()=" + pipeline.getName() +
//              ", pipeline.isEnabled()=" + pipeline.isEnabled() +
//              ", pipeline.getDatabaseHelper()=" + pipeline.getDatabaseHelper());

      spHandler.setSensorOn(true);
      funfManager.disablePipeline(pipeline.getName());
      funfManager.enablePipeline(pipeline.getName());
      pipeline = (SCDCPipeline)funfManager.getRegisteredPipeline(
              Config.PIPELINE_NAME);
      Log.d(LogKeys.DEBUG, TAG + ".funfManagerConn.onServiceConnected(): " +
              "pipeline.getName()=" + pipeline.getName() +
              ", pipeline.isEnabled()=" + pipeline.isEnabled() +
              ", pipeline.getDatabaseHelper()=" + pipeline.getDatabaseHelper());
      startCalibrating();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      funfManager.disablePipeline(pipeline.getName());
      pipeline = (SCDCPipeline)funfManager.getRegisteredPipeline(
                  Config.PIPELINE_NAME);
      Log.d(LogKeys.DEBUG, TAG+".funfManagerConn.onServiceDisconnected(): " +
              "pipeline.getName()=" + pipeline.getName() +
              ", pipeline.isEnabled()=" + pipeline.isEnabled() +
              ", pipeline.getDatabaseHelper()=" + pipeline.getDatabaseHelper());
      funfManager = null;
      pipeline = null;
      spHandler.setSensorOn(false);
    }
  };
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calibrate);
    calibrateTimeRemainedLayout = (LinearLayout)findViewById(R.id.ll_calibrate_time_remained);
    calibrateMessageTv = (TextView)findViewById(R.id.tv_calibrate_message);
    calibrateTimeRemainedTv = (TextView)findViewById(R.id.tv_calibrate_time_remained);

    spHandler = SharedPrefsHandler.getInstance(this,
                  Config.SCDC_PREFS, Context.MODE_PRIVATE);

    bindService(new Intent(this, SCDCManager.class),
            funfManagerConn, BIND_AUTO_CREATE);
  }

  private void startCalibrating() {
    setUpCountDownTimer();
    countDownTimerCalibrating.start();
  }

  private void setUpCountDownTimer() {
    countDownTimerCalibrating =
      new CountDownTimer(INITIAL_WAITING_TIME_IN_MILLIS +
                         CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS +
                         MIDDLE_WAITING_TIME_IN_MILLIS +
                         CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS +
                         END_WAITING_TIME_IN_MILLIS,
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
        // 1. First Waiting period before Table-Front calibration
        if (millisUntilFinished > CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS +
                                  MIDDLE_WAITING_TIME_IN_MILLIS +
                                  CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS +
                                  END_WAITING_TIME_IN_MILLIS) {
          // Change current calibration status
          if (spHandler.getCalibrationStatus() !=
                  CalibrationKeys.CALIBRATION_STATUS_DEFAULT) {
            spHandler.setCalibrationStatus(
                    CalibrationKeys.CALIBRATION_STATUS_DEFAULT);
          }
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.waiting);
          calibrateMessageTv.setText(R.string.waiting_to_calibrate_table_front);
          calibrateTimeRemainedTv.setText(
                  Long.toString((millisUntilFinished -
                                 CALIBRATE_TABLE_FRONT_DURATION_IN_MILLIS -
                                 MIDDLE_WAITING_TIME_IN_MILLIS -
                                 CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS -
                                 END_WAITING_TIME_IN_MILLIS)
                  / 1000 + 1)
          );
        // 2. First calibration by putting the device
        //    on a table facing the front side
        } else if (millisUntilFinished > MIDDLE_WAITING_TIME_IN_MILLIS +
                                         CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS +
                                         END_WAITING_TIME_IN_MILLIS) {
          // Change current calibration status
          if (spHandler.getCalibrationStatus() !=
              CalibrationKeys.CALIBRATION_STATUS_TABLE_FRONT) {
            spHandler.setCalibrationStatus(
              CalibrationKeys.CALIBRATION_STATUS_TABLE_FRONT);
          }
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.table_front);
          calibrateMessageTv.setText(R.string.calibrating_table_front);
          calibrateTimeRemainedTv.setText(
                  Long.toString((millisUntilFinished -
                                 MIDDLE_WAITING_TIME_IN_MILLIS -
                                 CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS -
                                 END_WAITING_TIME_IN_MILLIS)
                  / 1000 + 1)
          );
        // 3. Second Waiting period before Table-Back calibration
        } else if (millisUntilFinished > CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS +
                                         END_WAITING_TIME_IN_MILLIS) {
          // Change current calibration status
          if (spHandler.getCalibrationStatus() !=
                  CalibrationKeys.CALIBRATION_STATUS_DEFAULT) {
            spHandler.setCalibrationStatus(
                    CalibrationKeys.CALIBRATION_STATUS_DEFAULT);
          }
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.waiting);
          calibrateMessageTv.setText(R.string.waiting_to_calibrate_table_back);
          calibrateTimeRemainedTv.setText(
                  Long.toString((millisUntilFinished -
                                 CALIBRATE_TABLE_BACK_DURATION_IN_MILLIS -
                                 END_WAITING_TIME_IN_MILLIS)
                  / 1000 + 1)
          );
        // 4. Second calibration by putting the device
        //    on a table facing the back side
        } else if (millisUntilFinished > END_WAITING_TIME_IN_MILLIS) {
          // Change current calibration status
          if (spHandler.getCalibrationStatus() !=
                  CalibrationKeys.CALIBRATION_STATUS_TABLE_BACK) {
            spHandler.setCalibrationStatus(
                    CalibrationKeys.CALIBRATION_STATUS_TABLE_BACK);
          }
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.table_back);
          calibrateMessageTv.setText(R.string.calibrating_table_back);
          calibrateTimeRemainedTv.setText(
                  Long.toString((millisUntilFinished -
                                 END_WAITING_TIME_IN_MILLIS)
                  / 1000 + 1)
          );
        // 5. Final Waiting period at the end of calibration
        } else {
          // Change current calibration status
          if (spHandler.getCalibrationStatus() !=
                  CalibrationKeys.CALIBRATION_STATUS_DEFAULT) {
            spHandler.setCalibrationStatus(
                    CalibrationKeys.CALIBRATION_STATUS_DEFAULT);
          }
          calibrateTimeRemainedLayout.setBackgroundResource(R.color.waiting);
          calibrateMessageTv.setText(R.string.end_calibration);
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
//        calibrateTimeRemainedLayout.setVisibility(View.INVISIBLE);
        finish();
      }
    };
  }

  @Override
  protected void onDestroy() {
    spHandler.setSensorOn(false);
    spHandler.setIsCalibrated(true);
    countDownTimerCalibrating.cancel();
    unbindService(funfManagerConn);
    super.onDestroy();
  }
  
}
