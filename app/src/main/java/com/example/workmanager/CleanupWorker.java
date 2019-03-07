package com.example.workmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Cleans up temporary files generated during blurring process
 */
public class CleanupWorker extends Worker {

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public CleanupWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private static final String TAG = CleanupWorker.class.getSimpleName();

    @NonNull
    @Override
    public Worker.Result doWork() {
        Context applicationContext = getApplicationContext();

        // Makes a notification when the work starts and slows down the work so that it's easier to
        // see each WorkRequest start, even on emulated devices
        WorkerUtils.makeStatusNotification("Cleaning up old temporary files",
                applicationContext);
        WorkerUtils.sleep();

        try {
            File outputDirectory = new File(applicationContext.getFilesDir(),
                    Constants.OUTPUT_PATH);
            if (outputDirectory.exists()) {
                File[] entries = outputDirectory.listFiles();
                if (entries != null && entries.length > 0) {
                    for (File entry : entries) {
                        String name = entry.getName();
                        if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            boolean deleted = entry.delete();
                            Log.i(TAG, String.format("Deleted %s - %s", name, deleted));
                        }
                    }
                }
            }
            return Worker.Result.success();
        } catch (Exception exception) {
            Log.e(TAG, "Error cleaning up", exception);
            return Result.failure();
        }
    }
}