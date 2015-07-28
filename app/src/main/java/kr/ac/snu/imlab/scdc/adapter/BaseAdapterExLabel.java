package kr.ac.snu.imlab.scdc.adapter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import edu.mit.media.funf.probe.builtin.ProbeKeys.LabelKeys;
import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.activity.LaunchActivity;

import java.util.ArrayList;
import android.os.Handler;

public class BaseAdapterExLabel extends BaseAdapter {
  Context mContext = null;
  ArrayList<LabelEntry> mData = null;
  LayoutInflater mLayoutInflater = null;

  Handler handler;
//  ArrayList<Timer> mTimers = null;
//  TimerTask mCountTimerTask = null;

//  private FunfManager funfManager = null;
//  private BasicPipeline pipeline = null;
//  public static final String PIPELINE_NAME = "scdc";

  public BaseAdapterExLabel(Context context, ArrayList<LabelEntry> data) {
    this.mContext = context;
    this.mData = data;
    this.mLayoutInflater = LayoutInflater.from(this.mContext);
//    this.mTimers = new ArrayList<Timer>(data.size());

    // Create a new TimerTask
//    this.mCountTimerTask = new TimerTask() {
//      @Override
//      public void run() {
//
//      }
//    };
  }

  @Override
  public int getCount() {
    return this.mData.size();
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public LabelEntry getItem(int position) {
    return this.mData.get(position);
  }

  class ViewHolder {
    TextView logLabelTextView;
    TextView scheduleTextView;
    Button startLogButton;
    Button endLogButton;
  }

  @Override
  public boolean isEnabled(int position) {
    return true;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    View itemLayout = convertView;
    final ViewHolder viewHolder;

    if (itemLayout == null) {
      itemLayout = mLayoutInflater.inflate(R.layout.label_list_view_item_layout, null);

      viewHolder = new ViewHolder();
      viewHolder.logLabelTextView =
              (TextView)itemLayout.findViewById(R.id.logLabelTextView);
      viewHolder.scheduleTextView =
              (TextView)itemLayout.findViewById(R.id.scheduleTextView);
      viewHolder.startLogButton =
              (Button)itemLayout.findViewById(R.id.startLogButton);
      viewHolder.endLogButton =
              (Button)itemLayout.findViewById(R.id.endLogButton);

      itemLayout.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder)itemLayout.getTag();
    }

    itemLayout.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        return false;
      }
    });

    viewHolder.logLabelTextView.setText(mData.get(position).getName());

    // Load enabledToggleButton view from LaunchActivity context
    final ToggleButton enabledToggleButton = (ToggleButton)((LaunchActivity)mContext).findViewById(R.id.enabledToggleButton);
    // If enabledToggleButton is enabled, enable startLogButton
    viewHolder.startLogButton.setEnabled(!mData.get(position).isLogged() &&
                                         enabledToggleButton.isChecked());
    viewHolder.endLogButton.setEnabled(mData.get(position).isLogged() &&
                                       enabledToggleButton.isChecked());

    handler = new Handler();

    // Refresh the elapsed time if the label is logged
    if (mData.get(position).isLogged()) {
      String elapsedTime =
        getElapsedTimeUntilNow(mData.get(position).getStartLoggingTime());
      viewHolder.scheduleTextView.setText(
              "Currently " + mData.get(position).getName() +
              " for " + elapsedTime);
    } else {
      viewHolder.scheduleTextView.setText(R.string.probe_disabled);
    }

    viewHolder.startLogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // mData.get(position).setLogged(true);
            mData.get(position).startLog();
            // Start label logging
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.setAction(LabelKeys.ACTION_LABEL_LOG);
            for (int i = 0; i < mData.size(); i++) {
              intent.putExtra(mData.get(i).getName(), mData.get(i).isLogged());
            }
//            intent.putExtra(LabelKeys.LABEL_TYPE, mData.get(position).getName());
//            intent.putExtra(LabelKeys.IS_LABELLED, true);
            // Log.w("DEBUG", "LABEL_TYPE=" + intent.getStringExtra(LabelKeys.LABEL_TYPE) + ", IS_LABELLED=" + intent.getBooleanExtra(LabelKeys.IS_LABELLED, false));
            mContext.sendBroadcast(intent);

            BaseAdapterExLabel.this.notify(position, "SSC Client",
                    mData.get(position).getName(), "Label logging");

            // FIXME: Update the elapsed time
            handler.postDelayed(new Runnable() {
              @Override
              public void run() {
              }
            }, 1000L);
