package kr.ac.snu.imlab.ohpclient;

import android.content.Context;
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

import java.util.ArrayList;

import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.DisplayName;

public class BaseAdapterEx extends BaseAdapter {
  Context mContext = null;
  ArrayList<ProbeEntry> mData = null;
  LayoutInflater mLayoutInflater = null;

  public BaseAdapterEx(Context context, ArrayList<ProbeEntry> data) {
    this.mContext = context;
    this.mData = data;
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
    viewHolder.scheduleTextView.setText(R.string.probe_disabled);
    viewHolder.enabledCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         mData.get(position).setEnabled(isChecked);
      }
    });
    viewHolder.changeScheduleButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // TODO: start activity: ProbeRescheduleActivity
        // by setting an intent and put extras to it
      }
    });

    itemLayout.setClickable(true);
    return itemLayout;
  }
}
