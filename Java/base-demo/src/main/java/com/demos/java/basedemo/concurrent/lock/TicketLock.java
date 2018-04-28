package com.demos.java.basedemo.concurrent.lock;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 * <p>
 * TicketLock
 * <p>
 * 解决了公平性问题, 但所有线程读写同一个变量serviceNumber, 造成总线流量压力大
 */
public class TicketLock {

    // 当前服务号
    private AtomicInteger serviceNumber = new AtomicInteger();
    // 排队号生成器
    private AtomicInteger ticketGenerator = new AtomicInteger();

    public int lock() {
        // 首先获取一个排队号
        int ticketNumber = ticketGenerator.getAndIncrement();
        // 如果当前服务号不是自己则自旋等待
        while (serviceNumber.get() != ticketNumber) {
        }
        return ticketNumber;
    }

    public void unlock(int ticketNumber) {
        int nextNumber = ticketNumber + 1;
        // 只有当前持有锁的线程才能释放锁
        serviceNumber.compareAndSet(ticketNumber, nextNumber);
    }

}
