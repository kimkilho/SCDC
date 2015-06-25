package kr.ac.snu.imlab.ohpclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class BaseAdapterExLabel extends BaseAdapter {
  Context mContext = null;
  ArrayList<LabelEntry> mData = null;
  // ArrayList<Boolean> isEnableds = null;
  LayoutInflater mLayoutInflater = null;

  public BaseAdapterExLabel(Context context, ArrayList<LabelEntry> data) {
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
    ViewHolder viewHolder = null;

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
    // viewHolder.registerProbeTextView.setText(mData.get(position)
    //        .getProbeClass().getAnnotation(DisplayName.class).value());
    // Load enabledToggleButton view from LaunchActivity context
    // ToggleButton enabledToggleButton = (ToggleButton)((LaunchActivity)mContext)
    //        .findViewById(R.id.enabledToggleButton);
    // If enabledToggleButton is enabled, disable enabledCheckBox
    viewHolder.scheduleTextView.setText(R.string.probe_disabled);

    itemLayout.setClickable(true);
    return itemLayout;
  }
}
