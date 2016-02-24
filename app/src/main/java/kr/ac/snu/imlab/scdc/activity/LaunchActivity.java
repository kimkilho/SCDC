package kr.ac.snu.imlab.scdc.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import edu.mit.media.funf.config.ConfigUpdater.ConfigUpdateException;
import edu.mit.media.funf.config.Configurable;
import edu.mit.media.funf.config.HttpConfigUpdater;
import edu.mit.media.funf.util.EqualsUtil;
import kr.ac.snu.imlab.scdc.service.core.SCDCManager;
import kr.ac.snu.imlab.scdc.service.alarm.AlarmButlerService;
import kr.ac.snu.imlab.scdc.service.alarm.LabelAlarm;
import kr.ac.snu.imlab.scdc.service.alarm.WakefulIntentService;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCPipeline;
import edu.mit.media.funf.storage.FileArchive;

import android.os.IBinder;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.ArrayList;

import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LabelKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LogKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.AlertKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCService;
import kr.ac.snu.imlab.scdc.service.storage.MultipartEntityArchive;
import kr.ac.snu.imlab.scdc.service.storage.SCDCDatabaseHelper;
import kr.ac.snu.imlab.scdc.service.storage.SCDCUploadService;
import kr.ac.snu.imlab.scdc.service.storage.ZipArchive;
import kr.ac.snu.imlab.scdc.adapter.BaseAdapterExLabel;
import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;


