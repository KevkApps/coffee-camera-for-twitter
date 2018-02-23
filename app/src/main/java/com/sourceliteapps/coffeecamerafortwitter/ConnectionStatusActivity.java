/*
 * Copyright (C) 2017 Kevin Kasamo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourceliteapps.coffeecamerafortwitter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConnectionStatusActivity extends AppCompatActivity {

    private FrameLayout frameLayoutOne;
    private LinearLayout linearLayoutOne;
    private TextView textViewOne;
    private Button buttonOne;
    private Button buttonTwo;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connection_status);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        frameLayoutOne = (FrameLayout) findViewById(R.id.frameLayout1);
        linearLayoutOne = (LinearLayout) findViewById(R.id.linearLayout1);
        textViewOne = (TextView) findViewById(R.id.textView1);
        buttonOne = (Button) findViewById(R.id.button1);
        buttonTwo = (Button) findViewById(R.id.button2);

        textViewOne.setText("wifi or mobile data is tured off, this app requires an internet" +
                " connection. Do you want to go to settings to turn on Wifi now?");

        // cancel
        buttonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mVibrator.vibrate(100);

               System.exit(0);
            }
        });

        // go to settigns
        buttonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mVibrator.vibrate(100);

                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
                finish();
            }
        });

        // get layout width before init is called
        frameLayoutOne.post(new Runnable() {

            @Override
            public void run() {

                init(frameLayoutOne.getWidth());
            }
        });

    } // onCreate

    public void init(int width) {

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (width * 0.8), FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        linearLayoutOne.setLayoutParams(params);
        
    } // init

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    } // onWindowFocusChanged

} // ConnectionStatusActivity