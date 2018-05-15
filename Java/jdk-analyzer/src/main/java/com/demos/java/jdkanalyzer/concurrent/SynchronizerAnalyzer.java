package com.demos.java.jdkanalyzer.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/15
 */
public class SynchronizerAnalyzer {

    /*------------------------------ Semaphore ------------------------------*/
    // 内部`Sync`继承`AbstractQueuedSynchronizer`: Shared模式: 直接操作`state`(compareAndSetState)
    // 公平版本: 先判断`同步队列`中是否有节点正在等待
    // 非公平版本: 直接`compareAndSetState`设置`state`

    public class Semaphore implements java.io.Serializable {
        private final Sync sync;

        abstract class Sync extends AbstractQueuedSynchronizer {
            private static final long serialVersionUID = 1192457210091910933L;

            Sync(int permits) {
                setState(permits);
            }

            final int getPermits() {
                return getState();
            }

            final int nonfairTryAcquireShared(int acquires) {
                for (;;) {
                    int available = getState();
                    int remaining = available - acquires;
                    if (remaining < 0 ||
                            compareAndSetState(available, remaining))
                        return remaining;
                }
            }

            protected final boolean tryReleaseShared(int releases) {
                for (;;) {
                    int current = getState();
                    int next = current + releases;
                    if (next < current) // overflow
                        throw new Error("Maximum permit count exceeded");
                    if (compareAndSetState(current, next)) // 每次成功释放后都会唤醒`同步队列`中的节点
                        return true;
                }
            }
        }

        /**
         * 非公平版本: 直接compareAndSetState
         */
        final class NonfairSync extends Sync {
            NonfairSync(int permits) {
                super(permits);
            }
            protected int tryAcquireShared(int acquires) {
                return nonfairTryAcquireShared(acquires);
            }
        }

        /**
         * 公平判断: 先判断`同步队列`中是否有节点正在等待
         */
        final class FairSync extends Sync {
            FairSync(int permits) {
                super(permits);
            }
            protected int tryAcquireShared(int acquires) {
                for (;;) {
                    if (hasQueuedPredecessors()) // 如果`同步队列`中有节点正在等待, 则获取失败
                        return -1;
                    int available = getState();
                    int remaining = available - acquires;
                    if (remaining < 0 ||
                            compareAndSetState(available, remaining))
                        return remaining;
                }
            }
        }

        /**
         * 获取许可
         * @param permits
         * @throws InterruptedException
         */
        public void acquire(int permits) throws InterruptedException {
            if (permits < 0) throw new IllegalArgumentException();
            sync.acquireSharedInterruptibly(permits);
        }

        /**
         * 释放许可
         * @param permits
         */
        public void release(int permits) {
            if (permits < 0) throw new IllegalArgumentException();
            sync.releaseShared(permits);
        }

    }

    /*------------------------------ CountDownLatch ------------------------------*/
    // 内部`Sync`继承`AbstractQueuedSynchronizer`: Shared模式
    // await(): state == 0 ?
    // countDown(): state--, 若 state == 0, 则唤醒所有等待节点

    public class CountDownLatch {

        private static final class Sync extends AbstractQueuedSynchronizer {

            Sync(int count) {
                setState(count);
            }

            int getCount() {
                return getState();
            }

            protected int tryAcquireShared(int acquires) {
                return (getState() == 0) ? 1 : -1; // `state`为0时则获取锁成功
            }

            protected boolean tryReleaseShared(int releases) {
                for (;;) {
                    int c = getState();
                    if (c == 0)
                        return false;
                    int nextc = c-1;
                    if (compareAndSetState(c, nextc)) // `state`每次减1, 为0时释放
                        return nextc == 0;
                }
            }
        }

        private final Sync sync;

        /**
         * 构造器: 传入`闭锁`的数量
         * @param count
         */
        public CountDownLatch(int count) {
            if (count < 0) throw new IllegalArgumentException("count < 0");
            this.sync = new Sync(count);
        }

        /**
         * 尝试获取一个状态
         * @throws InterruptedException
         */
        public void await() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        /**
         * `state`减1
         */
        public void countDown() {
            sync.releaseShared(1);
        }

        public long getCount() {
            return sync.getCount();
        }

    }

    /*------------------------------ CyclicBarrier ------------------------------*/
    // 内部使用`ReentrantLock`和`Condition`进行控制, 不直接继承`AbstractQueuedSynchronizer`