public class LaunchActivity extends ActionBarActivity
                            implements OnDataReceivedListener {

  protected static final String TAG = "LaunchActivity";

  @Configurable
  // FIXME: Change below to false when publishing
  public static boolean DEBUGGING = false;

  @Configurable
  protected int version = 5;

  // FIXME: The list of labels available
  @Configurable
  public static final String[] labelNames = {
          LabelKeys.WITH_2_TO_3_LABEL,
          LabelKeys.WITH_4_TO_6_LABEL,
          LabelKeys.WITH_OVER_7_LABEL,
          LabelKeys.SLEEP_LABEL,
          LabelKeys.IN_CLASS_LABEL,
          LabelKeys.EATING_LABEL,
          LabelKeys.STUDYING_LABEL,
          LabelKeys.DRINKING_LABEL,
          LabelKeys.CONVERSING_LABEL
  };

  // FIXME: The list of 'active' labels
  @Configurable
  public static final String[] activeLabelNames = {
          LabelKeys.WITH_2_TO_3_LABEL,
          LabelKeys.WITH_4_TO_6_LABEL,
          LabelKeys.WITH_OVER_7_LABEL,
          LabelKeys.IN_CLASS_LABEL,
          LabelKeys.EATING_LABEL,
          LabelKeys.STUDYING_LABEL,
          LabelKeys.DRINKING_LABEL,
          LabelKeys.CONVERSING_LABEL
  };

  private Handler handler;
  private SharedPrefsHandler spHandler;

  // Username EditText and Button
  private EditText userName;
  private Button userNameButton;
  private RadioButton isMaleRadioButton;
  private RadioButton isFemaleRadioButton;
  boolean isEdited = false;

  // Probe list View
  private ListView mListView;
  private BaseAdapterExLabel mAdapter;
  // Labels list
  private ArrayList<LabelEntry> labelEntries;

  // Run Data Collection button
  private ToggleButton enabledToggleButton;

  // Run Push notification button
  private ToggleButton reminderToggleButton;

  private Button archiveButton, truncateDataButton;
  private TextView dataCountView;
  private ImageView receivingDataImageView;

  private BroadcastReceiver alertReceiver;

  private SCDCManager scdcManager;
  private SCDCPipeline pipeline;

  private SCDCService scdcService;

  /**
   * Alertdialog which shows up when there is a problem with connection
   * to Google API.
   */
  private AlertDialog mAlertDialog;

  private ServiceConnection scdcServiceConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      scdcService = ((SCDCService.LocalBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      scdcService = null;
    }
  };

  private ServiceConnection scdcManagerConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      // IMPORTANT: Should disable pipeline here,
      //            as unbindService in SCDCService isn't called
      //            if LaunchActivity binds to SCDCManager right after it
      scdcManager = ((SCDCManager.LocalBinder) service).getManager();
      pipeline = (SCDCPipeline) scdcManager
              .getRegisteredPipeline(Config.PIPELINE_NAME);
      scdcManager.disablePipeline(Config.PIPELINE_NAME);
      pipeline.reloadDbHelper(scdcManager);

      Log.d(LogKeys.DEBUG, TAG+".scdcManagerConn.onServiceConnected(): " +
                           "pipeline.getName()=" + pipeline.getName() +
                           ", pipeline.getDatabaseHelper()=" +
                           pipeline.getDatabaseHelper());

      pipeline.setDataReceivedListener(LaunchActivity.this);

      // Update probe schedules of pipeline
      Log.d(LogKeys.DEBUG, TAG+".scdcManagerConn.onServiceConnected(): "
                            + "spHandler.isActiveLabelOn()=" +
                            spHandler.isActiveLabelOn());
      changeConfig(spHandler.isActiveLabelOn());

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      scdcManager = null;
      pipeline = null;
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    spHandler = SharedPrefsHandler.getInstance(this,
            Config.SCDC_PREFS, Context.MODE_PRIVATE);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_launch);

    // Make sure the keyboard only pops up
    // when a user clicks into an EditText
    this.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    // Set current username
    userName = (EditText) findViewById(R.id.user_name);
    userName.setText(spHandler.getUsername());
    isMaleRadioButton = (RadioButton) findViewById(R.id.radio_male);
    isFemaleRadioButton = (RadioButton) findViewById(R.id.radio_female);
    isMaleRadioButton.setChecked(!spHandler.getIsFemale());
    isFemaleRadioButton.setChecked(spHandler.getIsFemale());
    userName.setEnabled(false);
    isMaleRadioButton.setEnabled(false);
    isFemaleRadioButton.setEnabled(false);
    isEdited = false;

    userNameButton = (Button) findViewById(R.id.user_name_btn);
    userNameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // If it's currently not being edited now:
        if (!isEdited) {
            userName.setEnabled(true);
            isMaleRadioButton.setEnabled(true);
            isFemaleRadioButton.setEnabled(true);
            isEdited = true;
            userNameButton.setText(getString(R.string.save));
            // If it has just finished being edited:
        } else {
          spHandler.setUsername(userName.getText().toString());
          spHandler.setIsFemale(isFemaleRadioButton.isChecked());
          userName.setEnabled(false);
          isMaleRadioButton.setEnabled(false);
          isFemaleRadioButton.setEnabled(false);
          isEdited = false;
          userNameButton.setText(getString(R.string.edit));
        }
      }
    });

    // The list of labels available
    labelEntries = new ArrayList<LabelEntry>(labelNames.length);
    for (int i = 0; i < labelNames.length; i++) {
      labelEntries.add(new LabelEntry(i, labelNames[i],
                          LaunchActivity.this, Config.SCDC_PREFS));
    }

    // Put the total number of labels into SharedPreferences
    spHandler.setNumLabels(labelEntries.size());

    mAdapter = new BaseAdapterExLabel(this, labelEntries);

    mListView = (ListView) findViewById(R.id.label_list_view);
    mListView.setAdapter(mAdapter);

    // Displays the count of rows in the data
    dataCountView = (TextView) findViewById(R.id.dataCountText);

    // Used to make interface changes on main thread
    handler = new Handler();

    enabledToggleButton =
      (ToggleButton) findViewById(R.id.enabledToggleButton);
    reminderToggleButton =
      (ToggleButton)findViewById(R.id.reminderToggleButton);
    receivingDataImageView =
      (ImageView)findViewById(R.id.receiving_data_iv);
    archiveButton = (Button) findViewById(R.id.archiveButton);
    truncateDataButton = (Button) findViewById(R.id.truncateDataButton);

    enabledToggleButton.setEnabled(true);
    enabledToggleButton.setChecked(spHandler.isSensorOn());
    archiveButton.setEnabled(!spHandler.isSensorOn());
    truncateDataButton.setEnabled(!spHandler.isSensorOn());

    // Bind SCDCManager service if sensor is off
    if (!spHandler.isSensorOn()) {
      bindService(new Intent(LaunchActivity.this, SCDCManager.class),
              scdcManagerConn, BIND_AUTO_CREATE);
    } else { // Bind SCDCService service if sensor is on
      bindService(new Intent(LaunchActivity.this, SCDCService.class),
              scdcServiceConn, BIND_AUTO_CREATE);  // BIND_IMPORTANT?
    }

    // This checkbox enables or disables the pipeline
    enabledToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          Intent intent = new Intent(LaunchActivity.this, SCDCService.class);

          // Increment sensorId by 1
          spHandler.setSensorId(spHandler.getSensorId() + 1);
          Toast.makeText(LaunchActivity.this,
                  SCDCKeys.SharedPrefs.KEY_SENSOR_ID + ": " + spHandler.getSensorId(),
                  Toast.LENGTH_SHORT).show();

          // Start/Bind SCDCService and unbind SCDCManager instead
          startService(intent);
          bindService(intent, scdcServiceConn, BIND_AUTO_CREATE); // BIND_IMPORTANT?
          unbindService(scdcManagerConn);

        } else {
          mAdapter.notifyDataSetChanged();
          spHandler.setReminderRunning(isChecked);

          // Unbind/Stop SCDCService and bind SCDCManager instead
          unbindService(scdcServiceConn);
          stopService(new Intent(LaunchActivity.this, SCDCService.class));
          bindService(new Intent(LaunchActivity.this, SCDCManager.class),
                      scdcManagerConn, BIND_AUTO_CREATE);
        }

        spHandler.setSensorOn(isChecked);
        archiveButton.setEnabled(!isChecked);
        truncateDataButton.setEnabled(!isChecked);
      }
    });


    // This checkbox runs or stops the reminder alarm
    reminderToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView,
                                   boolean isChecked) {
        spHandler.setReminderRunning(isChecked);
        if (isChecked) {
          // Start service to check for alarms
          WakefulIntentService.acquireStaticLock(LaunchActivity.this);
          startService(new Intent(LaunchActivity.this,
                  AlarmButlerService.class));
        } else {
          for (LabelEntry labelEntry : labelEntries) {
            LabelAlarm alarm = new LabelAlarm();
            alarm.cancelAlarm(LaunchActivity.this, labelEntry.getId());
            alarm.cancelNotification(LaunchActivity.this,
                    labelEntry.getId());
          }
          LabelAlarm alarm = new LabelAlarm();
          int generalAlarmId = Integer.parseInt(SCDCKeys.AlarmKeys
                  .DEFAULT_GENERAL_ALARM_ID);
          alarm.cancelAlarm(LaunchActivity.this, generalAlarmId);
          alarm.cancelNotification(LaunchActivity.this, generalAlarmId);
          stopService(new Intent(LaunchActivity.this,
                  AlarmButlerService.class));
        }
      }
    });

    // Always enable reminderToggleButton
    // either enabledToggleButton is checked or not
    reminderToggleButton.setEnabled(true);
    if (reminderToggleButton.isEnabled()) {
      reminderToggleButton.setChecked(spHandler.isReminderRunning());
    }


    // Runs an archive if pipeline is enabled
    archiveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (isNetworkConnected()) {
          if (pipeline.getDatabaseHelper() != null) {
            v.setEnabled(false);

            try {
              // Asynchronously synchronize preferences with server
              if (spHandler.setPrefsToServer()) {
                SQLiteDatabase db = pipeline.getWritableDb();
                Log.d(LogKeys.DEBUG, "LaunchActivity/ db.getPath()=" + db.getPath());
                File dbFile = new File(db.getPath());

                // Asynchronously archive and upload dbFile
                archiveAndUploadDatabase(dbFile);
                dropAndCreateTable(db, true);

                // Wait 5 seconds for archive to finish, then refresh the UI
                // (Note: this is kind of a hack since archiving is seamless
                //         and there are no messages when it occurs)
                handler.postDelayed(new Runnable() {
                                  @Override
                                  public void run() {
                    // pipeline.onRun(BasicPipeline.ACTION_ARCHIVE, null);
                    // pipeline.onRun(BasicPipeline.ACTION_UPLOAD, null);
                    updateLaunchActivityUi();
                    if (!enabledToggleButton.isEnabled()) {
                      v.setEnabled(true);
                    }
            }
          }, 5000L);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        } else {
          Toast.makeText(getBaseContext(),
                getString(R.string.check_internet_connection_message),
                Toast.LENGTH_LONG).show();

        }
      }
    });

    // Truncate the data
    truncateDataButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (pipeline.getDatabaseHelper() != null) {
          SQLiteDatabase db = pipeline.getWritableDb();
          dropAndCreateTable(db, true);
        }
      }
    });


    IntentFilter filter = new IntentFilter();
    filter.addAction(AlertKeys.ACTION_ALERT);
    alertReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(LogKeys.DEBUG, TAG+".onCreate.alertReceiver/ Received broadcast");

        AlertDialog.Builder alert = new AlertDialog.Builder(LaunchActivity.this);
        int code = intent.getIntExtra(AlertKeys.EXTRA_ALERT_ERROR_CODE, -1);
        String message = intent.getStringExtra(AlertKeys.EXTRA_ALERT_ERROR_MESSAGE);
        mAlertDialog = alert.setTitle("Error")
                .setMessage(message + " (alert code:" + code + ")")
                .setPositiveButton("OK", null)
                .show();
      }
    };
    this.registerReceiver(alertReceiver, filter);
  }

  @Override
  public void onResume() {
    super.onResume();

    if (pipeline != null) {
      updateLaunchActivityUi();
    }

    // Dynamically refresh the ListView items
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        mAdapter.notifyDataSetChanged();
        updateLaunchActivityUi();   // FIXME
        handler.postDelayed(this, 1000L);
      }
    }, 1000L);

    // stopService(new Intent(this, AlarmButlerService.class));
  }

  @Override
  public void onPause() {
    super.onPause();

    // Save running status of reminder
    spHandler.setReminderRunning(reminderToggleButton.isChecked());
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Unbind SCDCManager service if sensor is off
    if (!spHandler.isSensorOn()) {
      unbindService(scdcManagerConn);
    } else { // Unbind SCDCService service if sensor is on
      unbindService(scdcServiceConn);
    }
    this.unregisterReceiver(alertReceiver);
  }

  /**
   * @author Kilho Kim
   * Truncate table of the database of the pipeline.
   */
  private void dropAndCreateTable(final SQLiteDatabase db,
                                  final boolean showProgress) {
    new AsyncTask<SQLiteDatabase, Void, Boolean>() {

      private ProgressDialog progressDialog;
      private SCDCDatabaseHelper databaseHelper;

      @Override
      protected void onPreExecute() {
        if (showProgress) {
          progressDialog = new ProgressDialog(LaunchActivity.this);
          progressDialog.setMessage(getString(R.string.truncate_message));
          progressDialog.setCancelable(false);
          progressDialog.show();
        }

        databaseHelper = (SCDCDatabaseHelper) pipeline.getDatabaseHelper();
      }

      @Override
      protected Boolean doInBackground(SQLiteDatabase... dbs) {
        return databaseHelper.dropAndCreateDataTable(dbs[0]);
      }

      @Override
      protected void onPostExecute(Boolean isSuccess) {
        if (showProgress) {
          progressDialog.dismiss();
        }
        dataCountView.setText("Data size: 0.0 MB");
        updateLaunchActivityUi();
        Toast.makeText(getBaseContext(), getString(R.string.truncate_complete_message),
                Toast.LENGTH_LONG).show();
      }
    }.execute(db);
  }

  public SCDCManager getActivitySCDCManager() {
    return scdcManager;
  }

  private void archiveAndUploadDatabase(final File dbFile) {
    new AsyncTask<File, Void, Boolean>() {

      private ProgressDialog progressDialog;
      private FileArchive archive;
      private MultipartEntityArchive upload;
      private SCDCUploadService uploader;

      @Override
      protected void onPreExecute() {
        progressDialog = new ProgressDialog(LaunchActivity.this);
        progressDialog.setMessage(getString(R.string.archive_message));
        progressDialog.setCancelable(false);
        progressDialog.show();

        archive = new ZipArchive(scdcManager, Config.PIPELINE_NAME);
        if (DEBUGGING) {
          upload = new MultipartEntityArchive(scdcManager,
                  Config.DEFAULT_UPLOAD_URL_DEBUG, LaunchActivity.this);
        } else {
          upload = new MultipartEntityArchive(scdcManager,
                  Config.DEFAULT_UPLOAD_URL, LaunchActivity.this);
        }
        uploader = new SCDCUploadService(scdcManager);
        uploader.setContext(LaunchActivity.this);
        uploader.start();
      }

      @Override
      protected Boolean doInBackground(File... files) {
        return archive.add(files[0]);
      }

      @Override
      protected void onPostExecute(Boolean isSuccess) {
        progressDialog.dismiss();
        uploader.run(archive, upload);

        // IMPORTANT: Update config at this time once more
        updateConfig();
        // uploader.stop();
      }
    }.execute(dbFile);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
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
      startActivity(new Intent(this, SettingsActivity.class));
    }

    return super.onOptionsItemSelected(item);
  }

  private boolean isNetworkConnected() {
    ConnectivityManager cm =
      (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo() != null;
  }

  public boolean changeConfig(boolean isActiveLabelOn) {
    if (pipeline != null) {
      JsonObject oldConfig = scdcManager.getPipelineConfig(pipeline.getName());
      String newConfigString;
      if (pipeline != null) {
        if (isActiveLabelOn) newConfigString = spHandler.getActiveConfig();
        else                 newConfigString = spHandler.getIdleConfig();

        Log.d(LogKeys.DEBUG,
                TAG+".changeConfig/ newConfig=" + newConfigString);
        JsonObject newConfig = new JsonParser().parse(newConfigString).getAsJsonObject();
        if (!EqualsUtil.areEqual(oldConfig, newConfig)) {
          scdcManager.saveAndReload(pipeline.getName(), newConfig);
        }
        Toast.makeText(getBaseContext(),
                getString(R.string.change_config_complete_message),
                Toast.LENGTH_SHORT).show();
        return true;
      } else {
        Log.d(LogKeys.DEBUG, TAG + ".changeConfig/ failed to change config");
        Toast.makeText(getBaseContext(),
                getString(R.string.change_config_failed_message),
                Toast.LENGTH_SHORT).show();
        return false;
      }
    } else {
      Log.d(LogKeys.DEBUG, TAG + ".changeConfig/ failed to change config");
      return false;
    }
  }

  // Update config for both active and idle state
  private void updateConfig() {
    new AsyncTask<Void, Void, Boolean>() {
      private HttpConfigUpdater hcu;
      private String updateActiveUrl;
      private String updateIdleUrl;
      private JsonObject oldConfig;

      @Override
      protected void onPreExecute() {
        hcu = new HttpConfigUpdater();
        if (DEBUGGING) {
          updateActiveUrl = Config.DEFAULT_UPDATE_URL_DEBUG;
          updateIdleUrl = Config.DEFAULT_UPDATE_URL_DEBUG;
        } else {
          updateActiveUrl = Config.DEFAULT_UPDATE_URL_ACTIVE;
          updateIdleUrl = Config.DEFAULT_UPDATE_URL_IDLE;
        }
        oldConfig = scdcManager.getPipelineConfig(pipeline.getName());
      }

      @Override
      protected Boolean doInBackground(Void... voids) {
        String newConfig;
        if (pipeline != null) {
          try {
            hcu.setUrl(updateActiveUrl);
            Log.d(LogKeys.DEBUG, TAG + ".updateConfig()/ url=" + updateActiveUrl);
            newConfig = hcu.getConfig().toString();
            spHandler.setActiveConfig(newConfig);
            hcu.setUrl(updateIdleUrl);
            Log.d(LogKeys.DEBUG, TAG + ".updateConfig()/ url=" + updateIdleUrl);
            newConfig = hcu.getConfig().toString();
            spHandler.setIdleConfig(newConfig);

            return true;
          } catch (ConfigUpdateException e) {
            Log.w(LogKeys.DEBUG, TAG+".updateConfig()/ Unable to get config", e);
            return false;
          }
        } else {
          Log.d(LogKeys.DEBUG, TAG+".updateConfig/ failed to update config");
          return false;
        }
      }

      @Override
      protected void onPostExecute(Boolean isSuccess) {
        if (isSuccess) {
          Toast.makeText(getBaseContext(),
                  getString(R.string.update_config_complete_message),
                  Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(getBaseContext(),
                  getString(R.string.update_config_failed_message),
                  Toast.LENGTH_LONG).show();
        }
      }
    }.execute();
  }

  public void updateLaunchActivityUi() {
    new AsyncTask<Void, Void, Boolean>() {

      @Override
      protected Boolean doInBackground(Void... voids) {
        publishProgress(voids);

        return true;
      }

      @Override
      protected void onProgressUpdate(Void... voids) {
        /**
         * Queries the database of the pipeline to determine
         * how many rows of data we have recorded so far.
         */
        if (pipeline != null) {
          if (pipeline.getDatabaseHelper() == null) {
            pipeline.reloadDbHelper(scdcManager);
          }

          if (pipeline.getDatabaseHelper() != null) {
            // Query the pipeline db for the count of rows in the data table
            SQLiteDatabase db = pipeline.getDb();
            final long dbSize = new File(db.getPath()).length();  // in bytes
            dataCountView.setText("Data size: " +
                    Math.round((dbSize / (1048576.0)) * 10.0) / 10.0 + " MB");
          }
        } else if (scdcService != null) {
          long dbSize = scdcService.getDBSize();
          dataCountView.setText("Data size: " +
                  Math.round((dbSize / (1048576.0)) * 10.0) / 10.0 + " MB");
        }

        /**
         * Temporarily turns on the receiving_data_iv for 3 seconds.
         */
        receivingDataImageView.setVisibility(View.VISIBLE);

        // Turn off iv after 3 seconds
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            receivingDataImageView.setVisibility(View.INVISIBLE);
          }
        }, 3000);
      }
    }.execute();
  }

}
