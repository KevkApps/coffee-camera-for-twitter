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

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TakePicture implements Runnable {
		
	private OnPictureTakenListener mOnPictureTakenListener;
	private Bitmap mBitmap;
	private Bitmap adjustedBitmap;
	private int bitmapWidth;
	private int bitmapHeight;

	@Override
	public void run() {
        
        String imageName = "coffee.jpg";

		try {

			File fileDirectory = new File(CoffeeApplication.imageStoragePath);

			if(!fileDirectory.exists()){

				fileDirectory.mkdirs();
			}

			File fileOut = new File(fileDirectory, imageName);

			if (!fileOut.exists()) {

				fileOut.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(fileOut);

			Matrix matrix = new Matrix();

			matrix.postRotate(90);

			int adjustedWidth = bitmapHeight;
			int widthOffset = (bitmapWidth - bitmapHeight) / 2;
			int adjustedHeight = bitmapHeight;

			adjustedBitmap = Bitmap.createBitmap(mBitmap, widthOffset, 0, adjustedWidth, adjustedHeight, matrix, false);

			if(adjustedBitmap.getWidth() > 1200 || adjustedBitmap.getWidth() > 1200) {

				adjustedBitmap = Bitmap.createScaledBitmap(adjustedBitmap, 1200, 1200, false);
			}

			adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);

			fos.flush();
			fos.close();

        } catch (FileNotFoundException e) {

        } catch (Exception e) {

        } finally {

		    adjustedBitmap.recycle();
		    adjustedBitmap = null;
			mBitmap.recycle();
			mBitmap = null;
		}

        mOnPictureTakenListener.onPictureTaken();

	} // run
	 
	public void setOnPictureTakenListener(OnPictureTakenListener listener) {
	
		mOnPictureTakenListener = listener;
		
	}

	public interface OnPictureTakenListener {

	     public abstract void onPictureTaken();
	
   }

	public void setBitamp(Bitmap bmp) {

		mBitmap = bmp;

		bitmapWidth = bmp.getWidth();
		bitmapHeight = bmp.getHeight();
		
	}
	
} // TakePicture