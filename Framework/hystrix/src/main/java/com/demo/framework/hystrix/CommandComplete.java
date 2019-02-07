package com.demo.framework.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategyDefault;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/17
 */
public class CommandComplete extends HystrixCommand<String> {

    private Long id;

    public CommandComplete(Long id) {
        super(setter());
        this.id = id;
    }

    private static Setter setter() {
        // 服务分组
        HystrixCommandGroupKey groupKey = HystrixCommandGroupKey.Factory.asKey("complete-group");
        // 服务标识
        HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey("complete-command");
        // 线程池
        HystrixThreadPoolKey threadPoolKey = HystrixThreadPoolKey.Factory.asKey("complete-pool");
        // 线程池配置
        HystrixThreadPoolProperties.Setter threadPoolProperties = HystrixThreadPoolProperties.Setter()
                .withCoreSize(10) // 核心线程池大小
                .withKeepAliveTimeMinutes(5) // 线程最大空闲时间
                .withMaxQueueSize(100) // 线程池队列最大大小
                .withQueueSizeRejectionThreshold(100); // 当前队列大小: 实际队列大小由该属性配置
        // 命令属性配置
        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter()
                // 开启熔断机制: 默认为true
                .withCircuitBreakerEnabled(true)
                // 是否强制关闭熔断开关: 默认为false
                .withCircuitBreakerForceClosed(false)
                // 是否强制打开熔断开关: 默认为false
                .withCircuitBreakerForceOpen(false)
                // 在一个采用时间窗口内, 若失败率超过该配置, 则自动打开熔断开关实现降级处理, 即"快速失败"
                .withCircuitBreakerErrorThresholdPercentage(10)
                // 熔断后的重试时间窗口, 在该窗口内只允许一次重试
                .withCircuitBreakerRequestVolumeThreshold(20)
                // Hystrix采用"舱壁模式"实现线程池的隔离, 它会为每个依赖服务创建一个独立的线程池
                // 隔离策略: Thread
                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD) // 隔离级别为THREAD
                // 开启降级处理
                .withFallbackEnabled(true)
                // fallback方法的并发请求的信号量, 如果超过则不会再尝试调用getFallback()方法, 而是快速失败
                .withFallbackIsolationSemaphoreMaxConcurrentRequests(100)
                // 隔离策略为THREAD时, 当执行线程超时时, 是否调用`Future.cancel()`进行中断处理
                .withExecutionIsolationThreadInterruptOnFutureCancel(true)
                // 隔离策略为THREAD时, 当执行线程超时时, 是否进行中断处理
                .withExecutionIsolationThreadInterruptOnTimeout(true)
                // 是否启用执行超时机制
                .withExecutionTimeoutEnabled(true)
                // 执行超时时间
                .withExecutionTimeoutInMilliseconds(1000)
                // 采样统计滚转时间窗口: 默认为10s
                .withMetricsRollingStatisticalWindowInMilliseconds(10000)
                // 采样统计滚转时间窗口内的桶数, 如: 10s, 10个桶, 则每秒一个桶统计
                .withMetricsRollingStatisticalWindowBuckets(10)
                // 采样统计间隔: 如: 10s, 间隔1s, 则桶数量为10个, 该配置不与`采样统计滚转时间窗口内的桶数`同时配置
                // .withMetricsHealthSnapshotIntervalInMilliseconds(1000)
                ;
        // 命令配置
        return HystrixCommand.Setter.withGroupKey(groupKey)
                .andCommandKey(commandKey)
                .andThreadPoolKey(threadPoolKey)
                .andThreadPoolPropertiesDefaults(threadPoolProperties)
                .andCommandPropertiesDefaults(commandProperties);

    }

    @Override
    protected String run() throws Exception {
        return "time = " + System.currentTimeMillis() + ", id = " + id;
    }

    /**
     * 重载该方法返回一个非`null`值即可开启缓存: 必须先初始化HystrixRequestContext
     *
     * @return
     */
    @Override
    protected String getCacheKey() {
        return "" + id;
    }

    /**
     * 提供静态方法: 根据ID删除缓存
     *
     * @param id
     */
    public static void clearCache(Long id) {
        HystrixRequestCache.getInstance(HystrixCommandKey.Factory.asKey("complete-command"),
                HystrixConcurrencyStrategyDefault.getInstance()).clear(String.valueOf(id));
    }

    public static void main(String[] args) {
        // 先初始化HystrixRequestContext
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            // 第一次执行
            System.out.println(new CommandComplete(10L).execute());
            // 第二次执行: 由于`getCacheKey()`, 直接从cache返回
            System.out.println(new CommandComplete(10L).execute());
            // 清除缓存
            clearCache(10L);
            // 第三次执行: 由于已清理缓存, 这次会得到不同的值
            System.out.println(new CommandComplete(10L).execute());
        } finally {
            context.shutdown();
        }
    }

}
