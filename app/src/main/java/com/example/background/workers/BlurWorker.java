package com.example.background.workers;

import static com.example.background.Constants.KEY_IMAGE_URI;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;
import com.example.background.R;

public class BlurWorker extends Worker {

    // Constructor
    public BlurWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private static final String TAG = BlurWorker.class.getSimpleName();

    //Overriding the doWork() method to use WorkerUtil's makeStatusNotification
    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();

        //A variable to get the input: the URI passed from the Data object
        String resourceUri = getInputData().getString(KEY_IMAGE_URI);

        //Creating a bitmap utilizing a try/catch statement
        try {

            if(TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input URI");
                throw new IllegalArgumentException("Invalid input URI");
            }

            ContentResolver resolver = applicationContext.getContentResolver();
//            Create a bitmap
            Bitmap picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));

            // Blur the bitmap
            Bitmap output = WorkerUtils.blurBitmap(picture, applicationContext);

            // Write bitmap to a temp file
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output);

            WorkerUtils.makeStatusNotification("Output is "
                    + outputUri.toString(), applicationContext);

            // Output temporary URI as a Data object, using the same (Constant) Key
            Data outputData = new Data.Builder()
                    .putString(KEY_IMAGE_URI, outputUri.toString())
                    .build();
            // If there were no errors, return SUCCESS
            return Result.success(outputData);

        } catch (Throwable throwable) {

            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }
}
