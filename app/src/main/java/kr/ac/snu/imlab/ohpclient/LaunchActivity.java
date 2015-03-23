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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import edu.mit.media.funf.FunfManager;
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;


public class LaunchActivity extends ActionBarActivity implements DataListener {
    public static final String PIPELINE_NAME = "default";

    private Handler handler;
    private FunfManager funfManager = null;
    private BasicPipeline pipeline = null;

    // Probes
    private AccelerometerFeaturesProbe accelerometerFeaturesProbe;
    private AccelerometerSensorProbe accelerometerSensorProbe;
    private AccountsProbe accountsProbe;
    private ActivityProbe activityProbe;
    // private AlarmProbe alarmProbe;
    private AndroidInfoProbe androidInfoProbe;
    private ApplicationsProbe applicationsProbe;
    // private AudioCaptureProbe audioCaptureProbe;
    // private AudioFeaturesProbe audioFeaturesProbe;
    private AudioMediaProbe audioMediaProbe;
    private BatteryProbe batteryProbe;
    private BluetoothProbe bluetoothProbe;
    private BrowserBookmarksProbe browserBookmarksProbe;
    private BrowserSearchesProbe browserSearchesProbe;
    private CallLogProbe callLogProbe;
    private CellTowerProbe cellTowerProbe;
    private ContactProbe contactProbe;
    // private ContentProviderProbe contentProviderProbe;
    // private DatedContentProviderProbe datedContentProviderProbe;
    private GravitySensorProbe gravitySensorProbe;
    private GyroscopeSensorProbe gyroscopeSensorProbe;
    private HardwareInfoProbe hardwareInfoProbe;
    // private ImageCaptureProbe imageCaptureProbe;
    private ImageMediaProbe imageMediaProbe;
    // private ImpulseProbe impulseProbe;
    private LightSensorProbe lightSensorProbe;
    private LinearAccelerationSensorProbe linearAccelerationSensorProbe;
    private LocationProbe locationProbe;
    private MagneticFieldSensorProbe magneticFieldSensorProbe;
    private OrientationSensorProbe orientationSensorProbe;
    private PressureSensorProbe pressureSensorProbe;
    private ProcessStatisticsProbe processStatisticsProbe;
    private ProximitySensorProbe proximitySensorProbe;
    private RotationVectorSensorProbe rotationVectorSensorProbe;
    private RunningApplicationsProbe runningApplicationsProbe;
    private ScreenProbe screenProbe;
    private SensorProbe sensorProbe;
    private ServicesProbe servicesProbe;
    private SimpleLocationProbe simpleLocationProbe;
    // private SimpleProbe simpleProbe;
    private SmsProbe smsProbe;
    private TelephonyProbe telephonyProbe;
    private TemperatureSensorProbe temperatureSensorProbe;
    private TimeOffsetProbe timeOffsetProbe;
    // private VideoCaptureProbe videoCaptureProbe;
    private VideoMediaProbe videoMediaProbe;
    private WifiProbe wifiProbe;

    // Run Data Collection button
    private ToggleButton enabledToggleButton;

    // Probe checkboxes
    // Device
    private CheckBox enabledServices;
    private CheckBox enabledHardwareInfo;
    private CheckBox enabledMobileNetworkInfo;
    private CheckBox enabledProcessStatistics;
    private CheckBox enabledBattery;
    private CheckBox enabledTimeOffset;
    private CheckBox enabledAndroidInfo;
    // Device Interaction
    private CheckBox enabledRunningApplications;
    private CheckBox enabledVideoFileStats;
    private CheckBox enabledImageFileStats;
    private CheckBox enabledBrowserBookmarks;
    private CheckBox enabledScreenOnOff;
    private CheckBox enabledBrowserSearches;
    private CheckBox enabledApplications;
    private CheckBox enabledAudioMediaFileStats;
    private CheckBox enabledAccounts;
    // Environment
    private CheckBox enabledProximitySensor;
    private CheckBox enabledLightSensor;
    private CheckBox enabledMagneticFieldSensor;
    private CheckBox enabledTemperatureSensor;
    private CheckBox enabledPressureSensor;
    private CheckBox enabledAudioFeatures;
    // Motion
    private CheckBox enabledActivity;
    private CheckBox enabledGyroscopeSensor;
    private CheckBox enabledAccelerometerFeatures;
    private CheckBox enabledAccelerometerSensor;
    private CheckBox enabledLinearAccelerationSensor;
    private CheckBox enabledOrientationSensor;
    private CheckBox enabledGravitySensor;
    private CheckBox enabledRotationVectorSensor;
    // Positioning
    private CheckBox enabledSimpleLocation;
    private CheckBox enabledNearbyWifiDevices;
    private CheckBox enabledNearbyCellularTowers;
    private CheckBox enabledNearbyBluetoothDevices;
    private CheckBox enabledContinuousLocation;
    // Social
    private CheckBox enabledCallLog;
    private CheckBox enabledContacts;
    private CheckBox enabledSmsLog;

