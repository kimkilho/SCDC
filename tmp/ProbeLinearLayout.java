package kr.ac.snu.imlab.ohpclient;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProbeLinearLayout extends LinearLayout {

    public ProbeLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        View rootView = inflateView();
        TextView textView = (TextView) rootView.findViewById(R.id
                .tv_scheduleWifiProbe);
        textView.setText("kilho");
    }

    private View inflateView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.probe_linear_layout, this, true);
    }

}
