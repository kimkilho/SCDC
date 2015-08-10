package kr.ac.snu.imlab.scdc.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.SCDCKeys;

/**
 * Created by kilho on 15. 8. 10.
 */
public class BaseAdapterExSettings extends BaseAdapter {

  Context mContext = null;
  ArrayList<LabelEntry> mData = null;
  LayoutInflater mLayoutInflater = null;
  int currFocusedPosition;

  public BaseAdapterExSettings(Context context, ArrayList<LabelEntry> data) {
    this.mContext = context;
    this.mData = data;
    this.mLayoutInflater = LayoutInflater.from(this.mContext);
    this.currFocusedPosition = -1;
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

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
//    Log.d(SCDCKeys.LogKeys.DEBUG, "BaseAdapterExSettings.getView()/ " +
//            "position=" + position);
    View itemLayout = convertView;
    ViewHolder viewHolder;
    if (itemLayout == null) {
      itemLayout = mLayoutInflater.inflate(
                    R.layout.list_view_label_settings_layout, parent, false);

      viewHolder = new ViewHolder();
      viewHolder.labelNameTv =
          (TextView)itemLayout.findViewById(R.id.label_name);
      viewHolder.labelRepeatIntervalEt =
          (EditText)itemLayout.findViewById(R.id.label_repeat_interval);
      viewHolder.labelRepeatIntervalEt.setInputType(
          InputType.TYPE_CLASS_NUMBER);
      addIntervalChangedListener(viewHolder, position);

      itemLayout.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder)itemLayout.getTag();
      removeIntervalChangedListener(viewHolder);
      addIntervalChangedListener(viewHolder, position);
    }

    itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        return false;
      }
    });

    viewHolder.labelNameTv.setText(getItem(position).getName());
    viewHolder.labelRepeatIntervalEt.setText(String.valueOf(
            mData.get(position).getRepeatInterval()));

    viewHolder.labelRepeatIntervalEt.setOnFocusChangeListener(
      new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        currFocusedPosition = position;
      }
    });

//    if (currFocusedPosition != -1 && position == currFocusedPosition) {
//      viewHolder.labelRepeatIntervalEt.requestFocus();
//    }

    itemLayout.setClickable(true);
    return itemLayout;
  }

  private static class ViewHolder {
    LabelIntervalChangedListener intervalChangedListener;
    TextView labelNameTv;
    EditText labelRepeatIntervalEt;
  }

  private void addIntervalChangedListener(ViewHolder viewHolder, int position) {
    LabelIntervalChangedListener intervalChangedListener =
            new LabelIntervalChangedListener(mData, position);
    EditText labelRepeatIntervalEt = viewHolder.labelRepeatIntervalEt;
    labelRepeatIntervalEt.addTextChangedListener(intervalChangedListener);
    viewHolder.intervalChangedListener = intervalChangedListener;
  }

  private void removeIntervalChangedListener(ViewHolder viewHolder) {
    LabelIntervalChangedListener intervalChangedListener =
            viewHolder.intervalChangedListener;
    EditText labelRepeatIntervalEt = viewHolder.labelRepeatIntervalEt;
    labelRepeatIntervalEt.removeTextChangedListener(intervalChangedListener);
  }
}
