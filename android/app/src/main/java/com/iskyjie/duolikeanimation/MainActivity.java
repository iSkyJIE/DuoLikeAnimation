package com.iskyjie.duolikeanimation;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView text = new TextView(this);
        text.setText("DuoLikeAnimation\nSAFE BOOT OK");
        text.setTextSize(24f);
        text.setTextColor(Color.BLACK);
        text.setBackgroundColor(Color.WHITE);
        text.setGravity(Gravity.CENTER);
        text.setPadding(48, 48, 48, 48);

        setContentView(text);
    }
}
