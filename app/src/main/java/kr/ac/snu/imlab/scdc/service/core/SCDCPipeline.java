package kr.ac.snu.imlab.scdc.service.core;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.HandlerThread;
import android.os.Looper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.config.ConfigUpdater;
import edu.mit.media.funf.config.Configurable;
import edu.mit.media.funf.config.RuntimeTypeAdapterFactory;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.Pipeline;
import edu.mit.media.funf.probe.Probe.DataListener;
import edu.mit.media.funf.probe.builtin.ProbeKeys;
import edu.mit.media.funf.storage.DefaultArchive;
import edu.mit.media.funf.storage.FileArchive;
import edu.mit.media.funf.storage.RemoteFileArchive;
import edu.mit.media.funf.storage.UploadService;
import edu.mit.media.funf.util.StringUtil;
import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.service.probe.LabelProbe;
import kr.ac.snu.imlab.scdc.service.storage.SCDCDatabaseHelper;

/**
 * Created by kilho on 15. 7. 28.
 */
public class SCDCPipeline implements Pipeline, DataListener {

  public static final String
    ACTION_ARCHIVE = "archive",
    ACTION_UPLOAD = "upload",
    ACTION_UPDATE = "update";

  protected final int ARCHIVE = 0, UPLOAD = 1, UPDATE = 2, DATA = 3;

  @Configurable
  protected String name = "scdc";

  @Configurable
  protected int version = 1;

  @Configurable
  protected FileArchive archive = null;

  @Configurable
  protected RemoteFileArchive upload = null;

  @Configurable
  protected ConfigUpdater update = null;

  @Configurable
  protected List<JsonElement> data = new ArrayList<JsonElement>();

  @Configurable
  protected Map<String, Schedule> schedules = new HashMap<String, Schedule>();

  private UploadService uploader;
  private Activity activity;

