package kr.ac.snu.imlab.scdc.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import kr.ac.snu.imlab.scdc.R;
import kr.ac.snu.imlab.scdc.adapter.BaseAdapterExSettings;
import kr.ac.snu.imlab.scdc.entry.LabelEntry;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
 * Created by kilho on 15. 8. 7.
 */
public class SettingsActivity extends ActionBarActivity {

  private EditText generalAlarmRepeatIntervalEt = null;
  private BaseAdapterExSettings mAdapter = null;
  private ArrayList<LabelEntry> labelEntries = null;
  private SharedPrefsHandler spHandler = null;
  private Button saveChangesBt = null;
  private ListView mListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    // Make sure the keyboard only pops up
    // when a user clicks into an EditText
    this.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    spHandler = SharedPrefsHandler.getInstance(this,
            Config.SCDC_PREFS, Context.MODE_PRIVATE);

    generalAlarmRepeatIntervalEt =
      (EditText)findViewById(R.id.general_alarm_repeat_interval);
    generalAlarmRepeatIntervalEt.setText(
      String.valueOf(spHandler.getGeneralRepeatInterval()));

    labelEntries = new ArrayList<LabelEntry>();
    for (int i = 0; i < spHandler.getNumLabels(); i++) {
      labelEntries.add(new LabelEntry(i, null,
                              this, Config.SCDC_PREFS));
    }

    mAdapter = new BaseAdapterExSettings(this, labelEntries);
    mListView = (ListView)findViewById(R.id.label_settings_list_view);
    mListView.setAdapter(mAdapter);

    saveChangesBt = (Button)findViewById(R.id.save_changes);
    saveChangesBt.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
//        for (int i = 0; i < spHandler.getNumLabels(); i++) {
//          View viewItem = mListView.getChildAt(i);
//          EditText et = (EditText)viewItem.findViewById(R.id
//                  .label_repeat_interval);
//          String text = et.getText().toString().replaceAll("\\s+", "");
//          if (text.length() > 0) {
//            labelEntries.get(i).setRepeatInterval(Integer.parseInt(text));
//          }
//        }
        String text = generalAlarmRepeatIntervalEt.getText().toString();
        spHandler.setGeneralRepeatInterval(Integer.parseInt(text));
        finish();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }
}
