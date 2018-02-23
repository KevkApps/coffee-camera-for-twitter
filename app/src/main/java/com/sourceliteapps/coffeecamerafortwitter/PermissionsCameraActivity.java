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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PermissionsCameraActivity extends AppCompatActivity {

    private FrameLayout rootLayout;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions_camera);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        rootLayout = (FrameLayout) findViewById(R.id.frameLayout1);

        // get screen size
        rootLayout.post(new Runnable() {

            @Override
            public void run() {

                init(rootLayout.getWidth());
            }
        });
    } // onCreate

    @Override
    protected void onPause() {

            int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if(permissionCamera != PackageManager.PERMISSION_GRANTED) {

                finish();

            }

        super.onPause();
    } // onPause

    public void init(int width) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(PermissionsCameraActivity.this.LAYOUT_INFLATER_SERVICE);
        LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.permissions_rationale, null);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (width * 0.8), FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        rootLayout.addView(inflatedView, params);

        inflatedView.bringToFront();

        Button buttonOne = (Button) inflatedView.findViewById(R.id.button1);
        TextView textViewOne = (TextView) inflatedView.findViewById(R.id.textView1);

        textViewOne.setText("Camera permission needed, Please allow in app settings Permissions.");

        buttonOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mVibrator.vibrate(100);

                // takes user directly to the settings page for the app
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + "com.sourceliteapps.coffeecam"));
                startActivity(intent);
            }
        });

    } // init

    // handle back button press
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            // if user approves the permission to use camera it will send back to the app
            Intent intent = new Intent(PermissionsCameraActivity.this, CoffeeActivity.class);
            startActivity(intent);
            finish();

        } // if keycode == keycode back

        return false;

    } // onKeyDown


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CoffeeActivity.REQUEST_CAMERA_PERMISSIONS) {
            // if user approves the permission to use camera it will send back to the app
            Intent intent = new Intent(PermissionsCameraActivity.this, CoffeeActivity.class);
            startActivity(intent);
            finish();
        }
    } // onRequestPermissionsResult

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

} // PermissionsCameraActivity