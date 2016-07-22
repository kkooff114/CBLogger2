package cn.loujiwei.android.cblogger.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LJW on 15/10/6.
 */
public class ThreadManager {

    private static volatile ExecutorService executorService = null; // uploadservice 使用

    // 线程池核心线程数
    private static int CORE_POOL_SIZE = 5;

    // 线程池最大线程数
    private static int MAX_POOL_SIZE = 100;

    // 额外线程空状态生存时间
    private static int KEEP_ALIVE_TIME = 10000;

    // 阻塞队列。当核心线程都被占用，且阻塞队列已满的情况下，才会开启额外线程。
    private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(
            10);
    private static BlockingQueue<Runnable> preLoadWorkQueue = new ArrayBlockingQueue<Runnable>(
            10);

    // 线程工厂
    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "myThreadPool thread:"
                    + integer.getAndIncrement());
        }
    };
    private static ThreadFactory preLoadThreadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "myThreadPool thread:"
                    + integer.getAndIncrement());
        }
    };

    // 线程池
    private static ThreadPoolExecutor threadPool;
    private static ThreadPoolExecutor preLoadThreadPool;

    static {
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, threadFactory);
        preLoadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS, preLoadWorkQueue, preLoadThreadFactory);
    }

    /**
     * 从线程池中抽取线程，执行指定的Runnable对象
     *
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    /**
     * 预加载线程池执行
     *
     * @param runnable
     */
    public static void executePreLoad(Runnable runnable) {
        preLoadThreadPool.execute(runnable);
    }


    /**
     * 在主线程中执行回调函数
     *
     * @param runnable
     */
    public static void mainThreadExecutor(Runnable runnable) {


        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(runnable);
    }


    public static void prepare() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }


    /**
     * uploadservice自动提价的线程池
     *
     * @return
     */
    public static ExecutorService getFixedThreadPool() {
        if (executorService == null) {
            synchronized (ExecutorService.class) {
                if (executorService == null) {
                    executorService = Executors.newFixedThreadPool(1);
                }
            }
        }
        return executorService;
    }

}
