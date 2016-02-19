package kr.ac.snu.imlab.scdc.service.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;

import edu.mit.media.funf.util.EqualsUtil;
import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.LogKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.SCDCServiceKeys;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;
import kr.ac.snu.imlab.scdc.R;

/**
 * Created by kilho on 16. 2. 19.
 */
public class SCDCService extends Service {

  protected static final String TAG = "SCDCService";

  private SCDCManager scdcManager;
  private SCDCPipeline pipeline;
  private SharedPrefsHandler spHandler;

  private ServiceConnection scdcManagerConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      scdcManager = ((SCDCManager.LocalBinder) service).getManager();
      scdcManager.reload();
      pipeline = (SCDCPipeline) scdcManager.getRegisteredPipeline
              (SCDCKeys.Config.PIPELINE_NAME);
      scdcManager.enablePipeline(pipeline.getName());

      Log.d(LogKeys.DEBUG, TAG+".scdcManagerConn" +
              ".onServiceConnected(): pipeline.getName()=" +
              pipeline.getName() + ", pipeline.isEnabled()=" + pipeline.isEnabled() +
              ", pipeline.getDatabaseHelper()=" + pipeline.getDatabaseHelper());
//      pipeline.setDataReceivedListener(LaunchActivity.this);
      // Update probe schedules of pipeline
      Log.d(LogKeys.DEBUG, TAG+".scdcManagerConn.onServiceConnected(): "
              + "spHandler.isActiveLabelOn()=" +
              spHandler.isActiveLabelOn());
      changeConfig(spHandler.isActiveLabelOn());

      spHandler.setSensorId(spHandler.getSensorId() + 1);
      Toast.makeText(getBaseContext(),
              SCDCKeys.SharedPrefs.KEY_SENSOR_ID + ": " + spHandler.getSensorId(),
              Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.d(LogKeys.DEBUG, TAG+".scdcManagerConn.onServiceDisconnected() called");
      scdcManager = null;
      pipeline = null;
    }
  };

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    spHandler = SharedPrefsHandler.getInstance(this,
            Config.SCDC_PREFS, Context.MODE_PRIVATE);

    bindService(new Intent(this, SCDCManager.class),
                scdcManagerConn, BIND_AUTO_CREATE);
    startForeground(SCDCServiceKeys.SCDC_NOTIFICATION_ID, makeNotification());

    return Service.START_STICKY;
  }

  private Notification makeNotification() {
    Intent intent = new Intent(this, LaunchActivity.class);
    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
    return new NotificationCompat.Builder(this)
            .setContentTitle("SCDC Service")
            .setContentText("Running SCDC Service")
            .setSmallIcon(R.drawable.red_icon)
            .setContentIntent(pIntent)
            .build();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(LogKeys.DEBUG, TAG+".onDestroy() called");
    stopForeground(true);
    unbindService(scdcManagerConn);
  }

  public class LocalBinder extends Binder {
    public SCDCService getService() {
      return SCDCService.this;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return new LocalBinder();
  }

  public long getDBSize() {
    // Query the pipeline db for the count of rows in the data table
    SQLiteDatabase db = pipeline.getDb();
    final long dbSize = new File(db.getPath()).length();  // in bytes
    return dbSize;
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
}
