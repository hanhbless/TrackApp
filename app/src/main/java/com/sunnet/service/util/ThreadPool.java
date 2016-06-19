package com.sunnet.service.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private static int MAX_POOL_SIZE;
    private static final int KEEP_ALIVE = 120;

    private static ThreadPool mInstance;

    private ThreadPoolExecutor mThreadPoolDatabaseExec;
    private ThreadPoolExecutor mThreadPoolUploadExec;

    BlockingQueue<Runnable> databaseQueue = new LinkedBlockingQueue<>();
    BlockingQueue<Runnable> uploadQueue = new LinkedBlockingQueue<>();

    public static synchronized void doDatabase(Runnable runnable) {
        if (mInstance == null) {
            mInstance = new ThreadPool();
        }
        mInstance.mThreadPoolDatabaseExec.execute(runnable);
    }

    public static synchronized void doUpload(Runnable runnable) {
        if (mInstance == null) {
            mInstance = new ThreadPool();
        }
        mInstance.mThreadPoolUploadExec.execute(runnable);
    }


    private ThreadPool() {
        int coreNum = 100/*Runtime.getRuntime().availableProcessors()*/;
        MAX_POOL_SIZE = 128/*coreNum * 2*/;
        mThreadPoolDatabaseExec = new ThreadPoolExecutor(
                coreNum,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                databaseQueue);

        mThreadPoolUploadExec = new ThreadPoolExecutor(
                coreNum,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                uploadQueue);
    }

    public static void finish() {
        mInstance.mThreadPoolDatabaseExec.shutdown();
        mInstance.mThreadPoolUploadExec.shutdown();
    }
}
