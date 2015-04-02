package kr.ac.snu.imlab.ohpclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.os.Handler;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonParser;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.DisplayName;


public class ProbeRescheduleActivity extends ActionBarActivity {
    public static final String PIPELINE_NAME = "ohpclient";

    private Handler handler;
    private FunfManager funfManager = null;
    private BasicPipeline pipeline = null;

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
            pipeline = (BasicPipeline)funfManager.getRegisteredPipeline(PIPELINE_NAME);

          try {
            probeName.setText(Class.forName(probeClassName).getAnnotation(DisplayName.class).value());
            isEnabledCheckBox.setChecked(isEnabled);

            if (pipeline.isEnabled()) {
              // TODO: Do I need the following line?
              funfManager.requestData(pipeline, probeConfig.get("@type"), null);
              Schedule probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
              probeInterval.setText(probeSchedule.getInterval().toString());
              probeDuration.setText(probeSchedule.getDuration().toString());

              // The forms are editable only if the probe is checked as enabled
              if (isEnabled) {
                probeInterval.setEnabled(true);
                probeDuration.setEnabled(true);
              } else {
                probeInterval.setEnabled(false);
                probeDuration.setEnabled(false);
              }
            } else {
              Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
                      Toast.LENGTH_SHORT).show();
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

      /*
      try {
        probeName.setText(Class.forName(probeClassName).getAnnotation(DisplayName.class).value());
        isEnabledCheckBox.setChecked(isEnabled);

        if (pipeline.isEnabled()) {
          // TODO: Do I need the following line?
          // funfManager.requestData(pipeline, probeConfig.get("@type"), null);
          Schedule probeSchedule = funfManager.getDataRequestSchedule(probeConfig, pipeline);
          probeInterval.setText(probeSchedule.getInterval().toString());
          probeDuration.setText(probeSchedule.getDuration().toString());

          // The forms are editable only if the probe is checked as enabled
          if (isEnabled) {
            probeInterval.setEnabled(true);
            probeDuration.setEnabled(true);
          } else {
            probeInterval.setEnabled(false);
            probeDuration.setEnabled(false);
          }
        } else {
          Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
                  Toast.LENGTH_SHORT).show();
        }

      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      */

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