    private Button archiveButton, scanNowButton, updateDataCountButton;
    private TextView dataCountView;
    private ServiceConnection funfManagerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            funfManager = ((FunfManager.LocalBinder)service).getManager();
            Gson gson = funfManager.getGson();
            pipeline = (BasicPipeline)funfManager.getRegisteredPipeline(PIPELINE_NAME);

            accelerometerFeaturesProbe = gson.fromJson(new JsonObject(), AccelerometerFeaturesProbe.class);
            Log.w(LogUtil.TAG, "accelerometerFeatuesProbe: " + accelerometerFeaturesProbe.getConfig() + ", " + accelerometerFeaturesProbe.getState());
            accelerometerSensorProbe = gson.fromJson(new JsonObject(), AccelerometerSensorProbe.class);
            accountsProbe = gson.fromJson(new JsonObject(), AccountsProbe.class);
            activityProbe = gson.fromJson(new JsonObject(), ActivityProbe.class);
            // alarmProbe = gson.fromJson(new JsonObject(), AlarmProbe.class);
            androidInfoProbe = gson.fromJson(new JsonObject(), AndroidInfoProbe.class);
            applicationsProbe = gson.fromJson(new JsonObject(), ApplicationsProbe.class);
            // audioCaptureProbe = gson.fromJson(new JsonObject(), AudioCaptureProbe.class);
            // audioFeaturesProbe = gson.fromJson(new JsonObject(), AudioFeaturesProbe.class);
            audioMediaProbe = gson.fromJson(new JsonObject(), AudioMediaProbe.class);
            batteryProbe = gson.fromJson(new JsonObject(), BatteryProbe.class);
            bluetoothProbe = gson.fromJson(new JsonObject(), BluetoothProbe.class);
            browserBookmarksProbe = gson.fromJson(new JsonObject(), BrowserBookmarksProbe.class);
            browserSearchesProbe = gson.fromJson(new JsonObject(), BrowserSearchesProbe.class);
            callLogProbe = gson.fromJson(new JsonObject(), CallLogProbe.class);
            cellTowerProbe = gson.fromJson(new JsonObject(), CellTowerProbe.class);
            contactProbe = gson.fromJson(new JsonObject(), ContactProbe.class);
            // contentProviderProbe = gson.fromJson(new JsonObject(), ContentProviderProbe.class);
            // datedContentProviderProbe = gson.fromJson(new JsonObject(), DatedContentProviderProbe.class);
            gravitySensorProbe = gson.fromJson(new JsonObject(), GravitySensorProbe.class);
            gyroscopeSensorProbe = gson.fromJson(new JsonObject(), GyroscopeSensorProbe.class);
            hardwareInfoProbe = gson.fromJson(new JsonObject(), HardwareInfoProbe.class);
            // imageCaptureProbe = gson.fromJson(new JsonObject(), ImageCaptureProbe.class);
            imageMediaProbe = gson.fromJson(new JsonObject(), ImageMediaProbe.class);
            // impulseProbe = gson.fromJson(new JsonObject(),
            // ImpulseProbe.class);
            lightSensorProbe = gson.fromJson(new JsonObject(), LightSensorProbe.class);
            linearAccelerationSensorProbe = gson.fromJson(new JsonObject(), LinearAccelerationSensorProbe.class);
            magneticFieldSensorProbe = gson.fromJson(new JsonObject(), MagneticFieldSensorProbe.class);
            orientationSensorProbe = gson.fromJson(new JsonObject(), OrientationSensorProbe.class);
            pressureSensorProbe = gson.fromJson(new JsonObject(), PressureSensorProbe.class);
            processStatisticsProbe = gson.fromJson(new JsonObject(), ProcessStatisticsProbe.class);
            proximitySensorProbe = gson.fromJson(new JsonObject(), ProximitySensorProbe.class);
            rotationVectorSensorProbe = gson.fromJson(new JsonObject(), RotationVectorSensorProbe.class);
            runningApplicationsProbe = gson.fromJson(new JsonObject(), RunningApplicationsProbe.class);
            screenProbe = gson.fromJson(new JsonObject(), ScreenProbe.class);
            // sensorProbe = gson.fromJson(new JsonObject(), SensorProbe.class);
            servicesProbe = gson.fromJson(new JsonObject(), ServicesProbe.class);
            simpleLocationProbe = gson.fromJson(new JsonObject(), SimpleLocationProbe.class);
            // simpleProbe = gson.fromJson(new JsonObject(), SimpleProbe.class);
            smsProbe = gson.fromJson(new JsonObject(), SmsProbe.class);
            telephonyProbe = gson.fromJson(new JsonObject(), TelephonyProbe.class);
            temperatureSensorProbe = gson.fromJson(new JsonObject(), TemperatureSensorProbe.class);
            timeOffsetProbe = gson.fromJson(new JsonObject(), TimeOffsetProbe.class);
            // videoCaptureProbe = gson.fromJson(new JsonObject(), VideoCaptureProbe.class);
            videoMediaProbe = gson.fromJson(new JsonObject(), VideoMediaProbe.class);
            locationProbe = gson.fromJson(new JsonObject(), LocationProbe.class);
            wifiProbe = gson.fromJson(new JsonObject(), WifiProbe.class);
            Log.w(LogUtil.TAG, "wifiProbe: " + wifiProbe.getConfig() + ", " + wifiProbe.getState());


//            accelerometerFeaturesProbe.registerPassiveListener(LaunchActivity.this);
//            Log.w(LogUtil.TAG, "accelerometerFeatuesProbe: " + accelerometerFeaturesProbe.getConfig() + ", " + accelerometerFeaturesProbe.getState());
//            accelerometerSensorProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledAccounts.isChecked())
                accountsProbe.registerPassiveListener(LaunchActivity.this);
//            activityProbe.registerPassiveListener(LaunchActivity.this);
            //// alarmProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledAndroidInfo.isChecked())
                androidInfoProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledApplications.isChecked())
                applicationsProbe.registerPassiveListener(LaunchActivity.this);
            //// audioCaptureProbe.registerPassiveListener(LaunchActivity.this);
            //// audioFeaturesProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledAudioMediaFileStats.isChecked())
                audioMediaProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledBattery.isChecked())
                batteryProbe.registerPassiveListener(LaunchActivity.this);
