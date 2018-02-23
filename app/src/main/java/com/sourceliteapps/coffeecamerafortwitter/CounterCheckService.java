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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import java.util.Calendar;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CounterCheckService extends Service {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ScheduledThreadPoolExecutor stpe;
    private Future<?> mFuture;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("CoffeeCameraForTwitter", MODE_PRIVATE);

        stpe = new ScheduledThreadPoolExecutor(2);

        mFuture = stpe.scheduleAtFixedRate(new DateChecker(), 0, 1, TimeUnit.MINUTES);

    } // onCreate

    public class DateChecker implements Runnable {

        @Override
        public void run() {

            Calendar mCalendar = Calendar.getInstance();

            int dayNow = mCalendar.get(Calendar.DAY_OF_MONTH);
            int monthNow = mCalendar.get(Calendar.MONTH);
            int yearNow = mCalendar.get(Calendar.YEAR);

            int day = sharedPreferences.getInt("day", 0);
            int month = sharedPreferences.getInt("month", 0);
            int year = sharedPreferences.getInt("year", 0);

            boolean counterIncrementedPastLimit = sharedPreferences.getBoolean("counter_incremented_past_limit", false);

            // if different day from when tweet limit passed then the counter is cleared
            if (((dayNow != day) || (monthNow != month) || (yearNow != year)) && (counterIncrementedPastLimit)) {

                editor = sharedPreferences.edit();
                editor.putBoolean("counter_incremented_past_limit", false);
                editor.putInt("counter", 0);
                editor.commit();

            }
        }

    } // DateChecker

    @Override
    public void onDestroy() {

        if(mFuture != null) {

            mFuture.cancel(true);

        }

        super.onDestroy();

    } // onDestroy

} // CounterCheckService