package com.demo.framework.guava.limiter;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/5
 * <p>
 * 限流算法
 */
public class Limiter {

    /**
     * JUC Semaphore: 漏斗算法, 利用信号量工具控制消费速度
     */
    public static void semaphore() {
        Semaphore semaphore = new Semaphore(2);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    // 获取信号量
                    semaphore.acquire();
                    System.out.println("获取到信号量, ms=" + System.currentTimeMillis());
                    Thread.sleep(2000);
                } catch (Exception e) {
                } finally {
                    semaphore.release();
                }
            }).start();
        }
    }

    /**
     * Guava RateLimiter: 令牌桶算法
     */
    public static void rateLimiter() {
        // 'SmoothBursty'模式, 每秒放2个令牌, 可以存储令牌(最大存储1秒的量), 允许预消费, 但下一次消费要等待
        RateLimiter burstyLimiter = RateLimiter.create(5.0D);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        // 最多可存储(5.0个/s * 1s = 5.0个)令牌, 加上允许一次预消费, 因此前6次消费不需要等待
        System.out.printf("第1次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.printf("第2次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.printf("第3次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.printf("第4次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.printf("第5次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.printf("第6次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.printf("第7次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.printf("第8次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), burstyLimiter.acquire(1));
        System.out.println("--------------------");
        // 'SmoothWarmingUp'模型: 冷启动需要预热, 不能存储令牌, 允许预消费, 但下一次消费要等待, 且要经过预热时间才能达到平均速率(这里是2个每秒)
        RateLimiter warmingUpLimiter = RateLimiter.create(2.0D, 2, TimeUnit.SECONDS);
        System.out.printf("第1次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第2次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第3次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第4次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第5次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        System.out.printf("第1次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第2次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第3次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第4次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
        System.out.printf("第5次消费1个令牌, 时间=%s, 需等待%f秒\n", System.currentTimeMillis(), warmingUpLimiter.acquire(1));
    }

    public static void main(String[] args) {
        semaphore();
        rateLimiter();
    }

}
