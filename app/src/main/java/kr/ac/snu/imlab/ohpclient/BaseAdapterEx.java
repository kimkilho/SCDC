package kr.ac.snu.imlab.ohpclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import java.util.ArrayList;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.DisplayName;

public class BaseAdapterEx extends BaseAdapter {
  Context mContext = null;
  ArrayList<ProbeEntry> mData = null;
  // ArrayList<Boolean> isEnableds = null;
  LayoutInflater mLayoutInflater = null;

  private FunfManager funfManager = null;
  private BasicPipeline pipeline = null;
  public static final String PIPELINE_NAME = "ohpclient";

  public BaseAdapterEx(Context context, ArrayList<ProbeEntry> data) {
    this.mContext = context;
    this.mData = data;
    // this.isEnableds = new ArrayList<Boolean>();
    // for (int i = 0; i < data.size(); i++) isEnableds.add(false);
    this.mLayoutInflater = LayoutInflater.from(this.mContext);
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
  public ProbeEntry getItem(int position) {
    return this.mData.get(position);
  }

  class ViewHolder {
    TextView registerProbeTextView;
    CheckBox enabledCheckBox;
    TextView scheduleTextView;
    Button changeScheduleButton;
  }

  @Override
  public boolean isEnabled(int position) {
    return true;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    View itemLayout = convertView;
    ViewHolder viewHolder = null;

    if (itemLayout == null) {
      itemLayout = mLayoutInflater.inflate(R.layout.list_view_item_layout, null);

      viewHolder = new ViewHolder();
      viewHolder.registerProbeTextView =
            (TextView)itemLayout.findViewById(R.id.registerProbeTextView);
      viewHolder.enabledCheckBox =
              (CheckBox)itemLayout.findViewById(R.id.enabledCheckBox);
      viewHolder.scheduleTextView =
              (TextView)itemLayout.findViewById(R.id.scheduleTextView);
      viewHolder.changeScheduleButton =
              (Button)itemLayout.findViewById(R.id.changeScheduleButton);

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

    viewHolder.registerProbeTextView.setText(mData.get(position)
            .getProbeClass().getAnnotation(DisplayName.class).value());
    // DEBUG: load enabledToggleButton view
    ToggleButton enabledToggleButton = (ToggleButton)((LaunchActivity)mContext)
            .findViewById(R.id.enabledToggleButton);
    // If enabledToggleButton is enabled, disable enabledCheckBox
    viewHolder.enabledCheckBox.setEnabled(!enabledToggleButton.isChecked());
    viewHolder.scheduleTextView.setText(R.string.probe_disabled);

    funfManager = ((LaunchActivity) mContext).getActivityFunfManager();
    if (funfManager != null) {
      if (enabledToggleButton.isChecked()) {
        pipeline = (BasicPipeline) funfManager.getRegisteredPipeline(PIPELINE_NAME);
        Probe.Base probe = mData.get(position).getProbe();
        if (mData.get(position).isEnabled()) {
          Schedule probeSchedule = funfManager.getDataRequestSchedule(probe.getConfig(), pipeline);
          viewHolder.scheduleTextView
                  .setText("Runs every "
                          + String.valueOf(probeSchedule.getInterval().longValue())
                          + " seconds for "
                          + String.valueOf(probeSchedule.getDuration().longValue())
                          + " seconds");
          // funfManager.requestData(pipeline, probe.getConfig().get("@type"), null);
          notifyDataSetChanged();
        }
      }
    }

    viewHolder.enabledCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         mData.get(position).setEnabled(isChecked);
      }
    });
    viewHolder.changeScheduleButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent rescheduleIntent = new Intent();
        ComponentName probeRescheduleActivity = new ComponentName(
                "kr.ac.snu.imlab.ohpclient",
                "kr.ac.snu.imlab.ohpclient.ProbeRescheduleActivity");
        rescheduleIntent.setComponent(probeRescheduleActivity);
        rescheduleIntent.putExtra("PROBE",
                mData.get(position).getProbeClass().getName());
        rescheduleIntent.putExtra("IS_ENABLED",
                mData.get(position).isEnabled());
        mContext.startActivity(rescheduleIntent);
      }
    });




    itemLayout.setClickable(true);
    return itemLayout;
  }

  public void setEnabledPipeline() {

  }
}