//            bluetoothProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledBrowserBookmarks.isChecked())
                browserBookmarksProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledBrowserSearches.isChecked())
                browserSearchesProbe.registerPassiveListener(LaunchActivity.this);
//            callLogProbe.registerPassiveListener(LaunchActivity.this);
//            cellTowerProbe.registerPassiveListener(LaunchActivity.this);
//            contactProbe.registerPassiveListener(LaunchActivity.this);
            //// contentProviderProbe.registerPassiveListener(LaunchActivity.this);
            //// datedContentProviderProbe.registerPassiveListener(LaunchActivity.this);
//            gravitySensorProbe.registerPassiveListener(LaunchActivity.this);
//            gyroscopeSensorProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledHardwareInfo.isChecked())
                hardwareInfoProbe.registerPassiveListener(LaunchActivity.this);
            //// imageCaptureProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledImageFileStats.isChecked())
                imageMediaProbe.registerPassiveListener(LaunchActivity.this);
            //// impulseProbe.registerPassiveListener(LaunchActivity.this);
//            lightSensorProbe.registerPassiveListener(LaunchActivity.this);
//            linearAccelerationSensorProbe.registerPassiveListener(LaunchActivity.this);
//            magneticFieldSensorProbe.registerPassiveListener(LaunchActivity.this);
//            orientationSensorProbe.registerPassiveListener(LaunchActivity.this);
//            pressureSensorProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledProcessStatistics.isChecked())
                processStatisticsProbe.registerPassiveListener(LaunchActivity.this);
