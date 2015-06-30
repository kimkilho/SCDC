package kr.ac.snu.imlab.ohpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.google.gson.JsonParser;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule.BasicSchedule;
import edu.mit.media.funf.config.ConfigUpdater.ConfigUpdateException;
import edu.mit.media.funf.config.HttpConfigUpdater;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.media.funf.probe.builtin.ProbeKeys.LabelKeys;


public class LaunchActivity extends ActionBarActivity implements DataListener {
  public static final String PIPELINE_NAME = "ohpclient";
  public static final String OHPCLIENT_PREFS = "kr.ac.snu.imlab.ohpclient";
  public static final String DEFAULT_USERNAME = "imlab_user";

  private Handler handler;
  private FunfManager funfManager = null;
  private BasicPipeline pipeline = null;

  // Username EditText and Button
  private EditText userName = null;
  private Button userNameButton = null;
  private RadioButton isMaleRadioButton = null;
  private RadioButton isFemaleRadioButton = null;
  // private CheckBox isFemaleCheckBox = null;
  boolean isEdited = false;

  // Probe list View
  private ListView mListView = null;
  private BaseAdapterExLabel mAdapter = null;
  // Probes list
  private ArrayList<ProbeEntry> probeEntries;
  private ArrayList<LabelEntry> labelEntries;

  // Run Data Collection button
  private ToggleButton enabledToggleButton;

  private Button archiveButton, updateDataCountButton, truncateDataButton;
  private TextView dataCountView;
  private ServiceConnection funfManagerConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      funfManager = ((FunfManager.LocalBinder)service).getManager();
      Gson gson = funfManager.getGson();
      pipeline = (BasicPipeline)funfManager.getRegisteredPipeline(PIPELINE_NAME);

      for (int i = 0; i < probeEntries.size(); i++) {
        probeEntries.get(i).setProbe(gson);
      }

      for (int i = 0; i < labelEntries.size(); i++) {
        labelEntries.get(i).setProbe(gson);
      }

      // This checkbox enables or disables the pipeline
      enabledToggleButton.setChecked(pipeline.isEnabled());
      enabledToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          if (funfManager != null) {
            if (isChecked) {
              funfManager.enablePipeline(PIPELINE_NAME);
              pipeline = (BasicPipeline)funfManager.getRegisteredPipeline(PIPELINE_NAME);

//              Log.w("DEBUG", "LaunchActivity/ pipeline.getDataRequests()="
//                      + pipeline.getDataRequests().toString());

              // Assigning new schedules to existing probes in
              // each probeEntries
              Map<String, BasicSchedule> newSchedules =
                      new HashMap<String, BasicSchedule>();
              List<JsonElement> newDataRequests = pipeline.getDataRequests();
              for (int i = 0; i < newDataRequests.size(); i++) {
                JsonObject currDataRequests = newDataRequests.get(i).getAsJsonObject();
                String currType = currDataRequests.get("@type").getAsString();
                JsonObject currSchedule = currDataRequests.get("@schedule").getAsJsonObject();
                BasicSchedule newSchedule = new BasicSchedule();
                if (currSchedule.get("interval") != null) {
                  newSchedule.setInterval(new BigDecimal
                  (currSchedule.get("interval").getAsString()));
                }
                if (currSchedule.get("duration") != null) {
                  newSchedule.setDuration(new BigDecimal
                  (currSchedule.get("duration").getAsString()));
                }
                if (currSchedule.get("opportunistic") != null) {
                  newSchedule.setOpportunistic(
                  currSchedule.get("opportunistic").getAsBoolean());
                }
                if (currSchedule.get("strict") != null) {
                  newSchedule.setStrict(
                  currSchedule.get("strict").getAsBoolean());
                }
                newSchedules.put(currType, newSchedule);
              }
              for (int i = 0; i < probeEntries.size(); i++) {
                ProbeEntry probeEntry = probeEntries.get(i);
                probeEntry.setSchedule(
                    newSchedules.get(probeEntry.getProbeClass()
                            .getName()));
              }

              // Request data from pipeline to normal probes
              for (int i = 0; i < probeEntries.size(); i++) {
                ProbeEntry probeEntry = probeEntries.get(i);
                Probe.Base probe = probeEntry.getProbe();
                if (probeEntry.isEnabled()) {
                  funfManager.requestData(pipeline,
                          probe.getConfig().get("@type"),
                          probeEntry.getSchedule());
                  probe.registerPassiveListener(LaunchActivity.this);
                } else {
                  probe.unregisterPassiveListener(LaunchActivity.this);
                }
              }

              // Request data from pipeline to label probe
              for (int i = 0; i < labelEntries.size(); i++) {
                LabelEntry labelEntry = labelEntries.get(i);
                Probe.Base labelProbe = labelEntry.getProbe();
                if (labelEntry.isEnabled()) {
                  funfManager.requestData(pipeline,
                          labelProbe.getConfig().get("@type"),
                          labelEntry.getSchedule());
                  // Log.w("DEBUG", "LaunchActivity/ @type=" + labelProbe.getConfig().get("@type"));
                  labelProbe.registerPassiveListener(LaunchActivity.this);
                } else {
                  labelProbe.unregisterPassiveListener(LaunchActivity.this);
                }
              }

              for (int i = 0; i < probeEntries.size(); i++) {
                ProbeEntry probeEntry = probeEntries.get(i);
                Probe.Base probe = probeEntry.getProbe();

//                Log.w("DEBUG", "LaunchActivity/ probe.getConfig()" +
//                        "=" + probe.getConfig() + ", " +
//                        "interval=" + probeEntry
//                        .getSchedule().getInterval() + ", " +
//                        "duration=" + probeEntry.getSchedule()
//                        .getDuration());
                // Log.w("DEBUG", "LaunchActivity/ probe.getConfig()=" + probe.getConfig() + ", interval=" + probeSchedule.getInterval() + ", duration=" + probeSchedule.getDuration());
              }

              archiveButton.setEnabled(true);
              updateDataCountButton.setEnabled(true);
              truncateDataButton.setEnabled(false);
            } else {
              /*
              for (int i = 0; i < probeEntries.size(); i++) {
                ProbeEntry probeEntry = probeEntries.get(i);
                Probe.Base probe = probeEntry.getProbe();
                probe.unregisterPassiveListener(LaunchActivity
                        .this);
              }
              */
              funfManager.disablePipeline(PIPELINE_NAME);
              archiveButton.setEnabled(false);
              updateDataCountButton.setEnabled(false);
              truncateDataButton.setEnabled(true);
            }
          }
          // Dynamically refresh the ListView items by calling mAdapter.getView() again.
          mAdapter.notifyDataSetChanged();

