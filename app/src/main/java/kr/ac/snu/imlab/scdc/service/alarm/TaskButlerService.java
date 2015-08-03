package kr.ac.snu.imlab.scdc.service.alarm;

import android.content.Intent;

/**
  * Created by kilho on 15. 8. 3.
  */
 public class TaskButlerService extends WakefulIntentService {

  int mCountdownNum = 0;

  public TaskButlerService() {
    super("TaskButlerService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    mCountdownNum = intent.getExtras().getInt()


    // TasksDataSource db = TasksDataSource.getInstance(this);
      // get access to the instance of TasksDataSource
    // TaskAlarm alarm = new TaskAlarm();

    // List<Task> tasks = db.getAllTasks();
      // get a list of all the tasks there
    // for (Task task : tasks) {


    // }
    super.onHandleIntent(intent);
  }
 }