//            proximitySensorProbe.registerPassiveListener(LaunchActivity.this);
//            rotationVectorSensorProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledRunningApplications.isChecked())
                runningApplicationsProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledScreenOnOff.isChecked())
                screenProbe.registerPassiveListener(LaunchActivity.this);
            //// sensorProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledServices.isChecked())
                servicesProbe.registerPassiveListener(LaunchActivity.this);
//            simpleLocationProbe.registerPassiveListener(LaunchActivity.this);
            //// simpleProbe.registerPassiveListener(LaunchActivity.this);
//            smsProbe.registerPassiveListener(LaunchActivity.this);
//            telephonyProbe.registerPassiveListener(LaunchActivity.this);
//            temperatureSensorProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledTimeOffset.isChecked())
                timeOffsetProbe.registerPassiveListener(LaunchActivity.this);
            //// videoCaptureProbe.registerPassiveListener(LaunchActivity.this);
            if (enabledVideoFileStats.isChecked())
                videoMediaProbe.registerPassiveListener(LaunchActivity.this);
//            locationProbe.registerPassiveListener(LaunchActivity.this);
//            wifiProbe.registerPassiveListener(LaunchActivity.this);
//            Log.w(LogUtil.TAG, "wifiProbe: " + wifiProbe.getConfig() + ", " + wifiProbe.getState());

            // This checkbox enables or disables the pipeline
            enabledToggleButton.setChecked(pipeline.isEnabled());
            enabledToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (funfManager != null) {
                    if (isChecked) {
                        funfManager.enablePipeline(PIPELINE_NAME);
                        pipeline = (BasicPipeline)funfManager.getRegisteredPipeline(PIPELINE_NAME);
                    } else {
                        funfManager.disablePipeline(PIPELINE_NAME);
                    }
                }
                }
            });

            // Set UI ready to use, by enabling buttons
            enabledToggleButton.setEnabled(true);
            archiveButton.setEnabled(true);
            updateDataCountButton.setEnabled(true);
            scanNowButton.setEnabled(true);
            reloadProbeList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            funfManager = null;
        }
    };

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // Displays the count of rows in the data
        dataCountView = (TextView)findViewById(R.id.dataCountText);

        // Used to make interface changes on main thread
        handler = new Handler();

        enabledToggleButton = (ToggleButton)findViewById(R.id.enabledToggleButton);
        enabledToggleButton.setEnabled(false);
        // Device
        enabledServices = (CheckBox)findViewById(R.id.enabledServices);
        enabledHardwareInfo = (CheckBox)findViewById(R.id.enabledHardwareInfo);
        enabledMobileNetworkInfo = (CheckBox)findViewById(R.id.enabledMobileNetworkInfo);
        enabledProcessStatistics = (CheckBox)findViewById(R.id.enabledProcessStatistics);
        enabledBattery = (CheckBox)findViewById(R.id.enabledBattery);
        enabledTimeOffset = (CheckBox)findViewById(R.id.enabledTimeOffset);
        enabledAndroidInfo = (CheckBox)findViewById(R.id.enabledAndroidInfo);
        // Device Interaction
        enabledRunningApplications = (CheckBox)findViewById(R.id.enabledRunningApplications);
        enabledVideoFileStats = (CheckBox)findViewById(R.id.enabledVideoFileStats);
        enabledImageFileStats = (CheckBox)findViewById(R.id.enabledImageFileStats);
        enabledBrowserBookmarks = (CheckBox)findViewById(R.id.enabledBrowserBookmarks);
        enabledScreenOnOff = (CheckBox)findViewById(R.id.enabledScreenOnOff);
        enabledBrowserSearches = (CheckBox)findViewById(R.id.enabledBrowserSearches);
        enabledApplications = (CheckBox)findViewById(R.id.enabledApplications);
        enabledAudioMediaFileStats = (CheckBox)findViewById(R.id.enabledAudioMediaFileStats);
        enabledAccounts = (CheckBox)findViewById(R.id.enabledAccounts);

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
                    }, 10L * 1000L);
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


        // Forces the pipeline to scan now
        scanNowButton = (Button)findViewById(R.id.scanNowButton);
        scanNowButton.setEnabled(false);
        scanNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pipeline.isEnabled()) {
                    // Manually register the pipeline
                    /*
                    accelerometerFeaturesProbe.registerListener(pipeline);
                    accelerometerSensorProbe.registerListener(pipeline);
                    accountsProbe.registerListener(pipeline);
                    activityProbe.registerListener(pipeline);
                    // alarmProbe.registerListener(pipeline);
                    androidInfoProbe.registerListener(pipeline);
                    applicationsProbe.registerListener(pipeline);
                    // audioCaptureProbe.registerListener(pipeline);
                    // audioFeaturesProbe.registerListener(pipeline);
                    audioMediaProbe.registerListener(pipeline);
                    batteryProbe.registerListener(pipeline);
                    bluetoothProbe.registerListener(pipeline);
                    browserBookmarksProbe.registerListener(pipeline);
                    browserSearchesProbe.registerListener(pipeline);
                    callLogProbe.registerListener(pipeline);
                    cellTowerProbe.registerListener(pipeline);
                    contactProbe.registerListener(pipeline);
                    // contentProviderProbe.registerListener(pipeline);
                    // datedContentProviderProbe.registerListener(pipeline);
                    gravitySensorProbe.registerListener(pipeline);
                    gyroscopeSensorProbe.registerListener(pipeline);
                    hardwareInfoProbe.registerListener(pipeline);
                    // imageCaptureProbe.registerListener(pipeline);
                    imageMediaProbe.registerListener(pipeline);
                    // impulseProbe.registerListener(pipeline);
                    lightSensorProbe.registerListener(pipeline);
                    linearAccelerationSensorProbe.registerListener(pipeline);
                    magneticFieldSensorProbe.registerListener(pipeline);
                    orientationSensorProbe.registerListener(pipeline);
                    pressureSensorProbe.registerListener(pipeline);
                    processStatisticsProbe.registerListener(pipeline);
                    proximitySensorProbe.registerListener(pipeline);
                    rotationVectorSensorProbe.registerListener(pipeline);
                    runningApplicationsProbe.registerListener(pipeline);
                    screenProbe.registerListener(pipeline);
                    // sensorProbe.registerListener(pipeline);
                    servicesProbe.registerListener(pipeline);
                    simpleLocationProbe.registerListener(pipeline);
                    // simpleProbe.registerListener(pipeline);
                    smsProbe.registerListener(pipeline);
                    telephonyProbe.registerListener(pipeline);
                    temperatureSensorProbe.registerListener(pipeline);
                    timeOffsetProbe.registerListener(pipeline);
                    // videoCaptureProbe.registerListener(pipeline);
                    videoMediaProbe.registerListener(pipeline);
                    locationProbe.registerListener(pipeline);
                    */
                    wifiProbe.registerListener(pipeline);
                } else {
                    Toast.makeText(getBaseContext(), "Pipeline is not enabled.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bind to the service, to create the connection with FunfManager
        bindService(new Intent(this, FunfManager.class), funfManagerConn,
                BIND_AUTO_CREATE);
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
        Log.w(LogUtil.TAG, "probeConfig: " + probeConfig + ", data: " + data);
    }

    @Override
    public void onDataCompleted(IJsonObject probeConfig, JsonElement checkpoint) {
        updateScanCount();
        // Re-register to keep listening after probe completes.
//            accelerometerFeaturesProbe.registerPassiveListener(LaunchActivity.this);
//            Log.w(LogUtil.TAG, "accelerometerFeatuesProbe: " + accelerometerFeaturesProbe.getConfig() + ", " + accelerometerFeaturesProbe.getState());
//            accelerometerSensorProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledAccounts.isChecked())
            accountsProbe.registerPassiveListener(LaunchActivity.this);
//            activityProbe.registerPassiveListener(LaunchActivity.this);
        //// alarmProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledAndroidInfo.isChecked())
            androidInfoProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledApplications.isChecked())
            applicationsProbe.registerPassiveListener(LaunchActivity.this);
        //// audioCaptureProbe.registerPassiveListener(LaunchActivity.this);
        //// audioFeaturesProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledAudioMediaFileStats.isChecked())
            audioMediaProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledBattery.isChecked())
            batteryProbe.registerPassiveListener(LaunchActivity.this);