  private boolean enabled;
  private FunfManager manager;
  private SQLiteOpenHelper databaseHelper = null;
  private Looper looper;
  private Handler handler;
  private Handler.Callback callback = new Handler.Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      onBeforeRun(msg.what, (JsonObject)msg.obj);
      switch (msg.what) {
        case ARCHIVE:
          if (archive != null) {
            Log.w("DEBUG", "SCDCPipeline/ running runArchive()");
            runArchive();
          }
          break;
        case UPLOAD:
          if (archive != null && upload != null && uploader != null) {
            Log.w("DEBUG", "SCDCPipeline/ running uploader.run(archive, " +
                    "upload)");
            // uploader.start();
            uploader.run(archive, upload);
          }
          break;
        case UPDATE:
          if (update != null) {
//            Log.w("DEBUG", "SCDCPipeline/ Entered handleMessage: UPDATE");
            update.run(name, manager);
          }
          break;
        case DATA:
          String name = ((JsonObject)msg.obj).get("name").getAsString();
          IJsonObject data = (IJsonObject)((JsonObject)msg.obj).get("value");
          writeData(name, data);
          break;
        default:
          break;
      }
      onAfterRun(msg.what, (JsonObject)msg.obj);
      return false;
    }
  };

  protected void reloadDbHelper(Context ctx) {
    this.databaseHelper =
      new SCDCDatabaseHelper(ctx,
        StringUtil.simpleFilesafe(name), version);
  }

  // Edited by Kilho Kim:
  protected void runArchive() {
    // new BackgroundArchiver().execute();
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    // TODO: add check to make sure this is not empty
    File dbFile = new File(db.getPath());
    db.close();
    archive.add(dbFile);
//    if (archive.add(dbFile)) {
//      dbFile.delete();
//    }
    reloadDbHelper(manager);
    databaseHelper.getWritableDatabase(); // Build new database
  }

  protected void writeData(String name, IJsonObject data) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    final double timestamp = data.get(ProbeKeys.BaseProbeKeys.TIMESTAMP).getAsDouble();
    final String value = data.toString();
    // if (name == null || value == null) {
    /*
    if (timestamp == 0L || name == null || value == null) {
        Log.e(LogUtil.TAG, "Unable to save data.  Not all required values specified. " + timestamp + " " + name + " - " + value);
        throw new SQLException("Not all required fields specified.");
    }
    */
    ContentValues cv = new ContentValues();
    cv.put(SCDCDatabaseHelper.COLUMN_NAME, name);
    cv.put(SCDCDatabaseHelper.COLUMN_VALUE, value);
    cv.put(SCDCDatabaseHelper.COLUMN_TIMESTAMP, timestamp);
    // Added by Kilho Kim: When the data table is suddenly truncated:
    try {
      db.insertOrThrow(SCDCDatabaseHelper.DATA_TABLE.name, "", cv);
    } catch (SQLiteException e) {
      // Do nothing
    }
  }


  @Override
  public void onCreate(FunfManager manager) {
    if (archive == null) {
      archive = new DefaultArchive(manager, name);
    }
    if (uploader == null) {
      uploader = new UploadService(manager);
      uploader.start();
    }
    this.manager = manager;
    reloadDbHelper(manager);
    HandlerThread thread = new HandlerThread(getClass().getName());
    Log.w("DEBUG", "new thread=" + thread.getName());
    thread.start();
    this.looper = thread.getLooper();
    this.handler = new Handler(looper, callback);
    enabled = true;
    for (JsonElement dataRequest : data) {
      manager.requestData(this, dataRequest);
    }
    for (Map.Entry<String, Schedule> schedule : schedules.entrySet()) {
      manager.registerPipelineAction(this, schedule.getKey(), schedule.getValue());
    }
  }

  /**
   * @author Kilho Kim
   * @description Set calling activity for pipeline
   */
  public void setActivity(Activity activity) {
    this.activity = activity;
  }

  @Override
  public void onDestroy() {
    for (JsonElement dataRequest : data) {
      manager.unrequestData(this, dataRequest);
    }
    for (Map.Entry<String, Schedule> schedule : schedules.entrySet()) {
      manager.unregisterPipelineAction(this, schedule.getKey());
    }
    if (uploader != null) {
      uploader.stop();
    }
    looper.quit();
    enabled = false;
  }

  @Override
  public void onRun(String action, JsonElement config) {
    Message message;
    // Run on handler thread
    if (ACTION_ARCHIVE.equals(action)) {
      message = Message.obtain(handler, ARCHIVE, config);
      handler.sendMessage(message);
    } else if (ACTION_UPLOAD.equals(action)) {
      message = Message.obtain(handler, UPLOAD, config);
      handler.sendMessage(message);
    } else if (ACTION_UPDATE.equals(action)) {
      message = Message.obtain(handler, UPDATE, config);
      handler.sendMessage(message);
    }
  }

  /**
   * Used as a hook to customize behavior before an action takes place.
   * @param action the type of action taking place
   * @param config the configuration for the action
   */
  protected void onBeforeRun(int action, JsonElement config) {

  }

  /**
   * Used as a hook to customize behavior after an action takes place.
   * @param action the type of action taking place
   * @param config the configuration for the action
   */
  protected void onAfterRun(int action, JsonElement config) {

  }

  public Handler getHandler() {
    return handler;
  }

  protected FunfManager getFunfManager() {
    return manager;
  }


  public SQLiteDatabase getDb() {
    return databaseHelper.getReadableDatabase();
  }

  public SQLiteDatabase getWritableDb() {
    return databaseHelper.getWritableDatabase();
  }

  public List<JsonElement> getDataRequests() {
    return data == null ? null : Collections.unmodifiableList(data);
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public int getVersion() {
    return version;
  }


  public void setVersion(int version) {
    this.version = version;
  }


  public FileArchive getArchive() {
    return archive;
  }


  public void setArchive(FileArchive archive) {
    this.archive = archive;
  }


  public RemoteFileArchive getUpload() {
    return upload;
  }


  public void setUpload(RemoteFileArchive upload) {
    this.upload = upload;
  }


  public ConfigUpdater getUpdate() {
    return update;
  }


  public void setUpdate(ConfigUpdater update) {
    this.update = update;
  }


  public void setDataRequests(List<JsonElement> data) {
    this.data = new ArrayList<JsonElement>(data); // Defensive copy
  }


  public Map<String, Schedule> getSchedules() {
    return schedules;
  }


  public void setSchedules(Map<String, Schedule> schedules) {
    this.schedules = schedules;
  }


  public UploadService getUploader() {
    return uploader;
  }


  public void setUploader(UploadService uploader) {
    this.uploader = uploader;
  }


  public SQLiteOpenHelper getDatabaseHelper() {
    return databaseHelper;
  }


  public void setDatabaseHelper(SQLiteOpenHelper databaseHelper) {
    this.databaseHelper = databaseHelper;
  }


  @Override
  public void onDataReceived(IJsonObject probeConfig, IJsonObject data) {
    JsonObject record = new JsonObject();
    record.add("name", probeConfig.get(RuntimeTypeAdapterFactory.TYPE));
    record.add("value", data);
//    Log.e("test0805", "data received from "+ probeConfig.get(RuntimeTypeAdapterFactory.TYPE).getAsString());
    Message message = Message.obtain(handler, DATA, record);
    if (probeConfig.get(RuntimeTypeAdapterFactory.TYPE).getAsString()
            .equals(LabelProbe.class.getName())) {
      Log.w("DEBUG", "SCDCPipeline.onDataReceived()/ LabelProbe data " +
              "received");

      if (handler != null) {
        handler.sendMessageAtFrontOfQueue(message);
      }
    } else {
      if (handler != null) {
        handler.sendMessage(message);
      }
    }
    // Added by Kilho Kim
    if (activity != null && activity instanceof LaunchActivity) {
      ((LaunchActivity)activity).updateScanCount();
    }
  }

  @Override
  public void onDataCompleted(IJsonObject probeConfig, JsonElement checkpoint) {
    // TODO Figure out what to do with continuations of probes, if anything

  }
}
