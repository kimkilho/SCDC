package kr.ac.snu.imlab.scdc.service.probe;

import android.Manifest;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.google.gson.JsonObject;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.builtin.ProbeKeys;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys;

@Probe.RequiredPermissions({Manifest.permission.ACCESS_NETWORK_STATE})
@Probe.DisplayName("Network Settings Log Probe")
@Probe.Description("Records Network status(mobile data on/off, wifi usage on/off, airplane mode on/off) for all time")
@Schedule.DefaultSchedule(interval = 0, duration = 0, opportunistic = true)
public class NetworkSettingsProbe extends Probe.Base implements Probe.ContinuousProbe {

    private static final String MOBILE_DATA_ON_NAME = "mobile_data";
    private JsonObject networkSettings;
    private static final int DEFAULT_VALUE = -1;
    private SettingsContentObserver settingsContentObserver;


    @Override
    protected void onEnable() {
        initializeNetworkSettings();
        initializeSettingsContentObserver();
        registerContentObserver();
    }

    private void initializeNetworkSettings() {
        networkSettings = getCurrentNetworkSettings();
        sendData(networkSettings);
    }

    private JsonObject getCurrentNetworkSettings() {
        JsonObject currentSystemSettings = new JsonObject();
        currentSystemSettings.addProperty(SCDCKeys.NetworkSettingsKeys.AIR_PLANE_MODE_ON, getCurrentValue(Settings.Global.AIRPLANE_MODE_ON));
        currentSystemSettings.addProperty(SCDCKeys.NetworkSettingsKeys.MOBILE_DATA_ON, getCurrentValue(MOBILE_DATA_ON_NAME));
        currentSystemSettings.addProperty(SCDCKeys.NetworkSettingsKeys.WIFI_ON, getCurrentValue(Settings.Global.WIFI_ON));
        return currentSystemSettings;
    }

    private int getCurrentValue(String name) {
        return android.provider.Settings.Global.getInt(getContext().getContentResolver(), name, DEFAULT_VALUE);
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
            JsonObject currentSystemSettings = getCurrentNetworkSettings();
            if (isNetworkSettingsChanged(currentSystemSettings)) {
                networkSettings = currentSystemSettings;
                sendData(networkSettings);
            }
        }

    }

    private void registerContentObserver() {
        getContext().getContentResolver().registerContentObserver(android.provider.Settings.Global.CONTENT_URI, true, settingsContentObserver);
    }

    private boolean isNetworkSettingsChanged(JsonObject currentSystemSettings) {
        networkSettings.remove(ProbeKeys.BaseProbeKeys.TIMESTAMP);
        return !(networkSettings.entrySet().equals(currentSystemSettings.entrySet()));
    }

    @Override
    protected void onDisable() {
        unregisterContentObserver();
    }

    private void unregisterContentObserver() {
        getContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
    }
}
