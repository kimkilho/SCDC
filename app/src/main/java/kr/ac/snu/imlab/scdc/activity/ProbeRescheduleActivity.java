package kr.ac.snu.imlab.scdc.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.os.Handler;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonParser;

import java.math.BigDecimal;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.Schedule.BasicSchedule;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.DisplayName;
import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.core.SCDCPipeline;


public class ProbeRescheduleActivity extends ActionBarActivity {

    private Handler handler;
    private FunfManager funfManager = null;
    private SCDCPipeline pipeline = null;

    private Probe probe;
    private String probeClassName;
    private boolean isEnabled;
    private IJsonObject probeConfig;

    TextView probeName;
    EditText probeInterval;
    EditText probeDuration;
    CheckBox isEnabledCheckBox;

    private ServiceConnection funfManagerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          funfManager = ((FunfManager.LocalBinder)service).getManager();
          pipeline = (SCDCPipeline)funfManager.getRegisteredPipeline
                  (Config.PIPELINE_NAME);

          try {
            probeName.setText(Class.forName(probeClassName).getAnnotation(DisplayName.class).value());
            isEnabledCheckBox.setChecked(isEnabled);

            if (pipeline.isEnabled()) {
              // IMPORTANT: Check if there is already a schedule for the probe.
              Schedule probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
              // If there's not, generate a default schedule and load it:
              if (probeSchedule == null) {
                // TODO: Do I need the following line?
                funfManager.requestData(pipeline, probeConfig.get("@type"), null);
                probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
              }
              probeInterval.setText(String.valueOf(probeSchedule.getInterval().longValue()));
              probeDuration.setText(String.valueOf(probeSchedule.getDuration().longValue()));

              // The forms are editable only if the probe is checked as enabled
              if (isEnabled) {
                isEnabledCheckBox.setEnabled(true);
                probeInterval.setEnabled(true);
                probeDuration.setEnabled(true);
              } else {
                isEnabledCheckBox.setEnabled(false);
                probeInterval.setEnabled(false);
                probeDuration.setEnabled(false);
              }
            } else {
              isEnabledCheckBox.setEnabled(false);
              probeInterval.setEnabled(false);
              probeDuration.setEnabled(false);
            }

          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            funfManager = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_probe_reschedule);

      Intent rescheduleIntent = getIntent();
      Log.w("DEBUG", "getStringExtra()=" + rescheduleIntent.getStringExtra("PROBE"));
      JsonParser jp = new JsonParser();
      probeClassName = rescheduleIntent.getStringExtra("PROBE");
      isEnabled = rescheduleIntent.getBooleanExtra("IS_ENABLED", false);
      probeConfig = new IJsonObject(jp.parse("{\"@type\": \"" + probeClassName + "\"}").getAsJsonObject());

      probeName = (TextView)findViewById(R.id.probe_name);
      isEnabledCheckBox = (CheckBox)findViewById(R.id.probe_isEnabled);
      probeInterval = (EditText)findViewById(R.id.probe_interval);
      probeDuration = (EditText)findViewById(R.id.probe_duration);


      probeInterval.addTextChangedListener(new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          Schedule probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
          // Log.v("DEBUG", "probeSchedule.getDuration()=" + probeSchedule.getDuration() + ", probeSchedule.getInterval()=" + probeSchedule.getInterval());
          ((BasicSchedule)probeSchedule).setInterval(BigDecimal.valueOf(Double.parseDouble(s.toString())));
          funfManager.requestData(pipeline, probeConfig.get("@type"), probeSchedule);
          // probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
          // Log.w("DEBUG", "probeSchedule.getDuration()=" + probeSchedule.getDuration() + ", probeSchedule.getInterval()=" + probeSchedule.getInterval());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {  }
      });

      probeDuration.addTextChangedListener(new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          Schedule probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
          // Log.v("DEBUG", "probeSchedule.getDuration()=" + probeSchedule.getDuration() + ", probeSchedule.getInterval()=" + probeSchedule.getInterval());
          ((BasicSchedule)probeSchedule).setDuration(BigDecimal.valueOf(Double.parseDouble(s.toString())));
          funfManager.requestData(pipeline, probeConfig.get("@type"), probeSchedule);
          // probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
          // Log.w("DEBUG", "probeSchedule.getDuration()=" + probeSchedule.getDuration() + ", probeSchedule.getInterval()=" + probeSchedule.getInterval());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {  }

      });


      // Bind to the service, to create the connection with FunfManager
      bindService(new Intent(this, FunfManager.class), funfManagerConn,
              BIND_AUTO_CREATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_probe_reschedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
