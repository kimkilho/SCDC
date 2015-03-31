package kr.ac.snu.imlab.ohpclient;

import android.support.v7.app.ActionBarActivity;
// import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapterFactory;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.config.RuntimeTypeAdapterFactory;
import edu.mit.media.funf.datasource.StartableDataSource;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.DataListener;
import edu.mit.media.funf.probe.builtin.*;
import edu.mit.media.funf.storage.NameValueDatabaseHelper;
import edu.mit.media.funf.probe.Probe.DisplayName;
import edu.mit.media.funf.util.LogUtil;
import edu.mit.media.funf.util.StringUtil;

import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;


public class LaunchActivity extends ActionBarActivity implements DataListener {
    public static final String PIPELINE_NAME = "ohpclient";

    private Handler handler;
    private FunfManager funfManager = null;
    private BasicPipeline pipeline = null;

    // Probes
    private WifiProbe wifiProbe;
    private SmsProbe smsProbe;

    // Run Data Collection button
    private ToggleButton enabledToggleButton;

    // Probe schedules
    TextView scheduleWifiProbe;
    TextView scheduleSmsProbe;

    // Probe checkboxes
    private CheckBox enabledWifiProbe;
    private CheckBox enabledSmsProbe;

    private Button archiveButton, updateDataCountButton;
    private TextView dataCountView;
    private ServiceConnection funfManagerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            funfManager = ((FunfManager.LocalBinder)service).getManager();
            Gson gson = funfManager.getGson();
            pipeline = (BasicPipeline)funfManager.getRegisteredPipeline(PIPELINE_NAME);

            wifiProbe = gson.fromJson(new JsonObject(), WifiProbe.class);
            smsProbe = gson.fromJson(new JsonObject(), SmsProbe.class);
            // Log.w(LogUtil.TAG, "wifiProbe: " + wifiProbe.getConfig() + ", " + wifiProbe.getState());

