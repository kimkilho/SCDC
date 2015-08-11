package kr.ac.snu.imlab.scdc.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.util.ArrayList;

import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.SCDCKeys;

/**
 * Created by kilho on 15. 8. 10.
 */
public class LabelIntervalChangedListener implements TextWatcher {

  private ArrayList<LabelEntry> mData;
  private int position;

  LabelIntervalChangedListener(ArrayList<LabelEntry> data, int position) {
    this.mData = data;
    this.position = position;
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count,
                                int after) {}

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
  }

  @Override
  public void afterTextChanged(Editable s) {
    String text = s.toString().replaceAll("\\s+","");
    if (text.length() > 0) {
//      Log.d(SCDCKeys.LogKeys.DEBUG, "BaseAdapterExSettings" +
//              ".addTextChangedListener.afterTextChanged()/ text=" + text
//              + ", position=" + position);
      mData.get(position).setRepeatInterval(Integer.parseInt(text));
    }
  }
}
