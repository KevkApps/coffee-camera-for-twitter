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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class SetTwitterAccountFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RequestToken mRequestToken;
    private AccessToken mAccessToken;
    public Activity activity;
    private String oauth_url;
    private String oauth_verifier;
    private String userName;
    private String userImageUrl;
    public Twitter twitter;
    private Handler mHandler;
    protected ScheduledThreadPoolExecutor stpe;
    private OnLoggedInListener mOnLoggedInListener;
    private Button buttonOne;
    private Button buttonTwo;
    private RelativeLayout relativeLayoutTwo;
    private RelativeLayout relativeLayoutOne;
    private TextView textViewOne;
    private TextView textViewTwo;
    private ImageView imageViewOne;
    private WebView webViewOne;
    private Vibrator mVibrator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        stpe = new ScheduledThreadPoolExecutor(3);

        mHandler = new Handler();

        activity = getActivity();

        mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        sharedPreferences = getActivity().getSharedPreferences("CoffeeCameraForTwitter", Context.MODE_PRIVATE);

        View view = inflater.inflate(R.layout.fragment_set_twitter_account, container, false);

        // web view for setting twitter account
        webViewOne = (WebView) view.findViewById(R.id.webView1);
        webViewOne.clearHistory();
        webViewOne.clearCache(true);
        webViewOne.bringToFront();

        // show account image and name after successful login
        relativeLayoutOne = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
        textViewOne = (TextView) view.findViewById(R.id.textView1);
        imageViewOne = (ImageView) view.findViewById(R.id.imageView1);
        buttonOne = (Button) view.findViewById(R.id.button1);

        // show error mesage if not correct account name or password
        relativeLayoutTwo = (RelativeLayout) view.findViewById(R.id.relativeLayout2);
        textViewTwo = (TextView) view.findViewById(R.id.textView2);
        buttonTwo = (Button) view.findViewById(R.id.button2);

        webViewOne.setVisibility(View.VISIBLE); // web view to set account
        relativeLayoutOne.setVisibility(View.GONE); // show image and name after login
        relativeLayoutTwo.setVisibility(View.GONE); // show error message

        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(CoffeeApplication.CONSUMER_KEY, CoffeeApplication.CONSUMER_SECRET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {

                }
            });

        } else {

            CookieManager.getInstance().removeAllCookie();
        }

        // ok button after loggedin success screen
        buttonOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mVibrator.vibrate(100);

                if(activity instanceof SetInitialTwitterAccountActivity) {

                    // return to CoffeeActivity
                    activity.finish();

                } else if (activity instanceof SettingsActivity) {

                    // close this fragment
                    ((SettingsActivity) activity).closeSetAccountFragment();
                    ((SettingsActivity) activity).setButtonsVisibility(true);

                }
            }
        });

        // ok button after error or wrong password screen
        buttonTwo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mVibrator.vibrate(100);

                if(activity instanceof SetInitialTwitterAccountActivity) {

                    // restart same activity
                    Intent intent = activity.getBaseContext().getPackageManager().getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                } else if (activity instanceof SettingsActivity) {

                    // restart this fragment again
                    ((SettingsActivity) activity).restartSetAccountFragment();

                }
            }
        });

        return view;

    } // onCreateView

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initTwitterAccount();

    } // onViewCreated called after oncreateView method above

    public void initTwitterAccount() {

        // to avoid opening webView in external browser always call
        // setWebViewClient before calling loadUrl for example
        webViewOne.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                // if url is correct format then show the successful login reuslt page
                if (url.contains("oauth_verifier") && url.startsWith(CoffeeApplication.CALLBACK_URL)) {

                    webViewOne.setVisibility(View.GONE);
                    relativeLayoutTwo.setVisibility(View.INVISIBLE);

                    Uri uri = Uri.parse(url);

                    // parse the uri to get the oauth_verifier
                    oauth_verifier = uri.getQueryParameter("oauth_verifier");

                    stpe.schedule(new GetLoginInfo(), 0, TimeUnit.SECONDS);

                    return true;

                    // one possible failure to login
                } else if (url.contains("username_or_email")) {

                    relativeLayoutOne.setVisibility(View.INVISIBLE);
                    relativeLayoutTwo.setVisibility(View.VISIBLE);
                    relativeLayoutTwo.bringToFront();
                    textViewTwo.setText("error, user name or email wrong");

                    return true;

                    // another fail to loging by denied result
                } else if (url.contains("denied")) {

                    relativeLayoutOne.setVisibility(View.INVISIBLE);
                    relativeLayoutTwo.setVisibility(View.VISIBLE);
                    relativeLayoutTwo.bringToFront();
                    textViewTwo.setText("access denied");

                    return true;

                    // any other error of not logging in
                } else {

                    relativeLayoutOne.setVisibility(View.INVISIBLE);
                    relativeLayoutTwo.setVisibility(View.VISIBLE);
                    relativeLayoutTwo.bringToFront();
                    textViewTwo.setText("error, not able to log in");

                    return true;

                } // if else

                // return true - to disable link and have application itself
                // handle the link
                // return false -  for link to send user to another external
                // page, application does not handle it

            } // shouldOverrideUrlLoading

        }); // setWebViewClient

        stpe.schedule(new GetRequestTokenAndLoadUrl(), 0, TimeUnit.SECONDS);

    } // initTwitterAccount

    public class GetRequestTokenAndLoadUrl implements Runnable {

        @Override
        public void run() {

            try {

                mRequestToken = twitter.getOAuthRequestToken();
                oauth_url = mRequestToken.getAuthorizationURL();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        // to enable JavaScript in WebView
                        webViewOne.getSettings().setJavaScriptEnabled(true);
                        webViewOne.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

                        webViewOne.loadUrl(oauth_url);
                        webViewOne.bringToFront();

                    }
                });

            } catch (TwitterException e) {

                if(activity instanceof SetInitialTwitterAccountActivity) {
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.setText("error, " + e.toString());
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.setDuration(2);
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.setRotationState(DisplayCustomTextView.NOT_ROTATED);
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.startDisplay();

                } else if(activity instanceof SettingsActivity) {
                    ((SettingsActivity) activity).mDisplayCustomTextView.setText("error, " + e.toString());
                    ((SettingsActivity) activity).mDisplayCustomTextView.setDuration(2);
                    ((SettingsActivity) activity).mDisplayCustomTextView.setRotationState(DisplayCustomTextView.NOT_ROTATED);
                    ((SettingsActivity) activity).mDisplayCustomTextView.startDisplay();
                }
            }
        }
    } // GetRequestToken

    public class GetLoginInfo implements Runnable {

        @Override
        public void run() {

            try {

                mAccessToken = twitter.getOAuthAccessToken(mRequestToken, oauth_verifier);

                User user = twitter.showUser(mAccessToken.getUserId());

                userName = user.getName();
                userImageUrl = user.getOriginalProfileImageURL();

                URL displayUrl = new URL(userImageUrl);

                final Bitmap bmp = BitmapFactory.decodeStream(displayUrl.openConnection().getInputStream());

                // load bitmap into internal memory location on device
                File directory = activity.getBaseContext().getDir("CoffeeCameraForTwitter", Context.MODE_PRIVATE);

                File path = new File(directory,"account_image.jpg");

                FileOutputStream fos = new FileOutputStream(path);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                fos.close();

                saveLoginInfo();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        relativeLayoutTwo.setVisibility(View.INVISIBLE);
                        relativeLayoutOne.setVisibility(View.VISIBLE);
                        relativeLayoutOne.bringToFront();

                        textViewOne.setText(userName.trim());

                        imageViewOne.setImageBitmap(bmp);

                        webViewOne.destroy();
                    }
                });

            } catch (Exception e) {

                if(activity instanceof SetInitialTwitterAccountActivity) {
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.setText("error, " + e.toString());
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.setDuration(2);
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.setRotationState(DisplayCustomTextView.NOT_ROTATED);
                    ((SetInitialTwitterAccountActivity) activity).mDisplayCustomTextView.startDisplay();

                } else if(activity instanceof SettingsActivity) {
                    ((SettingsActivity) activity).mDisplayCustomTextView.setText("error, " + e.toString());
                    ((SettingsActivity) activity).mDisplayCustomTextView.setDuration(2);
                    ((SettingsActivity) activity).mDisplayCustomTextView.setRotationState(DisplayCustomTextView.NOT_ROTATED);
                    ((SettingsActivity) activity).mDisplayCustomTextView.startDisplay();
                }

            }
        } // run

    } // GetLoginInfo

    public void saveLoginInfo() {

        mAccessToken.getTokenSecret();
        editor = sharedPreferences.edit();
        editor.putString("OAuthToken", mRequestToken.getToken().toString());
        editor.putString("OAuthSecret", mRequestToken.getTokenSecret().toString());
        editor.putString("userName", userName);
        editor.putString("userImageUrl", userImageUrl);
        editor.putString("accessToken", mAccessToken.getToken());
        editor.putString("accessTokenSecret", mAccessToken.getTokenSecret());
        editor.putBoolean("loggedInStatus", true);
        editor.commit();

    } // saveLoginInfo

    public void setOnLoggedInLister(OnLoggedInListener listener) {

        mOnLoggedInListener = listener;

    }

    public interface OnLoggedInListener {

        public abstract void onLoggedIn();

    }

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


} // SetTwitterAccountFragment