    public class CyclicBarrier {
        /**
         * 每一个使用中的`屏障`代表一个`代`
         */
        private class Generation {
            boolean broken = false;
        }

        // 使用`ReentrantLock`进行同步控制
        private final ReentrantLock lock = new ReentrantLock();
        // 先到达的线程在`Condition`上等待
        private final Condition trip = lock.newCondition();
        // 固定数量
        private final int parties;
        // 屏障通过时执行的任务
        private final Runnable barrierCommand;
        // 当前的代
        private Generation generation = new Generation();

        // 剩余数量, 每次开启新的屏障时赋值为`parties`
        private int count;

        /**
         * 开启新的代
         */
        private void nextGeneration() {
            // 唤醒已经到达的线程
            trip.signalAll();
            // 开启新的代
            count = parties;
            generation = new Generation();
        }

        /**
         * 将当前代置为失效
         */
        private void breakBarrier() {
            generation.broken = true;
            count = parties;
            trip.signalAll();
        }

        /**
         * 核心逻辑
         */
        private int dowait(boolean timed, long nanos) throws InterruptedException, BrokenBarrierException, TimeoutException {
            final ReentrantLock lock = this.lock;
            lock.lock(); // 先加锁
            try {
                final Generation g = generation;

                if (g.broken) // 当前`代`已经失效了, 则抛出异常
                    throw new BrokenBarrierException();

                if (Thread.interrupted()) { // 如果当前线程被中断了, 则将当前`代`置为失效
                    breakBarrier();
                    throw new InterruptedException();
                }

                int index = --count;
                if (index == 0) {  // 屏障已经到达, 唤醒所有等待的线程, 并开启下一代
                    boolean ranAction = false;
                    try {
                        final Runnable command = barrierCommand;
                        if (command != null) // `屏障任务`不为空则执行屏障任务
                            command.run();
                        ranAction = true;
                        nextGeneration(); // 开启下一代
                        return 0;
                    } finally {
                        if (!ranAction)
                            breakBarrier();
                    }
                }

                // loop until tripped, broken, interrupted, or timed out
                // 主循环: 线程在这里直到`屏障到达`、`屏障失效`、`被中断`或者`等待超时`
                for (;;) {
                    try {
                        if (!timed)
                            trip.await(); // 阻塞: 所有先到达的线程在这里阻塞
                        else if (nanos > 0L) // 如果设置了超时则超时等待, 超时后当前`代`会失效
                            nanos = trip.awaitNanos(nanos);
                    } catch (InterruptedException ie) {
                        if (g == generation && ! g.broken) { // 自己属于当前`代`, 且当前`代`已失效, 抛出异常
                            breakBarrier();
                            throw ie;
                        } else {
                            // We're about to finish waiting even if we had not
                            // been interrupted, so this interrupt is deemed to
                            // "belong" to subsequent execution.
                            Thread.currentThread().interrupt();
                        }
                    }

                    if (g.broken) // 醒来之后发现自己所属的`代`已经失效了, 则抛出异常
                        throw new BrokenBarrierException();

                    if (g != generation) // 醒来之后发现自己所属的`代`已经不是当前`代`了, 返回阻塞之前剩余的count
                        return index;

                    if (timed && nanos <= 0L) { // 已经超时了则将当前`代`置为失效
                        breakBarrier();
                        throw new TimeoutException();
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public CyclicBarrier(int parties, Runnable barrierAction) {
            if (parties <= 0) throw new IllegalArgumentException();
            this.parties = parties;
            this.count = parties;
            this.barrierCommand = barrierAction;
        }

        public CyclicBarrier(int parties) {
            this(parties, null);
        }

        public int getParties() {
            return parties;
        }

        /**
         * 屏障等待: 核心方法
         */
        public int await() throws InterruptedException, BrokenBarrierException {
            try {
                return dowait(false, 0L);
            } catch (TimeoutException toe) {
                throw new Error(toe); // cannot happen
            }
        }

        /**
         * 当前`代`是否已失效
         * @return
         */
        public boolean isBroken() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return generation.broken;
            } finally {
                lock.unlock();
            }
        }

        /**
         * 重置屏障: 将当前`代`置为失效, 并开启新的`代`
         */
        public void reset() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                breakBarrier();   // break the current generation
                nextGeneration(); // start a new generation
            } finally {
                lock.unlock();
            }
        }

    }

}
