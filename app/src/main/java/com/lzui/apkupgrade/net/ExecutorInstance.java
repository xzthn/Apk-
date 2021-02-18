package com.lzui.apkupgrade.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ExecutorInstance {

    // 线程池并发线程数,服务器请求减少并发数，一个一个请求
    private static final int POOL_SIZE = 1;
    // 线程池执行器
    private static ExecutorService executor = null;

    /**
     * 获取线程池执行器
     */
    public static ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(POOL_SIZE, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    // 设置线程的优先级别，让线程先后顺序执行（级别越高，抢到cpu执行的时间越多）
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                }
            });
        }
        return executor;
    }

}
