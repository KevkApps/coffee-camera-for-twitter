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

import android.app.Application;
import android.content.res.Resources;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.util.concurrent.atomic.AtomicInteger;

public class CoffeeApplication extends Application {

    public static AtomicInteger origin = new AtomicInteger(0);

    public static final String DEFAULT_TWEET_MESSAGE = "coffee is served #coffee #coffeecamerafortwitter";

    public static final String imageStoragePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/CoffeeCameraForTwitter";

    public static final int FROM_SETTIGNS_ACTIVITY = 3;
    public static final int FROM_COFFEE_ACTIVITY = 4;

    public static final int COUNTER_TWEET_LIMIT = 90; // the maximum number of tweets per day

    // Twitter account settings
    public static final String CONSUMER_KEY = "cEifhgwsdoC5bLnsFibu232W7";
    public static final String CONSUMER_SECRET = "yjLyBMVLc9hDp6EEYlO0NsHWTERdsa57v5jwrhR7LbR8Ud3PMp";
    public static final String CALLBACK_URL = "http://www.sourceliteapps.com/";
    public static final String CONSUMER_KEY2 = "cEifhgwy5g4jk3v3ibu232W7";
    public static final String CONSUMER_SECRET2 = "yjLyBMVLc9hDp6EEYlO0jhkI8TfxgF08Hgr4Dt5gFbR8Ud3PMp";
    public static final String CALLBACK_URL2 = "http://www.sourceliteapps.com/";
    public static final String CONSUMER_KEY3 = "cEifhgwsdoG7fV65bLnsFiW7";
    public static final String CONSUMER_SECRET3 = "yjLyBMVLh6ftR57gGhHjkrE4fdFgFhlj5jwrhR7LbR8Ud3PMp";
    public static final String CALLBACK_URL3 = "http://www.sourceliteapps.com/";
    public static final String CONSUMER_KEY4 = "cEifhgwsdg6Yuc5Dtyu232W7";
    public static final String CONSUMER_SECRET4 = "yjLyBMVLc9hDgf5fdTyT6jghgf8uJghFdweFs47LbR8Ud3PMp";
    public static final String CALLBACK_URL4 = "http://www.sourceliteapps.com/";
    public static final String CONSUMER_KEY5 = "cEifhgd7g5D8NgF5gRd232W7";
    public static final String CONSUMER_SECRET5 = "yjLyBMVLc9jh7gFT5trgDghgfjh7ygHgyhjoHg3LbR8Ud3PMp";
    public static final String CALLBACK_URL5 = "http://www.sourceliteapps.com/";
    public static final String CONSUMER_KEY6 = "cEifhgwh7gRf8J6Fibu232W7";
    public static final String CONSUMER_SECRET6 = "yjLyBMVLjh7yG8rlJ8ErdfD5Gs3DcsdfRojH7HjuoR8Ud3PMp";
    public static final String CALLBACK_URL6 = "http://www.sourceliteapps.com/";

    @Override
    public void onCreate() {
        super.onCreate();

        origin.set(FROM_COFFEE_ACTIVITY);

    }
} // CoffeeApplication