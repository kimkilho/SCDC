package kr.ac.snu.imlab.ohpclient;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
import android.widget.ListView;
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


    private ListView mListView = null;
    private BaseAdapterEx mAdapter = null;
    // Probes list
    private ArrayList<ProbeEntry> probeEntries;

    // Run Data Collection button
    private ToggleButton enabledToggleButton;

    private Button archiveButton, updateDataCountButton;
    private TextView dataCountView;
    private ServiceConnection funfManagerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            funfManager = ((FunfManager.LocalBinder)service).getManager();
            Gson gson = funfManager.getGson();
            pipeline = (BasicPipeline)funfManager.getRegisteredPipeline(PIPELINE_NAME);

            for (int i = 0; i < mAdapter.getCount(); i++) {
              ProbeEntry probeEntry = mAdapter.getItem(i);
              probeEntry.setProbe(gson);
            }
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

                          for (int i = 0; i < mAdapter.getCount(); i++) {
                            ProbeEntry probeEntry = mAdapter.getItem(i);
                            Probe.Base probe = probeEntry.getProbe();
                            if (probeEntry.isEnabled()) {
                              funfManager.requestData(pipeline,
                                      probe.getConfig().get("@type"), null);
                              probe.registerPassiveListener(LaunchActivity.this);
                              Schedule probeSchedule = funfManager.getDataRequestSchedule(probe.getConfig(), pipeline);
                              // FIXME:
                              ((TextView)((ViewGroup)((ViewGroup)mListView.getChildAt(mListView.getFirstVisiblePosition()+i)).getChildAt(0)).getChildAt(1)).setText("Runs every " + probeSchedule.getInterval() + " seconds for " + probeSchedule.getDuration() + " seconds");
                            } else {
                              probe.unregisterPassiveListener(LaunchActivity.this);
                            }
                          }
                        } else {
                          funfManager.disablePipeline(PIPELINE_NAME);
                          ArrayList<View> scheduleTextViews = getViewsByTag((ViewGroup)findViewById(R.id.list_view), "PROBE_SCHEDULE");
                          for (View scheduleTextView : scheduleTextViews) {
                            ((TextView)scheduleTextView).setText(R.string.probe_disabled);
                          }
                        }
                    }
                }
            });

            // Set UI ready to use, by enabling buttons
            enabledToggleButton.setEnabled(true);
            archiveButton.setEnabled(true);
            updateDataCountButton.setEnabled(true);
            ArrayList<View> enabledCheckBoxes = getViewsByTag((ViewGroup)findViewById(R.id.list_view), "PROBE_CHECKBOX");
            for (View enabledCheckBox : enabledCheckBoxes) {
              ((CheckBox)enabledCheckBox).setEnabled(true);
            }
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

        probeEntries = new ArrayList<ProbeEntry>();
        probeEntries.add(new ProbeEntry(SmsProbe.class));
        probeEntries.add(new ProbeEntry(WifiProbe.class));
        Log.w("DEBUG", "probeEntries has number of elements : " + probeEntries.size());

        mAdapter = new BaseAdapterEx(this, probeEntries);

        mListView = (ListView)findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
//        mListView.setClickable(true);


        // Displays the count of rows in the data
        dataCountView = (TextView)findViewById(R.id.dataCountText);

        // Used to make interface changes on main thread
        handler = new Handler();

        enabledToggleButton = (ToggleButton)findViewById(R.id.enabledToggleButton);
        enabledToggleButton.setEnabled(false);

        // Add available probes to the probeEntries
        /*
        probeEntries = new ArrayList<ProbeEntry>();
        probeEntries.add(new ProbeEntry(WifiProbe.class, R.id.buttonWifiProbe,
                R.id.scheduleWifiProbe, R.id.enabledWifiProbe));
        probeEntries.add(new ProbeEntry(SmsProbe.class, R.id.buttonSmsProbe,
                R.id.scheduleSmsProbe, R.id.enabledSmsProbe));
        */

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

        // ListView item long click listener: register probe
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.w("DEBUG", "onItemLongClick()");
            if (pipeline.isEnabled()) {
              mAdapter.getItem(position).getProbe().registerListener(pipeline);
              Toast.makeText(getBaseContext(), "Register listener.",
                      Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
                      Toast.LENGTH_SHORT).show();
            }
            return true;
          }
        });

        // Bind to the service, to create the connection with FunfManager
        bindService(new Intent(this, FunfManager.class), funfManagerConn,
                BIND_AUTO_CREATE);
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
      if (pipeline.isEnabled()) {
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
      } else {
        Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
                Toast.LENGTH_SHORT).show();
      }
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
      for (ProbeEntry probeEntry : probeEntries) {
        probeEntry.getProbe().registerPassiveListener(LaunchActivity.this);
      }
      // Log.w(LogUtil.TAG, "wifiProbe: " + wifiProbe.getConfig() + ", " + wifiProbe.getState());
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

  private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
    ArrayList<View> views = new ArrayList<View>();
    final int childCount = root.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = root.getChildAt(i);
      if (child instanceof ViewGroup) {
        views.addAll(getViewsByTag((ViewGroup) child, tag));
      }

      final Object tagObj = child.getTag();
      if (tagObj != null && tagObj.equals(tag)) {
        views.add(child);
      }

    }
    return views;
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
