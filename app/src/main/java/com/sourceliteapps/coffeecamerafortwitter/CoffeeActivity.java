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
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CoffeeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,
        TakePicture.OnPictureTakenListener, Animation.AnimationListener {

    // access specifier in opencv library internal JavaCameraView class must be changed from private to public to
    // allow for access from this class to focus camera
    private FrameLayout rootLayout; // activity root view
    private ImageView imageViewTopCover; // to cover for screen
    private ImageView imageViewBottomCover; // bottom cover for screen
    private ImageView imageViewSix; // countdown timer image bottom left of screen
    private ImageView imageViewSeven; // twitter pause indicator bottom right of screen
    private ImageView imageViewShutter; // shutter image animation
    private volatile int detected = 0;
    private volatile int intervalCheckCounter = 0;
    private boolean tweetInProgress = false;
    private volatile boolean runIntervalCheck = false; // run check to see if cup is still in camera viewfinder
    private volatile int intervalCheckSum = 0;
    private boolean twitterEnabled = false;
    private boolean previewStatus = false; // true when preview is started after onCameraViewStarted is called
    private boolean surfaceClickable = false;
    public static final int INITIAL_DELAY_AFTER_DETECTION = 1000; // initial dealy before sending tweet in milliseconds
    public static final int REQUEST_CAMERA_PERMISSIONS = 1; // request code for camera permissions
    public static final int REQUEST_STORAGE_PERMISSIONS = 2; // request code for storage permissions
    private Mat mRgba;
    private Mat mGray;
    public JavaCameraView mJavaCameraView;
    private TakePicture mTakePicture; // Runnable for taking picture
    private FrameLayout splashFrame;
    private Animation fadeOut;
    private Animation shutterFadeInAndOut;
    private Vibrator mVibrator;
    protected int frameCounter = 0; // used to select a subset of inputFrames instead of all returned by onCameraFrame
    public DisplayCustomTextView mDisplayCustomTextView;
    private RestartObjectDetection mRestartObjectDetection;
    private TakeImageAndTweet mTakeImageAndTweet; // take image and start tweet process
    private boolean pictureTakable = false;
    private Countdown mCountdown; // shows graphic countdown to inform user of image being tweeted
    private Future<?> futureCountdown; // stop countdown for Countdown if exiting activity
    AtomicInteger imageCounter = new AtomicInteger(0); // counter graphic position counter
    protected int timeOutSeconds = 0; // timeout set by AdjustSafetyDelayFragment class
    private static boolean staticLoaded = false; // used for OpenCV static initialization
    protected static int rootLayoutWidth = 0;
    protected static int rootLayoutHeight = 0;
    private int navigationBarHeight = 0;
    private Handler mHandler;
    private ScheduledThreadPoolExecutor stpe;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;
    private Camera mCamera;

    // broadcast receiver gets broadcast from from OS or wifi state
    // if wifi is turned off alert dialog is started to direct user to turn it on
    public BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals("tweetSent")) {

                runIntervalCheck = true;

                mDisplayCustomTextView.setText("TWEET SENT");
                mDisplayCustomTextView.setDuration(3);
                mDisplayCustomTextView.setRotationState(DisplayCustomTextView.ROTATED);
                mDisplayCustomTextView.startDisplay();

            }

            if (action.equals("tweetMaxLimit")) {

                runIntervalCheck = true;

                mDisplayCustomTextView.setText("at maximum daily tweet limit of 90 per day");
                mDisplayCustomTextView.setDuration(4);
                mDisplayCustomTextView.setRotationState(DisplayCustomTextView.ROTATED);
                mDisplayCustomTextView.startDisplay();

            }

            if (action.equals("errorTweetNotSent")) {

                runIntervalCheck = true;

                mDisplayCustomTextView.setText("ERROR TWEET NOT SENT " + intent.getExtras().getString("errorMessage"));
                mDisplayCustomTextView.setDuration(10);
                mDisplayCustomTextView.setRotationState(DisplayCustomTextView.ROTATED);
                mDisplayCustomTextView.startDisplay();
            }

            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

                // if wifi is currently set to on, returned int is 3
                // if wifi is currently set to off, returned int is 1
                // only checks if wifi is turned on for the device, not actual internate connectivity
            }

        } // end on receive
    }; // BroadcastReceive

    //  used for openCV static initialization
    static {

        if (OpenCVLoader.initDebug()) {

            staticLoaded = true;

        } else {

            staticLoaded = false;

        }
    } // static block

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        // timer threads
        stpe = new ScheduledThreadPoolExecutor(8);

        sharedPreferences = getSharedPreferences("CoffeeCameraForTwitter", MODE_PRIVATE);

        int firstInstall = sharedPreferences.getInt("firstInstall", 0);

        // set initial tweet message in shared preferences if not set before
        if (firstInstall != 2) {

            editor = sharedPreferences.edit();
            editor.putInt("firstInstall", 2);
            editor.putString("tweetMessage", CoffeeApplication.DEFAULT_TWEET_MESSAGE);
            editor.putInt("counter", 0);
            editor.remove("OAuthToken");
            editor.remove("OAuthSecret");
            editor.remove("userName");
            editor.remove("userImageUrl");
            editor.remove("accessToken");
            editor.remove("accessTokenSecret");
            editor.putBoolean("loggedInStatus", false);
            editor.commit();

        }

        // haptic feedback when button is pressed or picture is taken
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        setContentView(R.layout.activity_coffee);

        // prevent screen from turning off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // gets height of navigation bar
        Resources resources = getResources();
        int resourceIdNavigationBar = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        navigationBarHeight = resources.getDimensionPixelSize(resourceIdNavigationBar);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        rootLayoutWidth = displayMetrics.widthPixels;
        rootLayoutHeight = displayMetrics.heightPixels + navigationBarHeight;

        mJavaCameraView = (JavaCameraView) findViewById(R.id.camera_surface_view);

        mJavaCameraView.setRootLayoutWidthAndHeight(rootLayoutWidth, rootLayoutHeight);

        mJavaCameraView.setCvCameraViewListener(CoffeeActivity.this);

        mCountdown = new Countdown();

        mTakeImageAndTweet = new TakeImageAndTweet();

        mRestartObjectDetection = new RestartObjectDetection();

        mTakePicture = new TakePicture();
        mTakePicture.setOnPictureTakenListener(this);

        // visible root layout
        rootLayout = (FrameLayout) findViewById(R.id.activityRootView);

        splashFrame = (FrameLayout) findViewById(R.id.splashFrame);

        splashFrame.setVisibility(View.INVISIBLE);

        fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(this);

        mDisplayCustomTextView = new DisplayCustomTextView(this, mHandler, rootLayout);

        imageViewTopCover = (ImageView) findViewById(R.id.imageViewTopCover);
        imageViewBottomCover = (ImageView) findViewById(R.id.imageViewBottomCover);

        // imageview for countdown and user account bottom left of screen
        imageViewSix = (ImageView) findViewById(R.id.imageView6);

        // twitter function paused bottom right of screen
        imageViewSeven = (ImageView) findViewById(R.id.imageView7);

        // set initial default image if not logged in
        imageViewSeven.setImageResource(R.drawable.clear);

        imageViewShutter = (ImageView) findViewById(R.id.imageViewShutter);

        imageViewShutter.setVisibility(View.INVISIBLE);

        shutterFadeInAndOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_and_out);
        shutterFadeInAndOut.setAnimationListener(this);

        // touch main screen root view sends to settings screen
        rootLayout.setOnTouchListener(new View.OnTouchListener() {

            int touchDown = 0;

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    touchDown = (int) event.getY();

                    mVibrator.vibrate(100);

                    return true;

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

                    // if touch enent is not at edge of screen over navigation bar and it is not a swipe event
                    if(((int) event.getY()) < (rootLayoutHeight - (navigationBarHeight + (navigationBarHeight / 2))) &&
                            !(Math.abs(touchDown - (int) event.getY()) > 30) && surfaceClickable) {

                        Intent intent = new Intent(CoffeeActivity.this, SettingsActivity.class);
                        startActivity(intent);

                    }

                    return true;
                }
                return true;
            }
        });

        final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

            @Override
            public void onGlobalLayout() {

                int coverWidth = (rootLayout.getWidth() - rootLayout.getHeight()) / 2;
                int coverHeight = rootLayout.getWidth();

                imageViewTopCover.getLayoutParams().width = coverWidth;
                imageViewTopCover.getLayoutParams().height = coverHeight;
                imageViewTopCover.requestLayout();

                imageViewBottomCover.getLayoutParams().width = coverWidth;
                imageViewBottomCover.getLayoutParams().height = coverHeight;
                imageViewBottomCover.requestLayout();

                rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            } // onGlobalLayout

        };

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

    } // onCreate

    @Override
    protected void onDestroy() {

        unregisterReceiver(receiver);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        if(CoffeeApplication.origin.get() != CoffeeApplication.FROM_SETTIGNS_ACTIVITY) {

            splashFrame.setVisibility(View.VISIBLE);

            splashFrame.setAnimation(fadeOut);
            splashFrame.startAnimation(fadeOut);

        } else {

            surfaceClickable = true;

            splashFrame.setVisibility(View.INVISIBLE);

        }

        if ((mJavaCameraView != null) && staticLoaded) {

            mJavaCameraView.enableView();

        }

        pictureTakable = true;
        imageCounter.set(0);
        runIntervalCheck = false;
        tweetInProgress = false;
        frameCounter = 0;
        intervalCheckCounter = 0;

        imageViewShutter.setVisibility(View.INVISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("tweetSent");
        filter.addAction("tweetMaxLimit");
        filter.addAction("errorTweetNotSent");
        registerReceiver(receiver, filter);

        // get status of twitterEnabled
        twitterEnabled = sharedPreferences.getBoolean("twitterEnabled", false);
        if (twitterEnabled) {

            imageViewSix.setImageResource(R.drawable.twitter_active);

        } else {

            imageViewSix.setImageResource(R.drawable.twitter_paused);
        }

        // start service CounterCheckService
        Intent service = new Intent(this, CounterCheckService.class);
        startService(service);

        timeOutSeconds = sharedPreferences.getInt("secondsDelay", 0);

        // check internet connection status
        if (!checkInternetConnectionStatus()) {

            Intent intent = new Intent(this, ConnectionStatusActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);

            return;

        } // check internet connection status

        // check or get user permissions for camera and external storage
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // handle camera permission
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                Intent intent = new Intent(this, PermissionsCameraActivity.class);
                startActivity(intent);

                return;

            } else {

                String[] permissions = new String[]{Manifest.permission.CAMERA};
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSIONS);

                return;

            }

        } // handle camera permission if not granted

        // handle write to storage permission
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Intent intent = new Intent(this, PermissionsWriteStorageActivity.class);
                startActivity(intent);

                return;

            } else {

                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, REQUEST_STORAGE_PERMISSIONS);

                return;

            }

        } // handle storage permission if not granted

        // check logged in status
        boolean loggedInStatus = sharedPreferences.getBoolean("loggedInStatus", false);

        if(!loggedInStatus) {

            Intent intent = new Intent(CoffeeActivity.this, SetInitialTwitterAccountActivity.class);
            startActivity(intent);

            return;

        } else {

            // get image from internal storage and set for main activity if logged in
            try {

                File directory = getBaseContext().getDir("CoffeeCameraForTwitter", MODE_PRIVATE);

                File file = new File(directory, "account_image.jpg");

                FileInputStream fis = new FileInputStream(file);

                Bitmap bmp = BitmapFactory.decodeStream(fis);

                imageViewSeven.setImageBitmap(bmp);

                fis.close();

            }

            catch (Exception e) {

            }

        } // if else logged in

    } // onResume

    private boolean checkInternetConnectionStatus() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = connManager.getActiveNetworkInfo();

        if (netInfo == null) {

            return false;

        } else {

            return true;
        }

    } // checkInternetConnectionStatus

    @Override
    protected void onPause() {

        CoffeeApplication.origin.set(CoffeeApplication.FROM_COFFEE_ACTIVITY);

        pictureTakable = false;
        imageCounter.set(0);
        runIntervalCheck = false;
        tweetInProgress = false;
        frameCounter = 0;
        intervalCheckCounter = 0;

        // stop service CounterCheckService
        Intent service = new Intent(this, CounterCheckService.class);
        stopService(service);

        if (mJavaCameraView != null && (staticLoaded)) {

            mJavaCameraView.disableView();
        }

        if (futureCountdown != null) {

            futureCountdown.cancel(true);
        }

        imageViewSeven.setImageResource(R.drawable.clear);

        super.onPause();

    } // onPause

    @Override
    public void onCameraViewStarted(int width, int height) {

        mGray = new Mat();
        mRgba = new Mat();

        // access specifier in opencv library JavaCameraView must be changed from private to public to
        // allow for access from this class to focus camera
        mCamera = mJavaCameraView.getCamera();

        mCamera.autoFocus(null);

        previewStatus = true;

    } // onCameraViewStarted

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        /*
         * This types define color depth, number of channels and channel layout
		 * in the image. On Android the most useful are CvType.CV_8UC4 and
		 * CvType.CV_8UCq. CvType.CV_8UC4 is 8-bit per channel RGBA image and
		 * can be captured from camera with NativeCameraView or JavaCameraView
		 * classes and drawn on surface. CvType.CV_8UC1 is gray scale image and
		 * is mostly used in computer vision algorithms.
		 */

        mGray = inputFrame.gray();
        mRgba = inputFrame.rgba();

        // user Rect and new Mat for taking a smaller image of the supplied frame for detection
        // create mGraySource Mat to get the grayscale image then resize it using Rect
        // can not use this resizing for the mRgba that is returned back to the function, that size can not be changed
        // Rect roi = new Rect(0, 0 ,mGraySource.cols(), mGraySource.rows()); // this example is same size as input frame, no change
        // mGray = new Mat(mGraySource, roi);

        // pick a subset of inputFrames instead of every inputFrame returned by onCameraFrame
        if (frameCounter > 6) {

            frameCounter = 0;
        }

        frameCounter++;

        if ((frameCounter == 1) && previewStatus && twitterEnabled) {

            Size size = new Size(mGray.width() / 3, mGray.height() / 3);

            Imgproc.resize(mGray, mGray, size);

            MatOfRect circles = new MatOfRect();

            Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 2, 2);

            Imgproc.HoughCircles(mGray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, mGray.rows(), 150, 60, 30, 0);

            detected = circles.cols();

            if ((detected > 0) && (!tweetInProgress)) {

                tweetInProgress = true;

                // start process for countdown graphic
                futureCountdown = stpe.scheduleAtFixedRate(mCountdown, INITIAL_DELAY_AFTER_DETECTION, 1000, TimeUnit.MILLISECONDS);

                // start thread for take picture and send image
                stpe.schedule(mTakeImageAndTweet, 4000, TimeUnit.MILLISECONDS);

            } // if detected and not tweetInProgress

            if (runIntervalCheck) {

                intervalCheck();
            }

            mGray.release();
            circles.release();
            System.gc();

        } // if check for doorsInMotion, frameCounter, and previewStatus

        return mRgba;

    } // onCameraFrame

    public class TakeImageAndTweet implements Runnable {

        @Override
        public void run() {

            if(pictureTakable) {

                Bitmap.Config conf = Bitmap.Config.ARGB_8888;

                Bitmap bmp = Bitmap.createBitmap(mRgba.width(), mRgba.height(), conf);

                Utils.matToBitmap(mRgba, bmp);

                mTakePicture.setBitamp(bmp);

                // focus camera and take picture after AutoFocus listener callback
                if (previewStatus) {

                    CoffeeActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            imageViewShutter.setAnimation(shutterFadeInAndOut);
                            imageViewShutter.startAnimation(shutterFadeInAndOut);

                        }
                    });

                    stpe.schedule(mTakePicture, 0, TimeUnit.MILLISECONDS);

                    mVibrator.vibrate(100);
                }

            }

        }

    } // TakeImageAndTweet

    // start process for countdown graphic and send tweet message with image
    public class Countdown implements Runnable {

        @Override
        public void run() {

            CoffeeActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (imageCounter.get() == 0) {

                        imageCounter.incrementAndGet();

                        imageViewSix.setImageResource(R.drawable.countdown_3);

                    } else if (imageCounter.get() == 1) {

                        imageCounter.incrementAndGet();

                        imageViewSix.setImageResource(R.drawable.countdown_2);

                        mCamera.autoFocus(null);

                    } else if (imageCounter.get() == 2) {

                        imageCounter.incrementAndGet();

                        imageViewSix.setImageResource(R.drawable.countdown_1);

                    } else if (imageCounter.get() == 3) {

                        imageCounter.incrementAndGet();

                        // set imageViewSix back to twitter enabled status indicator
                        if (twitterEnabled) {

                            imageViewSix.setImageResource(R.drawable.twitter_active);

                        } else {

                            imageViewSix.setImageResource(R.drawable.twitter_paused);
                        }

                    } else if (imageCounter.get() == 4) {

                        imageCounter.set(0);

                        if(futureCountdown != null) {

                            futureCountdown.cancel(true);

                        }

                    } // if else

                } // run

            });

        } // run

    } // Countdown

    // check to see if cup is still in camera viewfinder
    public void intervalCheck() {

        if (intervalCheckCounter < 3) {

            if (detected > 0) {

                intervalCheckSum++;

            }

            intervalCheckCounter++;

        } else {

            if (intervalCheckSum > 0) {

                // wait for object removal
                runIntervalCheck = true;
                intervalCheckSum = 0;

            } else {

                runIntervalCheck = false;
                // restart object detection wiht delay of timeOutSeconds set by user
                stpe.schedule(mRestartObjectDetection, timeOutSeconds, TimeUnit.SECONDS);

            }

            intervalCheckCounter = 0;

        } // else not smaller than 3

    } // intervalCheck

    // restarts object detection wiht delay of timeOutSeconds set by user
    public class RestartObjectDetection implements Runnable {

        @Override
        public void run() {

            tweetInProgress = false;

            CoffeeActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (mDisplayCustomTextView != null) {
                        mDisplayCustomTextView.setText("Coffee detection restarted after safety delay");
                        mDisplayCustomTextView.setDuration(2);
                        mDisplayCustomTextView.setRotationState(DisplayCustomTextView.ROTATED);
                        mDisplayCustomTextView.startDisplay();
                    }

                }

            });

        } // run

    } // RestartObjectDetection

    @Override
    public void onCameraViewStopped() {

        previewStatus = false;

        mGray.release();
        mRgba.release();

    } // onCameraViewStopped

    @Override
    public void onPictureTaken() {

        // start Twitter intent service after picture is taken and saved
        Intent startTwitterServiceIntent = new Intent(CoffeeActivity.this, TwitterService.class);
        startService(startTwitterServiceIntent);

    } // onPictureTaken

    private class TwitterServiceLaunch implements Runnable {

        @Override
        public void run() {

            Intent intent = new Intent(CoffeeActivity.this, TwitterService.class);
            CoffeeActivity.this.startService(intent);

        } // run
    } // TwitterServiceLaunch

    // handle back button press
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            CoffeeApplication.origin.set(CoffeeApplication.FROM_COFFEE_ACTIVITY);

            finish();

        } // if keycode == keycode back

        return false;
    } // onKeyDown

    @Override
    public void onAnimationStart(Animation animation) {

        if (animation.equals(shutterFadeInAndOut)) {

            imageViewShutter.setVisibility(View.VISIBLE);
            imageViewShutter.bringToFront();

        }

    } // onAnimationStart

    @Override
    public void onAnimationEnd(Animation animation) {

        if (animation.equals(fadeOut)) {

            splashFrame.setVisibility(View.INVISIBLE);
            surfaceClickable = true;
        }

    } // onAnimationEnd

    @Override
    public void onAnimationRepeat(Animation animation) {

    } // onAnimationRepeat

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    } // onWindowFocusChanged

} // CoffeeActivity