//            bluetoothProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledBrowserBookmarks.isChecked())
            browserBookmarksProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledBrowserSearches.isChecked())
            browserSearchesProbe.registerPassiveListener(LaunchActivity.this);
//            callLogProbe.registerPassiveListener(LaunchActivity.this);
//            cellTowerProbe.registerPassiveListener(LaunchActivity.this);
//            contactProbe.registerPassiveListener(LaunchActivity.this);
        //// contentProviderProbe.registerPassiveListener(LaunchActivity.this);
        //// datedContentProviderProbe.registerPassiveListener(LaunchActivity.this);
//            gravitySensorProbe.registerPassiveListener(LaunchActivity.this);
//            gyroscopeSensorProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledHardwareInfo.isChecked())
            hardwareInfoProbe.registerPassiveListener(LaunchActivity.this);
        //// imageCaptureProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledImageFileStats.isChecked())
            imageMediaProbe.registerPassiveListener(LaunchActivity.this);
        //// impulseProbe.registerPassiveListener(LaunchActivity.this);
//            lightSensorProbe.registerPassiveListener(LaunchActivity.this);
//            linearAccelerationSensorProbe.registerPassiveListener(LaunchActivity.this);
//            magneticFieldSensorProbe.registerPassiveListener(LaunchActivity.this);
//            orientationSensorProbe.registerPassiveListener(LaunchActivity.this);
//            pressureSensorProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledProcessStatistics.isChecked())
            processStatisticsProbe.registerPassiveListener(LaunchActivity.this);