            // This checkbox enables or disables the pipeline
            enabledToggleButton.setChecked(pipeline.isEnabled());
            enabledToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (funfManager != null) {
                        if (isChecked) {
                            funfManager.enablePipeline(PIPELINE_NAME);
                            pipeline = (BasicPipeline) funfManager.getRegisteredPipeline(PIPELINE_NAME);
                            // Probe probe = getGson().fromJson(wifiProbe.getConfig(), wifiProbe.getClass());

                            if (enabledWifiProbe.isChecked()) {
                                funfManager.requestData(pipeline, wifiProbe.getConfig().get("@type"), null);
                                wifiProbe.registerPassiveListener(LaunchActivity.this);
                                Schedule wifiSchedule = funfManager.getDataRequestSchedule(wifiProbe.getConfig(), pipeline);
                                scheduleWifiProbe.setText("   Runs every " + wifiSchedule.getInterval() + " seconds \n for " + wifiSchedule.getDuration() + " seconds");
                            } else {
                                wifiProbe.unregisterPassiveListener(LaunchActivity.this);
                            }

                            if (enabledSmsProbe.isChecked()) {
                                funfManager.requestData(pipeline, smsProbe.getConfig().get("@type"), null);
                                smsProbe.registerPassiveListener(LaunchActivity
                                        .this);
                                Schedule smsSchedule = funfManager.getDataRequestSchedule(smsProbe.getConfig(), pipeline);
                                scheduleSmsProbe.setText("   Runs every " + smsSchedule.getInterval() + " seconds \n for " + smsSchedule.getDuration() + " seconds");
                            } else {
                                smsProbe.unregisterPassiveListener(LaunchActivity.this);
                            }
                        } else {
                            funfManager.disablePipeline(PIPELINE_NAME);
                            scheduleWifiProbe.setText(R.string.probe_disabled);
                            scheduleSmsProbe.setText(R.string.probe_disabled);
                        }
                    }
                }
            });

            // Set UI ready to use, by enabling buttons
            enabledToggleButton.setEnabled(true);
            archiveButton.setEnabled(true);
            updateDataCountButton.setEnabled(true);
            enabledWifiProbe.setEnabled(true);
            enabledSmsProbe.setEnabled(true);
            reloadProbeList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            funfManager = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // Displays the count of rows in the data
        dataCountView = (TextView)findViewById(R.id.dataCountText);

        // Used to make interface changes on main thread
        handler = new Handler();

        enabledToggleButton = (ToggleButton)findViewById(R.id.enabledToggleButton);
        enabledToggleButton.setEnabled(false);

        // Wifi Probe : Nearby Wifi Deices
        enabledWifiProbe = (CheckBox)findViewById(R.id.enabledWifiProbe);
        enabledWifiProbe.setEnabled(false);
        scheduleWifiProbe = (TextView)findViewById(R.id.scheduleWifiProbe);
        scheduleWifiProbe.setText(R.string.probe_disabled);

        // Sms Probe : SMS Log
        enabledSmsProbe = (CheckBox)findViewById(R.id.enabledSmsProbe);
        enabledSmsProbe.setEnabled(false);
        scheduleSmsProbe = (TextView)findViewById(R.id.scheduleSmsProbe);
        scheduleSmsProbe.setText(R.string.probe_disabled);


        // Runs an archive if pipeline is enabled
        archiveButton = (Button)findViewById(R.id.archiveButton);
        archiveButton.setEnabled(false);
        archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pipeline.isEnabled()) {

                    // Wait 1 second for archive to finish, then refresh the UI
                    // (Note: this is kind of a hack since archiving is seamless
                    //         and there are no messages when it occurs)
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pipeline.onRun(BasicPipeline.ACTION_ARCHIVE, null);
                            pipeline.onRun(BasicPipeline.ACTION_UPLOAD, null);
                            Toast.makeText(getBaseContext(), "Archived!",
                                Toast.LENGTH_SHORT).show();
                            updateScanCount();
                        }
                    // }, 1000L);
                    }, 1000L);
                } else {
                    Toast.makeText(getBaseContext(), "Pipeline is not enabled",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update the data count
        updateDataCountButton = (Button)findViewById(R.id.updateDataCountButton);
        updateDataCountButton.setEnabled(false);
        updateDataCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateScanCount();
            }
        });

        // Bind to the service, to create the connection with FunfManager
        bindService(new Intent(this, FunfManager.class), funfManagerConn,
                BIND_AUTO_CREATE);
    }

    public void onClickProbeRegister(View v) {
        if (pipeline.isEnabled()) {
            // Manually register the pipeline
            // switch (v.getId()) {
            switch (v.getId()) {
                case R.id.buttonWifiProbe:
                    wifiProbe.registerListener(pipeline);
                    break;
                case R.id.buttonSmsProbe:
                    smsProbe.registerListener(pipeline);
                    break;
                default:
                    break;
            }
        } else {
            Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // FIXME:
    public void onClickProbeReschedule(View v, IJsonObject probeConfig,
                                       boolean isEnabled) {
        if (!pipeline.isEnabled()) {
            Intent rescheduleIntent = new Intent(this,
                    ProbeRescheduleActivity.class);
            rescheduleIntent.putExtra("PROBE", probeConfig.toString());
            rescheduleIntent.putExtra("IS_ENABLED", isEnabled);
            startActivity(rescheduleIntent);
        } else {
            Toast.makeText(getBaseContext(),
                    "Pipeline should be disabled to reschedule the probe.",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(funfManagerConn);
    }

    private static final String TOTAL_COUNT_SQL = "SELECT COUNT(*) FROM " +
            NameValueDatabaseHelper.DATA_TABLE.name;
    /**
     * Queries the database of the pipeline to determine how many rows of data we have recorded so far.
     */
    private void updateScanCount() {
        // Query the pipeline db for the count of rows in the data table
        SQLiteDatabase db = pipeline.getDb();
        Cursor mcursor = db.rawQuery(TOTAL_COUNT_SQL, null);
        mcursor.moveToFirst();
        final int count = mcursor.getInt(0);
        // Update interface on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataCountView.setText("Data Count: " + count);
            }
        });
    }

    @Override
    public void onDataReceived(IJsonObject probeConfig, IJsonObject data) {
        // Not doing anything with the data
        // As an exercise, you could display this to the screen
        // (Remember to make UI changes on the main thread)
        // Toast.makeText(getBaseContext(), probeConfig.toString() + " | " + data.toString(),
                // Toast.LENGTH_LONG).show();
        // Log.w(LogUtil.TAG, "probeConfig: " + probeConfig + ", data: " + data);
        updateScanCount();
    }

    @Override
    public void onDataCompleted(IJsonObject probeConfig, JsonElement checkpoint) {
        updateScanCount();
        // Re-register to keep listening after probe completes.
        wifiProbe.registerPassiveListener(LaunchActivity.this);
        smsProbe.registerPassiveListener(LaunchActivity.this);
//            Log.w(LogUtil.TAG, "wifiProbe: " + wifiProbe.getConfig() + ", " + wifiProbe.getState());
    }


    private void reloadProbeList() {
        // Load probe list view from config
        if (pipeline != null && pipeline instanceof BasicPipeline) {
            List<String> names = new ArrayList<String>();
            for (JsonElement el : ((BasicPipeline)pipeline).getDataRequests()) {
                String probeClassName = el.isJsonPrimitive() ? el.getAsString() : el.getAsJsonObject().get(RuntimeTypeAdapterFactory.TYPE).getAsString();
                DisplayName probeDisplayName = null;
                try {
                    probeDisplayName = Class.forName(probeClassName).getAnnotation(DisplayName.class);
                } catch (ClassNotFoundException e) {

                }
                String name = "Unknown";
                if (probeDisplayName == null) {
                    String[] parts = probeClassName.split("\\.");
                    if (parts.length == 0) {
                        Log.d(LogUtil.TAG, "Bad probe type: '" + probeClassName + "'");
                    } else {
                        name = parts[parts.length - 1].replace("Probe", "");
                    }
                } else {
                    name = probeDisplayName.value();
                }
                names.add(name);
            }
            ((TextView)findViewById(R.id.probe_list)).setText(StringUtil.join(names, ", "));
        } else {
            ((TextView)findViewById(R.id.probe_list)).setText("Unknown...");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Hkandle action bar item clicks here. The action bar will
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
