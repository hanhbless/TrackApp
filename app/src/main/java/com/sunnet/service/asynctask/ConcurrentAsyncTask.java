package com.sunnet.service.asynctask;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

public abstract class ConcurrentAsyncTask<Params, Progress, Result> extends
        AsyncTask<Params, Progress, Result> {

    private int index;

    protected ConcurrentAsyncTask() {
    }

    protected ConcurrentAsyncTask(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void executeConcurrently(Params... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            this.execute(params);
        }
    }

}
