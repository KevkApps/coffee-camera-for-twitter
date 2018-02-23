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
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SetTweetMessageFragment extends Fragment {

    private SettingsActivity activity;
    private Button buttonOne;
    private EditText editTextOne;
    private Vibrator mVibrator;
    private String tweetMessage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (SettingsActivity) getActivity();

        mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        sharedPreferences = activity.getSharedPreferences("CoffeeCameraForTwitter", Context.MODE_PRIVATE);

    } // onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_set_tweet_message, container, false);

        buttonOne = (Button) view.findViewById(R.id.button1); // set default tweet message

        editTextOne = (EditText) view.findViewById(R.id.editText1);

        String tweetMessageInitial = sharedPreferences.getString("tweetMessage", CoffeeApplication.DEFAULT_TWEET_MESSAGE);
        editTextOne.setText(tweetMessageInitial);


        // set default tweet message to defaultTweetMessage
        buttonOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mVibrator.vibrate(100);

                tweetMessage = CoffeeApplication.DEFAULT_TWEET_MESSAGE;

                editor = sharedPreferences.edit();
                editor.putString("tweetMessage", tweetMessage);
                editor.commit();

                editTextOne.setText(tweetMessage);

                activity.mDisplayCustomTextView.setText("Tweet message set to \"" + tweetMessage + "\"");
                activity.mDisplayCustomTextView.setDuration(2);
                activity.mDisplayCustomTextView.setRotationState(DisplayCustomTextView.NOT_ROTATED);
                activity.mDisplayCustomTextView.startDisplay();

            }
        });

        // save changes from keyboard command
        editTextOne.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    tweetMessage = editTextOne.getText().toString().trim();

                    if (!tweetMessage.equals("")) {

                        editor = sharedPreferences.edit();
                        editor.putString("tweetMessage", tweetMessage);
                        editor.commit();

                        editTextOne.setText(tweetMessage);

                        activity.mDisplayCustomTextView.setText("Tweet message set to \"" + tweetMessage + "\"");
                        activity.mDisplayCustomTextView.setDuration(2);
                        activity.mDisplayCustomTextView.startDisplay();

                        // removes soft keyboard from screen
                        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(editTextOne.getWindowToken(), 0);

                    } // input not null
                    return true;
                }

                return false;
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

        tweetMessage = sharedPreferences.getString("tweetMessage", CoffeeApplication.DEFAULT_TWEET_MESSAGE);
        editTextOne.setText(tweetMessage);

    }

    @Override
    public void onPause() {

        tweetMessage = editTextOne.getText().toString().trim();

        editor = sharedPreferences.edit();
        editor.putString("tweetMessage", tweetMessage);
        editor.commit();

        activity.mDisplayCustomTextView.setText("Tweet message set to \"" + tweetMessage + "\"");
        activity.mDisplayCustomTextView.setDuration(2);
        activity.mDisplayCustomTextView.startDisplay();

        super.onPause();
    }

} // SetTweetMessageFragment