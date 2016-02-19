package kr.ac.snu.imlab.scdc.service.core;

import android.app.Service;
import android.content.Intent;

/**
 * Created by kilho on 16. 2. 19.
 */
public class SCDCService extends Service {

  private SCDCManager scdcManager;
  private SCDCPipeline pipeline;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    scdcManager = ((SCDCManager.LocalBinder) service)



  }
}
