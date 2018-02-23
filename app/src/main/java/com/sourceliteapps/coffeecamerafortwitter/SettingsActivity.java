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

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity implements SetTwitterAccountFragment.OnLoggedInListener {

    private Handler mHandler;
    private ImageView imageViewOne; // set tweet message button
    private ImageView imageViewTwo; // adjust delay button
    private ImageView imageViewThree; // change twitter account button
    private ImageView imageViewFour; // reset twitter account button
    private ImageView imageViewFive; // twitter is on / off button
    private View openView = null; // holds view to remove if exiting activity or back button press
    private SetTweetMessageFragment mSetTweetMessageFragment; // sets tweet message
    private AdjustDelayFragment mAdjustDelayFragment; // adjust delay
    private SetTwitterAccountFragment mSetTwitterAccountFragment; // sets twitter account
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ScheduledThreadPoolExecutor stpe;
    private boolean twitterEnabled = false;
    private ReturnToCoffeeTimer mReturnToCoffeeTimer = null;
    private Future<?> returnToCoffeeTimerFuture;
    private Vibrator mVibrator;
    private FrameLayout rootLayout; // root layout;
    protected boolean otherViewShowing = false; // fragment or view showing in root view
    public DisplayCustomTextView mDisplayCustomTextView; // displays result text status to user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        setContentView(R.layout.activity_settings);

        stpe = new ScheduledThreadPoolExecutor(3);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        sharedPreferences = getSharedPreferences("CoffeeCameraForTwitter", MODE_PRIVATE);

        rootLayout = (FrameLayout) findViewById(R.id.frameLayoutRoot); // root layout

        mDisplayCustomTextView = new DisplayCustomTextView(this, mHandler, rootLayout);

        mSetTweetMessageFragment = new SetTweetMessageFragment();
        mAdjustDelayFragment = new AdjustDelayFragment();
        mSetTwitterAccountFragment = new SetTwitterAccountFragment();

        imageViewOne = (ImageView) findViewById(R.id.imageView1); // set tweet message button
        imageViewTwo = (ImageView) findViewById(R.id.imageView2); // adjust delay button
        imageViewThree = (ImageView) findViewById(R.id.imageView3); // change twitter account button
        imageViewFour = (ImageView) findViewById(R.id.imageView4); // reset tweet counter button
        imageViewFive = (ImageView) findViewById(R.id.imageView5); // twitter is on / off button

        // set tweet message
        imageViewOne.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    mVibrator.vibrate(100);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();

                    if (!otherViewShowing) {

                        otherViewShowing = true;
                        clearReturnToCoffeeTimer();

                        if(mSetTweetMessageFragment.isAdded()) {

                            return true;

                        } else {

                            FragmentManager mFragmentManager = getFragmentManager();
                            FragmentTransaction transaction = mFragmentManager.beginTransaction();
                            transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
                            transaction.add(R.id.frameLayoutRoot, mSetTweetMessageFragment, "SetTweetMessage");
                            transaction.addToBackStack(null);
                            transaction.commit();

                        }

                    }
                }

                return false;

            } // onTouch
        }); // set tweet message

        // adjust delay
        imageViewTwo.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    mVibrator.vibrate(100);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();

                    if (!otherViewShowing) {

                        otherViewShowing = true;
                        clearReturnToCoffeeTimer();

                        if(mAdjustDelayFragment.isAdded()) {

                            return true;

                        } else {

                            FragmentManager mFragmentManager = getFragmentManager();
                            FragmentTransaction transaction = mFragmentManager.beginTransaction();
                            transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
                            transaction.add(R.id.frameLayoutRoot, mAdjustDelayFragment, "AdjustDelay");
                            transaction.addToBackStack(null);
                            transaction.commit();

                        }

                    } // ohterView showing

                        return true;

                }
                return false;
            } // onTouch
        }); // adjust delay

        // tweeting is on or off
        imageViewFive.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    mVibrator.vibrate(100);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();

                    if (!otherViewShowing) {

                        if (sharedPreferences.getBoolean("twitterEnabled", false)) {

                            editor = sharedPreferences.edit();
                            editor.putBoolean("twitterEnabled", false);
                            editor.commit();

                            imageViewFive.setImageResource(R.drawable.twitter_is_off);

                        } else {

                            editor = sharedPreferences.edit();
                            editor.putBoolean("twitterEnabled", true);
                            editor.commit();

                            imageViewFive.setImageResource(R.drawable.twitter_is_on);

                        }

                    } // check doors in motion, UILoaded and otherview showing

                    return true;

                }

                return false;

            } // onTouch
        }); // tweeting is on or off

        // change twitter account, using set twitter account view, log out and log in with different account
        imageViewThree.setOnTouchListener(new OnTouchListenerChangeAccount());

        // reset tweet counter
        imageViewFour.setOnTouchListener(new OnTouchListenerResetTweet());

        // done button

    } // onCreate

    @Override
    public void onLoggedIn() {

    }

    // set twitter account view, log out and log in with different account
    public class OnTouchListenerChangeAccount implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                mVibrator.vibrate(50);

                return true;

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();

                if (!otherViewShowing) {

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View inflatedView = inflater.inflate(R.layout.set_twitter_account_dialog, null);
                    final FrameLayout frameLayout = rootLayout; // final framelayout for passing in to anonymous class

                    openView = inflatedView; // pass view to openView global to be cleared in onPause if exiting activity

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.CENTER;
                    params.setMargins(20, 20, 20, 20);

                    frameLayout.addView(inflatedView, params);
                    frameLayout.bringToFront();

                    otherViewShowing = true;
                    clearReturnToCoffeeTimer();

                    Button buttonOne = (Button) inflatedView.findViewById(R.id.button1); // positive button
                    Button buttonTwo = (Button) inflatedView.findViewById(R.id.button2); // negative button
                    TextView textViewOne = (TextView) inflatedView.findViewById(R.id.textView1);

                    textViewOne.setText("Logout and change twitter account regestered with this app");

                    buttonOne.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            mVibrator.vibrate(50);

                            logOutOfTwitterAccount();

                            setButtonsVisibility(false);

                            if(mSetTwitterAccountFragment.isAdded()) {

                                return;

                            } else {

                                FragmentManager mFragmentManager = getFragmentManager();
                                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                                transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
                                transaction.add(R.id.frameLayoutRoot, mSetTwitterAccountFragment, "SetTwitterAccount");
                                transaction.addToBackStack(null);
                                transaction.commit();

                                if(openView != null) {

                                   rootLayout.removeView(openView);
                                   openView = null;

                                }

                                otherViewShowing = true;
                                clearReturnToCoffeeTimer();

                            }
                        }
                    }); // buttonOne

                    buttonTwo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mVibrator.vibrate(50);

                            if(openView != null) {

                                rootLayout.removeView(openView);
                                openView = null;

                            }

                            otherViewShowing = false;
                            restartReturnToCoffeeTimer();

                        }
                    }); // buttonTwo

                    return true;
                } // doorsInMotion and UILoaded and otherViewShowing check

            } // else if action up

            return false;
        }
    }; // OnTouchListenerChangeAccount

    // reset tweet counter view
    public class OnTouchListenerResetTweet implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                mVibrator.vibrate(50);

                return true;

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();

                if (!otherViewShowing) {

                    int counter = sharedPreferences.getInt("counter", 0);

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View inflatedView = inflater.inflate(R.layout.reset_tweet_counter, null);
                    final FrameLayout frameLayout = rootLayout; // final framelayout for passing in to anonymous class

                    openView = inflatedView; // pass view to global to be cleared in onPause if exiting activity

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.CENTER;
                    params.setMargins(20, 20, 20, 20);

                    frameLayout.addView(inflatedView, params);
                    frameLayout.bringToFront();

                    otherViewShowing = true;
                    clearReturnToCoffeeTimer();

                    Button buttonOne = (Button) inflatedView.findViewById(R.id.button1); // positive button
                    Button buttonTwo = (Button) inflatedView.findViewById(R.id.button2); // negative button
                    TextView textViewOne = (TextView) inflatedView.findViewById(R.id.textView1);

                    if (counter > 90) {

                        textViewOne.setText("WARNING, Counter is at the maximum number of tweets before needing a reset. " +
                                "If you reset the counter today it could go over the 100 maximum tweets allowed by" +
                                " twitter during a 24 hour period.");

                    } else if (counter <= 90) {

                        textViewOne.setText("Reset counter to zero, Counter is at the maximum number of tweets before needing a reset. " +
                                "If you reset the counter today it could go over the 100 maximum tweets allowed by" +
                                " twitter during a 24 hour period.");

                    }

                    buttonOne.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            mVibrator.vibrate(50);

                            editor = sharedPreferences.edit();
                            editor.putInt("counter", 0);
                            editor.commit();

                            mDisplayCustomTextView.setText("Counter has been reset to zero");
                            mDisplayCustomTextView.setDuration(2);
                            mDisplayCustomTextView.startDisplay();

                            if(openView != null) {

                                rootLayout.removeView(openView);
                                openView = null;

                            }

                            otherViewShowing = false;
                            restartReturnToCoffeeTimer();

                        }
                    });

                    buttonTwo.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            mVibrator.vibrate(50);

                            if(openView != null) {

                                rootLayout.removeView(openView);
                                openView = null;

                            }

                            otherViewShowing = false;
                            restartReturnToCoffeeTimer();

                        }
                    });
                }
            }

            return true;
        }

    }; // OnTouchListenerResetTweet

    public void logOutOfTwitterAccount() {

        editor = sharedPreferences.edit();
        editor.remove("OAuthToken");
        editor.remove("OAuthSecret");
        editor.remove("userName");
        editor.remove("userImageUrl");
        editor.remove("accessToken");
        editor.remove("accessTokenSecret");
        editor.remove("loggedInStatus");
        editor.commit();

    } // logOutOfTwitterAccount

    public void setButtonsVisibility(boolean visibility) {

        if(visibility) {

            imageViewOne.setVisibility(View.VISIBLE);
            imageViewTwo.setVisibility(View.VISIBLE);
            imageViewThree.setVisibility(View.VISIBLE);
            imageViewFour.setVisibility(View.VISIBLE);
            imageViewFive.setVisibility(View.VISIBLE);

        } else {

            imageViewOne.setVisibility(View.INVISIBLE);
            imageViewTwo.setVisibility(View.INVISIBLE);
            imageViewThree.setVisibility(View.INVISIBLE);
            imageViewFour.setVisibility(View.INVISIBLE);
            imageViewFive.setVisibility(View.INVISIBLE);
        }
    } // setButtonsVisibility

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        if ((mSetTwitterAccountFragment != null) && (mSetTwitterAccountFragment.isAdded())) {

            setButtonsVisibility(false);

        } else {

            setButtonsVisibility(true);

        }

        // get status of twitterEnabled
        twitterEnabled = sharedPreferences.getBoolean("twitterEnabled", false);

        if (twitterEnabled) {

            imageViewFive.setImageResource(R.drawable.twitter_is_on);

        } else {

            imageViewFive.setImageResource(R.drawable.twitter_is_off);
        }

        if(!otherViewShowing) {

            restartReturnToCoffeeTimer();

        }

    } // onResume

    @Override
    protected void onPause() {

        if(!otherViewShowing) {

            clearReturnToCoffeeTimer();
        }

        CoffeeApplication.origin.set(CoffeeApplication.FROM_SETTIGNS_ACTIVITY);

        super.onPause();

    } // onPause

    // handle back button press
    @Override
    public void onBackPressed() {

        if ((mSetTweetMessageFragment != null) && (mSetTweetMessageFragment.isAdded())) {

            FragmentManager mFragmentManager = getFragmentManager();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
            transaction.remove(mSetTweetMessageFragment);
            transaction.commit();

            otherViewShowing = false;
            restartReturnToCoffeeTimer();

        } else if ((mAdjustDelayFragment != null) && (mAdjustDelayFragment.isAdded())) {

            FragmentManager mFragmentManager = getFragmentManager();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
            transaction.remove(mAdjustDelayFragment);
            transaction.commit();

            otherViewShowing = false;
            restartReturnToCoffeeTimer();

        } else if ((mSetTwitterAccountFragment != null) && (mSetTwitterAccountFragment.isAdded())) {

            boolean loggedInStatus = sharedPreferences.getBoolean("loggedInStatus", false);

            if(!loggedInStatus) {

                FragmentManager mFragmentManager = getFragmentManager();
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
                transaction.remove(mSetTwitterAccountFragment);
                transaction.commit();

                mSetTwitterAccountFragment = new SetTwitterAccountFragment();

                transaction = mFragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
                transaction.add(R.id.frameLayoutRoot, mSetTwitterAccountFragment, "SetTwitterAccount");
                transaction.addToBackStack(null);
                transaction.commit();

            } else {

                FragmentManager mFragmentManager = getFragmentManager();
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
                transaction.remove(mSetTwitterAccountFragment);
                transaction.commit();

                otherViewShowing = false;
                restartReturnToCoffeeTimer();

                setButtonsVisibility(true);

            }

        } else if (openView != null) {

            rootLayout.removeView(openView);

            openView = null;

            otherViewShowing = false;
            restartReturnToCoffeeTimer();

        } else {

            clearReturnToCoffeeTimer();

            otherViewShowing = false;

            super.onBackPressed();

        }

    } // onBackPressed



    private class ReturnToCoffeeTimer implements Runnable {

        private int returnToCoffeeTimerCounter;

        public ReturnToCoffeeTimer() {

            returnToCoffeeTimerCounter = 0;
        }

        @Override
        public void run() {

            if(returnToCoffeeTimerCounter > 10) {

                returnToCoffeeTimerCounter = 0;

                finish();

            } else {

                returnToCoffeeTimerCounter++;

            }

        }

    } // ReturnToCoffeeActivity

    private void clearReturnToCoffeeTimer() {

        if(returnToCoffeeTimerFuture != null) {

            returnToCoffeeTimerFuture.cancel(true);

            mReturnToCoffeeTimer = null;
        }

    } // ClearReturnToCoffeeTimer

    protected void restartReturnToCoffeeTimer() {

        if(returnToCoffeeTimerFuture != null) {

            returnToCoffeeTimerFuture.cancel(true);
        }

        mReturnToCoffeeTimer = new ReturnToCoffeeTimer();

        returnToCoffeeTimerFuture = stpe.scheduleAtFixedRate(mReturnToCoffeeTimer, 0, 1000, TimeUnit.MILLISECONDS);


    } // restartReturnToCoffeeTimer

    public void closeSetAccountFragment() {

        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
        transaction.remove(mSetTwitterAccountFragment);
        transaction.commit();

        otherViewShowing = false;
        clearReturnToCoffeeTimer();

    } // closeSetAccountFragment

    public void restartSetAccountFragment() {

        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
        transaction.remove(mSetTwitterAccountFragment);
        transaction.commit();

        mSetTwitterAccountFragment = new SetTwitterAccountFragment();

        transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.view_fade_in, R.animator.view_fade_out, R.animator.view_fade_in, R.animator.view_fade_out);
        transaction.add(R.id.frameLayoutRoot, mSetTwitterAccountFragment, "SetTwitterAccount");
        transaction.addToBackStack(null);
        transaction.commit();

    } // restartSetAccountFragment

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

} // SettingsActivity