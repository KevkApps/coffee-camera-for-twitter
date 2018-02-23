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

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Calendar;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterService extends IntentService {

private SharedPreferences sharedPreferences;
private SharedPreferences.Editor editor;

private String accessTokenString;
private String accessTokenSecretString;

private boolean tweetError = false;

private TwitterException mTwitterException;

Intent intent = new Intent();

public TwitterService() {

	super(TwitterService.class.getSimpleName());

} // TwitterService constructor

		@Override
		protected void onHandleIntent(Intent intent) {

			sharedPreferences = getApplicationContext().getSharedPreferences("CoffeeCameraForTwitter", MODE_PRIVATE);

			accessTokenString = sharedPreferences.getString("accessToken", "");
			accessTokenSecretString = sharedPreferences.getString("accessTokenSecret", "");

			int counter = sharedPreferences.getInt("counter", 0);

			if(counter < CoffeeApplication.COUNTER_TWEET_LIMIT) {

				counter++;

				editor = sharedPreferences.edit();
			   	editor.putInt("counter", counter);
			    editor.commit();

				// tweet and upload image to twitter
				// for display adds 1000 to twitter counter
				tweetAction((counter--) + 1000);

			} else if(counter == CoffeeApplication.COUNTER_TWEET_LIMIT) {

				counter++;

				Calendar mCalendar = Calendar.getInstance();

				int day = mCalendar.get(Calendar.DAY_OF_MONTH);
				int month = mCalendar.get(Calendar.MONTH);
				int year = mCalendar.get(Calendar.YEAR);

				editor = sharedPreferences.edit();
				editor.putInt("day", day);
				editor.putInt("month", month);
				editor.putInt("year", year);
				editor.putBoolean("counter_incremented_past_limit", true);
				editor.putInt("counter", counter);
				editor.commit();

				// tweet and upload image to twitter
				// for display adds 1000 to twitter counter
				tweetAction((counter--) + 1000);

			} else if(counter > CoffeeApplication.COUNTER_TWEET_LIMIT) {

				intent.setAction("tweetMaxLimit");
				TwitterService.this.sendBroadcast(intent);

		 	}

		} // onHandleIntent
		
		public void tweetAction(int counter) {

				String imageName = "coffee.jpg";

			    File fileDirectory = new File(CoffeeApplication.imageStoragePath);

			    File mFile = new File(fileDirectory, imageName);

				String tweetMessage = sharedPreferences.getString("tweetMessage", CoffeeApplication.DEFAULT_TWEET_MESSAGE);

				// text tweet message
				String message = tweetMessage + "   " + counter;

				ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
				configurationBuilder.setOAuthConsumerKey(CoffeeApplication.CONSUMER_KEY);
				configurationBuilder.setOAuthConsumerSecret(CoffeeApplication.CONSUMER_SECRET);

				AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecretString);

			    try {

				    TwitterFactory mTwitterFactory = new TwitterFactory(configurationBuilder.build());
				    Twitter twitter = mTwitterFactory.getInstance(accessToken);

				    StatusUpdate status = new StatusUpdate(message);

				    if (mFile != null) {

				        status.setMedia(mFile);
				    }

				    twitter.updateStatus(status);

				} catch (final TwitterException exception) {

					mTwitterException = exception;

					tweetError = true;

				} finally {

					resultMessage();
			    }

		} // tweetAction

	    public void resultMessage() {

			if (tweetError && mTwitterException != null) {

				intent.setAction("errorTweetNotSent");
				intent.putExtra("errorMessage", mTwitterException.toString());
				TwitterService.this.sendBroadcast(intent);

			} else {

				intent.setAction("tweetSent");
				TwitterService.this.sendBroadcast(intent);

			}

		} // resultMessage

} // TwitterService