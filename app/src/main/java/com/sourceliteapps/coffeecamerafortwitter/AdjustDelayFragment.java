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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class AdjustDelayFragment extends Fragment {

    private SettingsActivity activity;
    private SeekBar seekBarOne;
    private TextView textViewThree;
    private int timeOutSeconds; // seconds delay
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (SettingsActivity) getActivity();

        sharedPreferences = activity.getSharedPreferences("CoffeeCameraForTwitter", Context.MODE_PRIVATE);

    } // onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_adjust_delay, container, false);

        seekBarOne = (SeekBar) view.findViewById(R.id.seekBar1);
        textViewThree = (TextView) view.findViewById(R.id.textView3);

        seekBarOne.setMax(60);

        timeOutSeconds = sharedPreferences.getInt("secondsDelay", 0);
        textViewThree.setText(String.valueOf(timeOutSeconds).trim());

        seekBarOne.setProgress(timeOutSeconds);

        seekBarOne.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                timeOutSeconds = seekBar.getProgress();
                textViewThree.setText(String.valueOf(timeOutSeconds).trim());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        return view;

    } // end on create view

    @Override
    public void onResume() {
        super.onResume();

        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    @Override
    public void onPause() {

        editor = sharedPreferences.edit();
        editor.putInt("secondsDelay", Integer.valueOf(String.valueOf(timeOutSeconds).trim()));
        editor.commit();

        activity.mDisplayCustomTextView.setText("Delay set to " + String.valueOf(timeOutSeconds).trim());
        activity.mDisplayCustomTextView.setDuration(2);
        activity.mDisplayCustomTextView.startDisplay();

        super.onPause();
    }

} // AdjustDelayFragment