          // Broadcast the initial label log
          Intent intent = new Intent();
          intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
          intent.setAction(LabelKeys.ACTION_LABEL_LOG);
          for (int i = 0; i < labelEntries.size(); i++) {
            LabelEntry labelEntry = labelEntries.get(i);
            intent.putExtra(labelEntry.getName(), labelEntry.isLogged());
          }
          LaunchActivity.this.sendBroadcast(intent);
        }
      });

      // Set UI ready to use, by enabling buttons
      enabledToggleButton.setEnabled(true);
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

    // Make sure the keyboard only pops up
    // when a user clicks into an EditText
    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    // Set current username
    final SharedPreferences prefs = getSharedPreferences(OHPCLIENT_PREFS, Context.MODE_PRIVATE);
    userName = (EditText)findViewById(R.id.user_name);
    userName.setText(prefs.getString("userName", DEFAULT_USERNAME));
    isMaleRadioButton = (RadioButton)findViewById(R.id.radio_male);
    isFemaleRadioButton = (RadioButton)findViewById(R.id.radio_female);
    isMaleRadioButton.setChecked(!prefs.getBoolean("isFemale", false));
    isFemaleRadioButton.setChecked(prefs.getBoolean("isFemale", false));
    userName.setEnabled(false);
    isMaleRadioButton.setEnabled(false);
    isFemaleRadioButton.setEnabled(false);
    isEdited = false;

    userNameButton = (Button)findViewById(R.id.user_name_btn);
    userNameButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // If it's currently not being edited now:
            if (!isEdited) {
                userName.setEnabled(true);
                isMaleRadioButton.setEnabled(true);
                isFemaleRadioButton.setEnabled(true);
                isEdited = true;
                userNameButton.setText("Save");
            // If it has just finished being edited:
            } else {
                prefs.edit().putString("userName", userName.getText().toString()).apply();
                prefs.edit().putBoolean("isFemale", isFemaleRadioButton.isChecked()).apply();
                Log.w("DEBUG", "userName=" + prefs.getString("userName", DEFAULT_USERNAME));
                Log.w("DEBUG", "isFemale=" + prefs.getBoolean("isFemale", false));
                userName.setEnabled(false);
                isMaleRadioButton.setEnabled(false);
                isFemaleRadioButton.setEnabled(false);
                isEdited = false;
                userNameButton.setText("Modify");
            }
        }
    });


    // The list of probes available
    probeEntries = new ArrayList<ProbeEntry>();
      // Device Probes
      probeEntries.add(new ProbeEntry(BatteryProbe.class));
      // Environment Probes
      probeEntries.add(new ProbeEntry(LightSensorProbe.class));
      probeEntries.add(new ProbeEntry(MagneticFieldSensorProbe.class));
      probeEntries.add(new ProbeEntry(AudioFeaturesProbe.class));
      // Motion Probes
      probeEntries.add(new ProbeEntry(AccelerometerSensorProbe.class));
      probeEntries.add(new ProbeEntry(GyroscopeSensorProbe.class));
      probeEntries.add(new ProbeEntry(OrientationSensorProbe.class));
      // Positioning Probes
      probeEntries.add(new ProbeEntry(SimpleLocationProbe.class));
      probeEntries.add(new ProbeEntry(BluetoothProbe.class));
      // Device Interaction
      probeEntries.add(new ProbeEntry(RunningApplicationsProbe.class));
      probeEntries.add(new ProbeEntry(ScreenProbe.class));
    for (int i = 0; i < probeEntries.size(); i++) {
      probeEntries.get(i).setEnabled(true);
    }

    // The list of labels available
    labelEntries = new ArrayList<LabelEntry>();
    labelEntries.add(new LabelEntry(LabelKeys.SLEEP_LABEL, LabelProbe.class));
    labelEntries.add(new LabelEntry(LabelKeys.IN_CLASS_LABEL, LabelProbe.class));
    for (int i = 0; i < labelEntries.size(); i++) {
      labelEntries.get(i).setEnabled(true);
    }

    Log.w("DEBUG", "labelEntries has number of elements : " + labelEntries.size());

    mAdapter = new BaseAdapterExLabel(this, labelEntries);

    mListView = (ListView)findViewById(R.id.label_list_view);
    mListView.setAdapter(mAdapter);

    // Displays the count of rows in the data
    dataCountView = (TextView)findViewById(R.id.dataCountText);

    // Used to make interface changes on main thread
    handler = new Handler();

    enabledToggleButton = (ToggleButton)findViewById(R.id.enabledToggleButton);
    enabledToggleButton.setEnabled(false);

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

    // Truncate the data
    truncateDataButton = (Button)findViewById(R.id.truncateDataButton);
    truncateDataButton.setEnabled(false);
    truncateDataButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dropAndCreateTable();
        dataCountView.setText("Data Count: 0");

        // Broadcast the initial label log again
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.setAction(LabelKeys.ACTION_LABEL_LOG);
        for (int i = 0; i < labelEntries.size(); i++) {
          LabelEntry labelEntry = labelEntries.get(i);
          intent.putExtra(labelEntry.getName(), labelEntry.isLogged());
        }
        LaunchActivity.this.sendBroadcast(intent);

        // Update probe schedules of pipeline
        HttpConfigUpdater hcu = new HttpConfigUpdater();
        hcu.setUrl("http://imlab-ws2.snu.ac.kr:7000/config");
        pipeline.setUpdate(hcu);
        handler.post(new Runnable() {
          @Override
          public void run() {
            pipeline.onRun(BasicPipeline.ACTION_UPDATE, null);
          }
        });

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
  public void onResume() {
    super.onResume();

    mAdapter = new BaseAdapterExLabel(this, labelEntries);
    mListView = (ListView)findViewById(R.id.label_list_view);
    mListView.setAdapter(mAdapter);
    mAdapter.notifyDataSetChanged();
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
      final long dbSize = new File(db.getPath()).length();  // in bytes
      Cursor mcursor = db.rawQuery(TOTAL_COUNT_SQL, null);
      mcursor.moveToFirst();
      final int count = mcursor.getInt(0);
      // Update interface on main thread
      runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
          dataCountView.setText("Data Count: " + count + "\n Size: " + Math.round((dbSize/(1048576.0))*100.0)/100.0 + " MB");
      }
    });
      /*
    } else {
      Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
              Toast.LENGTH_SHORT).show();
              */
    }
  }
  // Table truncation SQL statement
  // FIXME: Change the query below to 'DROP' statement if needed
  private static final String TRUNCATE_TABLE_SQL = "DELETE FROM " + NameValueDatabaseHelper.DATA_TABLE.name;
  // private static final String CREATE_TABLE_SQL = "" + NameValueDatabaseHelper.DATA_TABLE.name;

  /**
   * @author Kilho Kim
   * Truncate table of the database of the pipeline.
   */
  private void dropAndCreateTable() {
    SQLiteDatabase db = pipeline.getWritableDb();
    NameValueDatabaseHelper databaseHelper = (NameValueDatabaseHelper)pipeline.getDatabaseHelper();
    databaseHelper.dropAndCreateDataTable(db);
    updateScanCount();
    Toast.makeText(getBaseContext(), "Dropped and re-created data table.",
            Toast.LENGTH_LONG).show();
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
      if (probeEntry.isEnabled())
        probeEntry.getProbe().registerPassiveListener(LaunchActivity.this);
    }
    // Log.w(LogUtil.TAG, "wifiProbe: " + wifiProbe.getConfig() + ", " + wifiProbe.getState());
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


  // DEBUG:
  public FunfManager getActivityFunfManager() {
                                                  return funfManager;
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
