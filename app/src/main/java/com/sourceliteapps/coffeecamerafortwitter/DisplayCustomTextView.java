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

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DisplayCustomTextView {

	private ScheduledThreadPoolExecutor stpe;
	public static final int NOT_ROTATED = 0;
	public static final int ROTATED = 1;
	private String textToDisplay = "";
	private int rotationState = NOT_ROTATED;
	protected Context context;
	private Handler handler;
	private ViewGroup viewGroup;
	private View view;
	private int seconds;
	private Queue<CustomTextLayout> textQueue = new LinkedList();

	public DisplayCustomTextView(Context context, Handler handler, ViewGroup view) {

		this.context = context;
		this.handler = handler;
		this.viewGroup = view;

		stpe = new ScheduledThreadPoolExecutor(6);

	} // DisplayCustomTextView constructor

	public void setText(String text) {

		textToDisplay = text;

	}

	public void setRotationState(int rotationState) {

		this.rotationState = rotationState;

	}

	public void setDuration(int seconds) {

		this.seconds = seconds;

	}

	public synchronized void startDisplay() {

		if (textQueue.size() < 5) {

			CustomTextLayout newLayout = new CustomTextLayout(context);
			newLayout.init(textToDisplay, rotationState, viewGroup);
			stpe.schedule(new TextStopperAndCleaner(newLayout), seconds, TimeUnit.SECONDS); // timed remove
			textQueue.add(newLayout);

		}

	} // startDisplay

	public synchronized void clearAll() {

		textQueue.clear();

	} // clearAll

	public class TextStopperAndCleaner implements Runnable {

		CustomTextLayout customText;

		public TextStopperAndCleaner(CustomTextLayout customText) {

			this.customText = customText;

		} // TextStopperAndCleaner

		@Override
		public void run() {

			handler.post(new Runnable() {
				@Override
				public synchronized void run() {

						viewGroup.removeView(customText);

                        textQueue.remove(customText);
				}
			});

		} // run

	} // TextStopperAndCleaner Class

	public class CustomTextLayout extends LinearLayout {

		LayoutInflater mInflater;
		TextView textViewOne;
		FrameLayout frameLayoutOne;

		public CustomTextLayout(Context context) {
			super(context);

		}

		public CustomTextLayout(Context context, AttributeSet attrs) {
			super(context, attrs);

		}

		public CustomTextLayout(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);

		}

		public synchronized void init(String textToDisplay, int rotationState, ViewGroup viewGroup) {

			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if(viewGroup != null) {

				int screenState = getResources().getConfiguration().orientation;

				view = mInflater.inflate(R.layout.view_text, this, true);
				frameLayoutOne  = (FrameLayout) view.findViewById(R.id.frameLayout1);
				textViewOne = (TextView) view.findViewById(R.id.textView1);

				frameLayoutOne.getLayoutParams().width = (int) (0.8 * CoffeeActivity.rootLayoutHeight);
				frameLayoutOne.getLayoutParams().height = (int) (0.8 * CoffeeActivity.rootLayoutWidth);

				if (rotationState == ROTATED) {

					textViewOne.setRotation(270);

				}

				textViewOne.setText(textToDisplay);
				viewGroup.addView(this);
				bringToFront();

			} // if not null

		} // init

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			super.onLayout(changed, l, t, r, b);

		} // onLayout

	} // CustomTextLayout

} // DisplayCustomTextView