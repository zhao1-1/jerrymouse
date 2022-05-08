package cn.zhaobin.jerrymouse.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtils {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            20, 100, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10));

    public static void run(Runnable r) {threadPoolExecutor.execute(r);}

}