//            viewHolder.scheduleTextView.setText("Currently " + mData.get(position).getName() + " for # minutes");
            v.setEnabled(false);
            viewHolder.endLogButton.setEnabled(true);

            // Disable enabledToggleButton,
            // Intentionally wait 1 second for label changes to be saved
            // then enable enabledToggleButton
            enabledToggleButton.setEnabled(false);
            Toast.makeText(mContext,
                    "Saving the label log...",
                    Toast.LENGTH_SHORT).show();
              handler.postDelayed(new Runnable() {
              @Override
              public void run() {
                enabledToggleButton.setEnabled(true);
              }
            }, 5000L);

        }
    });

    viewHolder.endLogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // mData.get(position).setLogged(false);
            mData.get(position).endLog();
            // End label logging
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.setAction(LabelKeys.ACTION_LABEL_LOG);
            for (int i = 0; i < mData.size(); i++) {
              intent.putExtra(mData.get(i).getName(), mData.get(i).isLogged());
            }
            // intent.putExtra(LabelKeys.LABEL_TYPE, mData.get(position).getName());
            // intent.putExtra(LabelKeys.IS_LABELLED, false);
            // Log.w("DEBUG", "LABEL_TYPE=" + intent.getStringExtra(LabelKeys.LABEL_TYPE) + ", IS_LABELLED=" + intent.getBooleanExtra(LabelKeys.IS_LABELLED, true));
            mContext.sendBroadcast(intent);

            BaseAdapterExLabel.this.cancelNotify(position);

//            viewHolder.scheduleTextView.setText(R.string.probe_disabled);
            v.setEnabled(false);
            viewHolder.startLogButton.setEnabled(true);

            // Disable enabledToggleButton,
            // Intentionally wait 1 second for label changes to be saved
            // then enable enabledToggleButton
            enabledToggleButton.setEnabled(false);
            Toast.makeText(mContext,
                    "Saving the label log...",
                    Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
              @Override
              public void run() {
                enabledToggleButton.setEnabled(true);
              }
            }, 5000L);
        }
    });

    itemLayout.setClickable(true);
    return itemLayout;
  }

  private String getElapsedTimeUntilNow(long startTime) {
    if (startTime == -1) return null;

    long timeDelta = System.currentTimeMillis() - startTime;
    String elapsedTime = null;

    long secondsInMillis = 1000;
    long minutesInMillis = secondsInMillis * 60;
    long hoursInMillis = minutesInMillis * 60;
    long daysInMillis = hoursInMillis * 24;

    if (timeDelta < minutesInMillis) {
      elapsedTime = String.valueOf(timeDelta / secondsInMillis) + " seconds";
    } else if (timeDelta < hoursInMillis) {
      elapsedTime = String.valueOf(timeDelta / minutesInMillis) + " minutes";
    } else if (timeDelta < daysInMillis) {
      elapsedTime = String.valueOf(timeDelta / hoursInMillis) + " hours";
    } else {
      elapsedTime = String.valueOf(timeDelta / daysInMillis) + " hours";
    }

    return elapsedTime;
  }

  /*
  private void startTimerTask(int i) {
    // Stop TimerTask if running
    stopTimerTask();

    // Run TimerTask immediately and repeat on every 1000ms
    mTimers.get(i).schedule(mCountTimerTask, 0, 1000);
  }

  private void stopTimerTask() {
    // Stop all TimerTasks
    if (mCountTimerTask != null) {
      mCountTimerTask.cancel();
      mCountTimerTask = null;
    }
  }
  */


  protected void notify(int mId, String title, String message,
                           String alert) {
    Log.w("DEBUG", "BaseAdapterExLabel/ Start notification #" + mId + "- " +
            title + ": " + message);

//    // Create a new notification builder
//    NotificationCompat.Builder notification =
//            new NotificationCompat.Builder(this.mContext);
//    notification.setContentTitle(title);
//    notification.setContentText(message);
//    notification.setTicker(alert);
//    // notification.setSmallIcon(R.drawable.);
//
//    // Create an explicit intent for an Activity
//    Intent resultIntent = new Intent(mContext, LaunchActivity.class);
//    // Create a new stack builder
//    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.mContext);
//    stackBuilder.addParentStack((Activity)this.mContext);
//    stackBuilder.addNextIntent(resultIntent);
//    PendingIntent resultPendingIntent =
//            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//    notification.setContentIntent(resultPendingIntent);
//    notification.setContentIntent(PendingIntent.getActivity(mContext, 0,
//                                  new Intent(), 0));

    NotificationManager notificationMgr =
            (NotificationManager)mContext.
                    getSystemService(Context.NOTIFICATION_SERVICE);
//    notificationMgr.notify(mId, notification.build());

    @SuppressWarnings("deprecation")
    Notification notification = new Notification(R.mipmap.ic_launcher, alert,
            System.currentTimeMillis());
    notification.setLatestEventInfo(mContext, title, message,
            PendingIntent.getActivity(mContext, 0, new Intent(), 0));
    notificationMgr.notify(mId, notification);
  }

  protected void cancelNotify(int mId) {
    Log.w("DEBUG", "BaseAdapterExLabel/ Cancel notification #" + mId);
    NotificationManager notificationMgr =
            (NotificationManager)mContext.
                    getSystemService(Context.NOTIFICATION_SERVICE);
    notificationMgr.cancel(mId);
  }

  protected void cancelNotifyAll() {
    NotificationManager notificationMgr =
            (NotificationManager)mContext.
                    getSystemService(Context.NOTIFICATION_SERVICE);
    notificationMgr.cancelAll();
  }


}
