package kr.ac.snu.imlab.ohpclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.builtin.ProbeKeys.LabelKeys;

import java.util.ArrayList;

public class BaseAdapterExLabel extends BaseAdapter {
  Context mContext = null;
  ArrayList<LabelEntry> mData = null;
  // ArrayList<Boolean> isEnableds = null;
  LayoutInflater mLayoutInflater = null;

//  private FunfManager funfManager = null;
//  private BasicPipeline pipeline = null;
//  public static final String PIPELINE_NAME = "ohpclient";

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
    ToggleButton enabledToggleButton = (ToggleButton)((LaunchActivity)mContext).findViewById(R.id.enabledToggleButton);
    // If enabledToggleButton is enabled, enable startLogButton
    viewHolder.startLogButton.setEnabled(!mData.get(position).isLogged() &&
                                         enabledToggleButton.isChecked());
    viewHolder.endLogButton.setEnabled(mData.get(position).isLogged() &&
                                       enabledToggleButton.isChecked());
    if (viewHolder.endLogButton.isEnabled()) {
      viewHolder.scheduleTextView.setText("Currently " + mData.get(position).getName() + " for # minutes");
    } else {
      viewHolder.scheduleTextView.setText(R.string.probe_disabled);
    }

    viewHolder.startLogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mData.get(position).setLogged(true);
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

            // FIXME:
            viewHolder.scheduleTextView.setText("Currently " + mData.get(position).getName() + " for # minutes");
            v.setEnabled(false);
            viewHolder.endLogButton.setEnabled(true);
        }
    });

    viewHolder.endLogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mData.get(position).setLogged(false);
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

            viewHolder.scheduleTextView.setText(R.string.probe_disabled);
            v.setEnabled(false);
            viewHolder.startLogButton.setEnabled(true);
        }
    });

    itemLayout.setClickable(true);
    return itemLayout;
  }
}
