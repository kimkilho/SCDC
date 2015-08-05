package kr.ac.snu.imlab.scdc.service.probe;

import android.util.Log;

import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.probe.Probe;

@Probe.DisplayName("Control Panel Log Probe")
@Probe.Description("Records System content(screen brightness, network status, screen orientation, volume level) for all time")
@Schedule.DefaultSchedule(interval=0, duration=0, opportunistic=true)
public class SystemSettingsProbe extends Probe.Base implements Probe.ContinuousProbe{

    @Override
    protected void onEnable() {
        Log.e("test0805", "System settings Probe enabled");
    }

    @Override
    protected void onDisable(){
        Log.e("test0805", "System settings Probe disabled");
    }
}
