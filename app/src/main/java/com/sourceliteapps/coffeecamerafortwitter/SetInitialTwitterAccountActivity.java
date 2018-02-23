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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SetInitialTwitterAccountActivity extends AppCompatActivity {

    private Handler mHandler;
    private Fragment mSetTwitterAccountFragment;
    public DisplayCustomTextView mDisplayCustomTextView; // displays result text status to user
    private FrameLayout rootLayout; // root layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_initial_twitter_account);

        mHandler = new Handler();

        rootLayout = (FrameLayout) findViewById(R.id.frameLayout1); // root layout

        mDisplayCustomTextView = new DisplayCustomTextView(this, mHandler, rootLayout);

        mSetTwitterAccountFragment = new SetTwitterAccountFragment();

    } // onCreate

    @Override
    protected void onResume() {
        super.onResume();

        if(mSetTwitterAccountFragment.isAdded()) {

            return;

        } else {

            FragmentManager mFragmentManager = getFragmentManager();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
            transaction.add(R.id.frameLayout1, mSetTwitterAccountFragment, "SetTwitterAccount");
            transaction.commit();

        }

    } // onResume

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

                // sends user back to app
                Intent intent = new Intent(SetInitialTwitterAccountActivity.this, CoffeeActivity.class);
                startActivity(intent);
                finish();

        } // if keycode == keycode back

        return false;

    } // onKeyDown

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

} // SetInitialTwitterAccountActivity