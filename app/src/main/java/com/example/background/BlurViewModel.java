/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.example.background;

import static com.example.background.Constants.KEY_IMAGE_URI;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanUpWorker;
import com.example.background.workers.SaveImageToFileWorker;

public class BlurViewModel extends ViewModel {

    private WorkManager mWorkManager;

    private Uri mImageUri;

    // Constructor
    public BlurViewModel(@NonNull Application application) {
        super();
        mWorkManager = WorkManager.getInstance(application);

        mImageUri = getImageUri(application.getApplicationContext());
    }

    /**
     * Creates the input data bundle which includes the Uri to operate on
     *
     * @return Data which contains the Image Uri as a String
     */
    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }


    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     *
     * @param blurLevel The amount to blur the image
     */
/*  Create a chain of
    - a CleanupWorker WorkRequest,
    - a BlurImage WorkRequest
    - and a SaveImageToFile WorkRequest in applyBlur.
    Pass input into the BlurImage WorkRequest.
    */
    void applyBlur(int blurLevel) {

//      Add WorkRequest to Cleanup temporary images
        WorkContinuation continuation = mWorkManager.beginWith(OneTimeWorkRequest.from(CleanUpWorker.class));

//      Add WorkRequest to blur the image
        OneTimeWorkRequest blurRequest = new OneTimeWorkRequest.Builder(BlurWorker.class)
                .setInputData(createInputDataForUri())
                .build();
        continuation = continuation.then(blurRequest);

//      Add WorkRequest to save the image to the filesystem
        OneTimeWorkRequest save = new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                .build();
        continuation = continuation.then(save);

//      Actually start the work
        continuation.enqueue();

//        OneTimeWorkRequest blurRequest =
//                new OneTimeWorkRequest.Builder(BlurWorker.class)
//                        .setInputData(createInputDataForUri())
//                        .build();
//
//        mWorkManager.enqueue(blurRequest);
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    private Uri getImageUri(Context context) {
        Resources resources = context.getResources();

        Uri imageUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
                .build();

        return imageUri;
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }


}