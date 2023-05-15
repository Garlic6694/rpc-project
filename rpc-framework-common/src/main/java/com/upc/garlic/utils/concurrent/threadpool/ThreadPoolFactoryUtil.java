package com.upc.garlic.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建线程池
 */
@Slf4j
public final class ThreadPoolFactoryUtil {
    /**
     * 通过key区分不同的线程池
     * key threadPoolName
     * value threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {
    }

    public static ExecutorService createCusTomTheadPoolIfAbsent(String threadPoolName) {
        CustomThreadPoolConfig config = new CustomThreadPoolConfig();
        return createCusTomTheadPoolIfAbsent(threadPoolName, config, false);
    }

    public static ExecutorService createCusTomTheadPoolIfAbsent(String threadPoolName, CustomThreadPoolConfig config) {
        return createCusTomTheadPoolIfAbsent(threadPoolName, config, false);
    }

    public static ExecutorService createCusTomTheadPoolIfAbsent(String threadPoolName, CustomThreadPoolConfig config, Boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadPoolName, k -> createThreadPool(config, threadPoolName, daemon));
        if (threadPool.isShutdown() | threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadPoolName);
            threadPool = createThreadPool(config, threadPoolName, daemon);
            THREAD_POOLS.put(threadPoolName, threadPool);
        }
        return threadPool;
    }

    private static ExecutorService createThreadPool(CustomThreadPoolConfig config, String threadPoolName, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadPoolName, daemon);
        return new ThreadPoolExecutor(config.getCorePoolSize(),
                config.getMaximumPoolSize(),
                config.getKeepAliveTime(),
                config.getTimeUnit(),
                config.getWorkQueue(),
                threadFactory
        );
    }

    public static ThreadFactory createThreadFactory(String threadPoolName, Boolean daemon) {
        if (threadPoolName != null) {
            return new ThreadFactoryBuilder()
                    .setNameFormat(threadPoolName + "-%d")
                    .setDaemon(daemon)
                    .build();
        }
        return Executors.defaultThreadFactory();
    }

    public static void shutDownAllThreadPool() {
        log.info("shutdown all thread pool");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            boolean terminated = false;
            try {
                terminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
            }
            if (!terminated) {
                executorService.shutdownNow();
            }
        });
    }

    public static void printTheadPoolStatus(ThreadPoolExecutor threadPool){
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }

}
