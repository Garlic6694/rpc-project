package com.upc.garlic.utils.concurrent.threadpool;


import lombok.Data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置参数
 */
@Data
public class CustomThreadPoolConfig {
    /**
     * 线程池默认参数
     */
    private static final int DEFAULT_CORE_POOL_SIZE = 10;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 100;
    private static final int DEFAULT_KEEP_ALIVE_TIME = 1;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    private static final int DEFAULT_BLOCKING_QUEUE_CAPACITY = 200;

    /**
     * 自定义参数
     */
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    private TimeUnit timeUnit = DEFAULT_TIME_UNIT;
    private int blockingQueueCapacity = DEFAULT_BLOCKING_QUEUE_CAPACITY;

    /**
     * 有界队列
     */
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(blockingQueueCapacity);
}
