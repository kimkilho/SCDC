package kr.ac.snu.imlab.scdc.service.probe;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.JsonObject;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.builtin.ProbeKeys;
import kr.ac.snu.imlab.scdc.service.SCDCKeys;

@Probe.DisplayName("System Settings Log Probe")
@Probe.Description("Records System content(screen brightness, accelerometer rotation, volume) for all time")
@Schedule.DefaultSchedule(interval = 0, duration = 0, opportunistic = true)
public class SystemSettingsProbe extends Probe.Base implements Probe.ContinuousProbe {

    private AudioManager audioManager;
    private JsonObject systemSettings;
    private static final int DEFAULT_VALUE = -1;
    private SettingsContentObserver settingsContentObserver;


    @Override
    protected void onEnable() {
        initializeAudioManager();
        initializeSystemSettings();
        initializeSettingsContentObserver();
        registerContentObserver();
    }

    private void initializeAudioManager() {
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    private void initializeSystemSettings() {
        systemSettings = getCurrentSystemSettings();
        sendData(systemSettings);
    }

    private JsonObject getCurrentSystemSettings() {
        JsonObject currentSystemSettings = new JsonObject();
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.SCREEN_BRIGHTNESS, getCurrentValue(Settings.System.SCREEN_BRIGHTNESS));
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.ACCELEROMETER_ROTATION, getCurrentValue(Settings.System.ACCELEROMETER_ROTATION));
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.VOLUME_ALARM, getCurrentVolume(AudioManager.STREAM_ALARM));
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.VOLUME_MUSIC, getCurrentVolume(AudioManager.STREAM_MUSIC));
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.VOLUME_NOTIFICATION, getCurrentVolume(AudioManager.STREAM_NOTIFICATION));
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.VOLUME_RING, getCurrentVolume(AudioManager.STREAM_RING));
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.VOLUME_SYSTEM, getCurrentVolume(AudioManager.STREAM_SYSTEM));
        currentSystemSettings.addProperty(SCDCKeys.SystemSettingsKeys.VOLUME_VOICE, getCurrentVolume(AudioManager.STREAM_VOICE_CALL));
        return currentSystemSettings;
    }

    private int getCurrentValue(String name) {
        return android.provider.Settings.System.getInt(getContext().getContentResolver(), name, DEFAULT_VALUE);
    }

    private int getCurrentVolume(int streamType) {
        return audioManager.getStreamVolume(streamType);
    }


    private void initializeSettingsContentObserver() {
        settingsContentObserver = new SettingsContentObserver(new Handler());
    }

    private class SettingsContentObserver extends ContentObserver {

        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            JsonObject currentSystemSettings = getCurrentSystemSettings();
            if (isSystemSettingsChanged(currentSystemSettings)) {
                systemSettings = currentSystemSettings;
                sendData(systemSettings);
            }
        }

    }

    private void registerContentObserver() {
        getContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
    }

    private boolean isSystemSettingsChanged(JsonObject currentSystemSettings) {
        systemSettings.remove(ProbeKeys.BaseProbeKeys.TIMESTAMP);
        return !(systemSettings.entrySet().equals(currentSystemSettings.entrySet()));
    }

    @Override
    protected void onDisable() {
        unregisterContentObserver();
    }

    private void unregisterContentObserver() {
        getContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
    }
}
