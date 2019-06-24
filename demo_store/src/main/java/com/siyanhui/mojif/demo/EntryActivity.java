package com.siyanhui.mojif.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class EntryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout rootLayout = new FrameLayout(this);
        Button button = new Button(this);
        button.setText("进入");
        rootLayout.addView(button);
        ((FrameLayout.LayoutParams) button.getLayoutParams()).gravity = Gravity.CENTER;
        setContentView(rootLayout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntryActivity.this, MyActivity.class);
                startActivity(intent);
            }
        });
    }
}
