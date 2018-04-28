package com.demos.java.basedemo.concurrent.lock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 * <p>
 * 自旋锁
 * <p>
 * 无法保证公平性, 总线流量压力大, 适用于临界区很小的场景
 */
public class SpinLock {

    private AtomicReference<Thread> owner = new AtomicReference<>();

    public void lock() {
        // 当前请求获取锁的线程
        Thread currentThread = Thread.currentThread();
        // 未获取成功则一直自旋
        while (!owner.compareAndSet(null, currentThread)) {
        }
    }

    public void unlock() {
        // 当前请求释放锁的线程
        Thread currentThread = Thread.currentThread();
        // 只有持有锁的线程才能释放锁
        owner.compareAndSet(currentThread, null);
    }

}
