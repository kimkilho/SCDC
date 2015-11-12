package kr.ac.snu.imlab.scdc.adapter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
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

import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.activity.LaunchActivity;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;
import kr.ac.snu.imlab.scdc.util.TimeUtil;

import java.util.ArrayList;
import android.os.Handler;

public class BaseAdapterExLabel extends BaseAdapter {

  protected static final String TAG = "BaseAdapterExLabel";

  Context mContext = null;
  ArrayList<LabelEntry> mData = null;
  LayoutInflater mLayoutInflater = null;
  SharedPrefsHandler spHandler = null;

  Handler handler;

  public BaseAdapterExLabel(Context context, ArrayList<LabelEntry> data) {
    this.mContext = context;
    this.mData = data;
    this.mLayoutInflater = LayoutInflater.from(this.mContext);
    this.spHandler = SharedPrefsHandler.getInstance(this.mContext,
                        Config.SCDC_PREFS, Context.MODE_PRIVATE);
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
        TimeUtil.getElapsedTimeUntilNow(
          mData.get(position).getStartLoggingTime());
      viewHolder.scheduleTextView.setText(
              " for " + elapsedTime);
    } else {
      viewHolder.scheduleTextView.setText(R.string.probe_disabled);
    }

    viewHolder.startLogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean pastIsActiveLabelOn = spHandler.isActiveLabelOn();

            // Save current isLogged value of labelEntries from SharedPreferences
            mData.get(position).startLog();  // Start label logging

            v.setEnabled(false);
            viewHolder.endLogButton.setEnabled(true);

            boolean currIsActiveLabelOn = spHandler.isActiveLabelOn();
            // Update config again only when isActiveLabelOn status gets changed
            if (pastIsActiveLabelOn != currIsActiveLabelOn &&
                mContext instanceof LaunchActivity)
              ((LaunchActivity)mContext).updateConfig(currIsActiveLabelOn);
        }
    });

    viewHolder.endLogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean pastIsActiveLabelOn = spHandler.isActiveLabelOn();
            mData.get(position).endLog();  // End label logging

            v.setEnabled(false);
            viewHolder.startLogButton.setEnabled(true);

            boolean currIsActiveLabelOn = spHandler.isActiveLabelOn();
            // Update config again only when isActiveLabelOn status gets changed
            if (pastIsActiveLabelOn != currIsActiveLabelOn &&
                    mContext instanceof LaunchActivity)
              ((LaunchActivity)mContext).updateConfig(currIsActiveLabelOn);
        }
    });

    itemLayout.setClickable(true);
    return itemLayout;
  }

  protected void notify(int mId, String title, String message,
                           String alert) {

    // Create a new notification builder
    NotificationCompat.Builder builder =
       new NotificationCompat.Builder(mContext)
              .setAutoCancel(false)
              .setContentIntent(getPendingIntent(mId))
              .setContentTitle(title)
              .setContentText(message)
              .setTicker(alert)
              // .setDefaults(Notification.DEFAULT_ALL)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setOngoing(true)
              .setWhen(System.currentTimeMillis());

    @SuppressWarnings("deprecation")
    Notification notification = builder.getNotification();
    NotificationManager notificationMgr = (NotificationManager)mContext.
                          getSystemService(Context.NOTIFICATION_SERVICE);
    notificationMgr.notify(mId, notification);

  }

  protected void cancelNotify(int mId) {
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

  PendingIntent getPendingIntent(int id) {
    Intent intent = new Intent(mContext, LaunchActivity.class);
    return PendingIntent.getActivity(mContext, id, intent, 0);
  }

}