//            proximitySensorProbe.registerPassiveListener(LaunchActivity.this);
//            rotationVectorSensorProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledRunningApplications.isChecked())
            runningApplicationsProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledScreenOnOff.isChecked())
            screenProbe.registerPassiveListener(LaunchActivity.this);
        //// sensorProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledServices.isChecked())
            servicesProbe.registerPassiveListener(LaunchActivity.this);
//            simpleLocationProbe.registerPassiveListener(LaunchActivity.this);
        //// simpleProbe.registerPassiveListener(LaunchActivity.this);
//            smsProbe.registerPassiveListener(LaunchActivity.this);
//            telephonyProbe.registerPassiveListener(LaunchActivity.this);
//            temperatureSensorProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledTimeOffset.isChecked())
            timeOffsetProbe.registerPassiveListener(LaunchActivity.this);
        //// videoCaptureProbe.registerPassiveListener(LaunchActivity.this);
        if (enabledVideoFileStats.isChecked())
            videoMediaProbe.registerPassiveListener(LaunchActivity.this);
//            locationProbe.registerPassiveListener(LaunchActivity.this);
//            wifiProbe.registerPassiveListener(LaunchActivity.this);
//            Log.w(LogUtil.TAG, "wifiProbe: " + wifiProbe.getConfig() + ", " + wifiProbe.getState());
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
