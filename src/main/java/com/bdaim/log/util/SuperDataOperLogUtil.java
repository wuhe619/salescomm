package com.bdaim.log.util;

import com.bdaim.AppConfig;
import com.bdaim.log.entity.SuperDataOperLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 公海线索转交记录
 *
 * @author chengning@salescomm.net
 * @date 2019/7/1 11:26
 */
public class SuperDataOperLogUtil {
    private static SuperDataOperLogUtil instance = null;

    public synchronized static SuperDataOperLogUtil getInstance() {
        if (null == instance) {
            int queueSize = AppConfig.getOper_log_queue_size();
            int threadNum = AppConfig.getOper_log_thread_num();
            instance = new SuperDataOperLogUtil(queueSize, threadNum);
        }
        return instance;
    }

    BlockingQueue<Runnable> queue;
    private ThreadPoolExecutor executor = null;

    public static boolean isEnable() {
        return AppConfig.getIs_enable_oper_log();
    }

    private SuperDataOperLogUtil(int queueSize, int threadNum) {
        queue = new LinkedBlockingQueue(queueSize);
        executor = new ThreadPoolExecutor(threadNum, threadNum * 2, 60, TimeUnit.SECONDS, queue);
    }

    public void insertLog(SuperDataOperLog value) {
        //future no use
        try {
            executor.submit(new SuperDataOperLogThread(value));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void terminate() {
        try {
            executor.shutdownNow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            queue.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
