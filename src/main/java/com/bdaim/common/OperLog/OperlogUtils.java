package com.bdaim.common.OperLog;


import com.bdaim.AppConfig;
import com.bdaim.log.entity.OperLog;
import com.bdaim.log.entity.UserOperLog;
import com.bdaim.log.thread.UserOperLogRunnable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class OperlogUtils {
    private static OperlogUtils olutils = null;

    public synchronized static OperlogUtils getInstance() {
        if (null == olutils) {
            //read config from file
            int queueSize = AppConfig.getOper_log_queue_size();
            int threadNum = AppConfig.getOper_log_thread_num();
            int insertTimeoutMs = AppConfig.getOper_log_insert_timeout();
            olutils = new OperlogUtils(queueSize, threadNum, insertTimeoutMs);
        }
        return olutils;
    }

    BlockingQueue<Runnable> logqueue = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor executor = null;

    BlockingQueue<Runnable> userLogQueue;
    private ThreadPoolExecutor userLogExecutor;

    public static boolean isEnable() {
        return AppConfig.getIs_enable_oper_log();
    }

    private OperlogUtils(int queueSize, int threadNum, int insertTimeoutMs) {
        logqueue = new LinkedBlockingQueue(queueSize);
        executor = new ThreadPoolExecutor(threadNum, threadNum * 2, 60, TimeUnit.SECONDS, logqueue);

        userLogQueue = new LinkedBlockingQueue(queueSize);
        userLogExecutor = new ThreadPoolExecutor(threadNum, threadNum * 2, 60, TimeUnit.SECONDS, userLogQueue);
    }

    public void insertLog(OperLog value) {
        //future no use
        try {
            executor.submit(new OperlogRunnable(value));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 保存用户行为记录
     *
     * @param value
     */
    public void insertUserOperLog(UserOperLog value) throws Exception {
        userLogExecutor.submit(new UserOperLogRunnable(value));
    }

    public int getQueueSize() {
        return logqueue.size();
    }

    public synchronized void terminate() {
        try {
            executor.shutdownNow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            logqueue.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized static void terminateUtils() {
        olutils.terminate();
        olutils = null;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 999; i++) {
            OperLog e = new OperLog();

            OperlogUtils.getInstance().insertLog(e);
        }
        Thread.sleep(2000);
        OperlogUtils.terminateUtils();
    }

}
