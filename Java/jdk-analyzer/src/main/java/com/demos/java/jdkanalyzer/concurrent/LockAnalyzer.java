package com.demos.java.jdkanalyzer.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/14
 */
public class LockAnalyzer {

    /*------------------------------ ReentrantLock ------------------------------*/
    // 可重入锁:
    // 1.提供`公平`和`非公平`实现
    // 2.可重入
    // 2.可中断, 可超时
    // 4.支持多个`条件队列`

    // 非公平实现: 主要是: `lock()`和`tryAcquire()`中均先尝试以CAS方式直接设置`state`来获取锁
    // 公平实现: `lock()`中不尝试设置state而是直接调用`acquire()`, 在`tryAcquire()`先判断`同步队列`中是否有节点等待, 如果有则返回false加入`同步队列`排队
    // AQS: 实际上也是`非公平锁`, 因为在`acquire()`中获取锁时, 先调用`tryAcquire()`尝试获取锁, 而不是判断`同步队列`中是否有节点等待
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        abstract void lock();

        /**
         * 非公平的`tryAcquire`
         * @param acquires
         * @return
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    // 若当前state为0, 则直接以CAS方式设置state, 不管此时`同步队列`中是否有节点正在等待
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) { // 锁重入
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }


        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        /**
         * 锁的占用者
         * @return
         */
        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        /**
         * 当前持有的状态值
         * @return
         */
        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        /**
         * 锁是否被占用
         * @return
         */
        final boolean isLocked() {
            return getState() != 0;
        }

    }

    /**
     * 非公平锁, AQS获取锁也是非公平的, 体现在:
     * `acquire()/acquireShared()`方法获取锁时, 先调用`tryAcquire()/tryAcquireShared`尝试获取锁, 而不询问此时`同步队列`中是否有节点正在等待
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * 非公平实现
         */
        final void lock() {
            if (compareAndSetState(0, 1)) // 非公平实现: 直接尝试设置state, 而不管同步队列中是否有节点正在等待
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * 公平锁
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        /**
         * 公平实现
         * 如果同步队列中有节点正在等待, 则此次获取锁失败, 必须加入同步队列排队等待
         * @param acquires
         * @return
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) { // 当前没有线程获取锁
                if (!hasQueuedPredecessors() // 同步队列中没有等待的节点
                        && compareAndSetState(0, acquires)) { // 以CAS方式设置同步状态成功: 成功获取到锁
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) { // 当前锁支持者是自己, 重入
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /*------------------------------ ReentrantReadWriteLock ------------------------------*/
    // 可重入读写锁:
    // 1.支持`公平`和`非公平`的方式获取锁
    // 2.可重入
    // 3.允许`写锁`降级为`读锁`: 1)获取`写锁`, 2)获取`读锁`, 3)释放`写锁`; 但不允许`读锁`升级为`写锁`
    // 4.`写锁`和`读锁`都支持中断
    // 5.`写锁`支持`Condition`

    // 核心逻辑:
    // 1.获取`读锁`时, 只有`写锁`被占用且不是当前线程时, 才会获取失败, 即: 允许占有`写锁`的线程获取`读锁`, 这就提供了`锁降级`的机制
    // 2.获取`写锁`时, 只要`读锁`被占用, 或者`写锁`被占用且不是自己, 则会获取失败, 即: 不允许占有`读锁`的线程获取`写锁`, 禁止了`锁升级`机制
    // 3.公平方式获取锁: `tryLock`方法中`读锁`和`写锁`都是只要`同步队列`有节点等待`就要阻塞
    // 4.非公平方式获取锁: `tryLock`方法中`写锁`不阻塞, `读锁`的判断是: 如果`同步队列`第一个等待锁的节点是`写锁`, 则应该阻塞, 否则不阻塞
    // 关于`应该阻塞`的问题: 已经占有`读锁`的线程再来获取`读锁`, 如果判断结果是应该`阻塞`, 那么也应该以自旋的方式反复尝试, 这是为了支持`读锁`的`可重入`
    // 而且`读锁`的占用时间通常较短, 因此使用自旋可以提高效率

    class ReentrantReadWriteLock
            implements ReadWriteLock, java.io.Serializable {
        private static final long serialVersionUID = -6992448646407690164L;
        // 读锁实现: 使用`state`同步状态的高`16`位, Shared mode
        private final ReentrantReadWriteLock.ReadLock readerLock;
        // 写锁实现: 使用`state`同步状态的低`16`位, Exclusive mode
        private final ReentrantReadWriteLock.WriteLock writerLock;
        // Sync实现了读写锁需要用到的方法, `ReadLock`和`WriteLock`中都使用该成员变量
        final Sync sync;

        /**
         * 默认为非公平实现`NonfairSync`
         */
        public ReentrantReadWriteLock() {
            this(false);
        }

        /**
         * 构造器
         * @param fair
         */
        public ReentrantReadWriteLock(boolean fair) {
            sync = fair ? new FairSync() : new NonfairSync();
            readerLock = new ReadLock(this);
            writerLock = new WriteLock(this);
        }

        public ReentrantReadWriteLock.WriteLock writeLock() { return writerLock; }
        public ReentrantReadWriteLock.ReadLock  readLock()  { return readerLock; }

        /**
         * 同步器
         */
        abstract static class Sync extends AbstractQueuedSynchronizer {
            private static final long serialVersionUID = 6317671515068378041L;

            // `读锁`使用`state`的高16位
            static final int SHARED_SHIFT   = 16;
            // 释放读锁时每次减去`2的16次方`
            static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
            // 最大获取数量
            static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
            // `写锁`使用`state`的低16位, 使用掩码快速计算
            static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

            /** `读锁`获取数量: 绝对右移`16`位 */
            static int sharedCount(int c)    { return c >>> SHARED_SHIFT; }
            /** `写锁`重入次数: 对低`16`位取余 */
            static int exclusiveCount(int c) { return c & EXCLUSIVE_MASK; }

            /**
             * `读锁`持有数量
             */
            static final class HoldCounter {
                int count = 0;
                // Use id, not reference, to avoid garbage retention
                final long tid = getThreadId(Thread.currentThread());
            }

            /**
             * 记录`读锁`持有数量: 继承`ThreadLocal`
             */
            static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
                public HoldCounter initialValue() {
                    return new HoldCounter();
                }
            }

            // 线程调用`releaseShared`时判断线程持有`读锁`数量的步骤:
            // 1.当前线程是否是`firstReader`, 如果是, 则数量为`firstReaderHoldCount`, 否则进入步骤2
            // 2.当前线程的线程ID是否与`cachedHoldCounter`一致, 如果一致则数量为`cachedHoldCounter.count`, 否则进入步骤3
            // 3.从容器`readHolds`中(继承`ThreadLocal`, 效率不高)取出`HoldCounter`, 得到其`count`

            // 成员变量`firstReader`, `firstReaderHoldCount`, `cachedHoldCounter`的存在, 主要是为了快速判断`count`值

            // 存放当前线程读锁持有数量的容器
            private transient ThreadLocalHoldCounter readHolds;

            // 最后一个成功获取`读锁`的线程所持有的数量, 基于: 通常下一个`释放`锁的线程就是最后一个`获取`锁的线程
            private transient HoldCounter cachedHoldCounter;

            // 第一个获取`读锁`的线程
            private transient Thread firstReader = null;
            // 第一个获取`读锁`的线程持有的数量
            private transient int firstReaderHoldCount;

            Sync() {
                readHolds = new ThreadLocalHoldCounter();
                setState(getState()); // ensures visibility of readHolds
            }

            /**
             * 当前正在获取`读锁`的线程是否应该被阻塞
             * @return
             */
            abstract boolean readerShouldBlock();

            /**
             * 当前正在获取`写锁`的线程是否应该被阻塞
             * @return
             */
            abstract boolean writerShouldBlock();

            protected final boolean tryRelease(int releases) {
                if (!isHeldExclusively())
                    throw new IllegalMonitorStateException();
                int nextc = getState() - releases; // `写锁`使用低`16`位, 可以直接做减法
                boolean free = exclusiveCount(nextc) == 0;
                if (free)
                    setExclusiveOwnerThread(null);
                setState(nextc); // 设置`nextc`而不是`0`是因为`写锁`可以降级, 此时`state`高`16`位不为0
                return free;
            }

            protected final boolean tryAcquire(int acquires) {
                Thread current = Thread.currentThread();
                int c = getState();
                int w = exclusiveCount(c);
                if (c != 0) { // 写锁占用不为0
                    // 1. if c != 0 and w == 0 then shared count != 0: 此时`读锁`不为`0`, 不允许获取`写锁`, 即: 不允许锁升级
                    // 2. current != getExclusiveOwnerThread(): 当前线程不是`写锁`占用者, 不允许获取`写锁`
                    if (w == 0 || current != getExclusiveOwnerThread())
                        return false;
                    if (w + exclusiveCount(acquires) > MAX_COUNT) // 锁溢出
                        throw new Error("Maximum lock count exceeded");
                    // Reentrant acquire
                    setState(c + acquires); // 设置`state`
                    return true;
                }
                if (writerShouldBlock() || !compareAndSetState(c, c + acquires)) // 如果获取写锁需要被阻塞则返回`false`
                    return false;
                setExclusiveOwnerThread(current);
                return true;
            }

            protected final boolean tryReleaseShared(int unused) {
                Thread current = Thread.currentThread();
                if (firstReader == current) { // 快速判断1: 当前线程是否是`firstReader`
                    // assert firstReaderHoldCount > 0;
                    if (firstReaderHoldCount == 1)
                        firstReader = null;
                    else
                        firstReaderHoldCount--; // 由于`firstReaderHoldCount`只会被一个线程操作, 不需要用CAS方式更新
                } else { // 快速判断2: 当前线程是否是`cachedHoldCounter`
                    HoldCounter rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current))
                        rh = readHolds.get(); // 否则, 从容器`readHolds`中取出`count`
                    int count = rh.count;
                    if (count <= 1) {
                        readHolds.remove();
                        if (count <= 0)
                            throw unmatchedUnlockException();
                    }
                    --rh.count;
                }
                for (;;) {
                    int c = getState();
                    int nextc = c - SHARED_UNIT; // 写锁使用`state`的高16位, 因此释放一个`读锁`时直接减去`2的16次方`
                    if (compareAndSetState(c, nextc)) // 更新`state`成功才返回
                        return nextc == 0;
                }
            }

            private IllegalMonitorStateException unmatchedUnlockException() {
                return new IllegalMonitorStateException(
                        "attempt to unlock read lock, not locked by current thread");
            }

            /**
             * 尝试获取读锁: 快速版
             * @param unused
             * @return
             */
            protected final int tryAcquireShared(int unused) {
                Thread current = Thread.currentThread();
                int c = getState();
                if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current) // 写锁被占用且不是当前线程, 获取失败
                    return -1;
                int r = sharedCount(c); // 读锁占用数量
                if (!readerShouldBlock() &&
                        r < MAX_COUNT &&
                        compareAndSetState(c, c + SHARED_UNIT)) { // 尝试获取读锁成功
                    if (r == 0) { // r == 0: 读锁还未被占用, 当前线程是第一个
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) { // 读锁已经被自己占用, 占用数+1
                        firstReaderHoldCount++;
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        // 否则将自己设为`cachedHoldCounter`, 即: 最后一个获取读锁的线程为`cachedHoldCounter`
                        if (rh == null || rh.tid != getThreadId(current))
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return 1;
                }
                // 快速获取锁失败, 尝试完整版
                return fullTryAcquireShared(current);
            }

            /**
             * 尝试获取读锁: 完整版, 处理了`读锁`的`可重入`和CAS设置`state`失败的问题
             */
            final int fullTryAcquireShared(Thread current) {
                HoldCounter rh = null;
                for (;;) {
                    int c = getState();
                    if (exclusiveCount(c) != 0) {
                        if (getExclusiveOwnerThread() != current) // 写锁被占用且不是当前线程, 获取失败
                            return -1;
                    } else if (readerShouldBlock()) {
                        // 获取读锁应该被阻塞, 判断获取线程现在是否持有`读锁`
                        // 如果不持有`读锁`才可以阻塞, 否则应该自旋进行尝试, 因此`读锁`是可重入的
                        if (firstReader == current) { // 当前线程是`firstReader`, 现在肯定持有`读锁`, 不能阻塞
                            // assert firstReaderHoldCount > 0;
                        } else {
                            if (rh == null) {
                                rh = cachedHoldCounter;
                                if (rh == null || rh.tid != getThreadId(current)) {
                                    rh = readHolds.get();
                                    if (rh.count == 0)
                                        readHolds.remove();
                                }
                            }
                            if (rh.count == 0) // 如果该线程不持有`读锁`, 则可以阻塞, 返回`-1`
                                return -1;
                        }
                    }
                    if (sharedCount(c) == MAX_COUNT) // 锁溢出
                        throw new Error("Maximum lock count exceeded");
                    if (compareAndSetState(c, c + SHARED_UNIT)) {
                        if (sharedCount(c) == 0) { // 更新之前的读锁数量为`0`, 当前线程是第一个获取读锁的线程
                            firstReader = current;
                            firstReaderHoldCount = 1;
                        } else if (firstReader == current) {
                            firstReaderHoldCount++;
                        } else {
                            if (rh == null)
                                rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current))
                                rh = readHolds.get();
                            else if (rh.count == 0)
                                readHolds.set(rh);
                            rh.count++;
                            cachedHoldCounter = rh; // cache for release
                        }
                        return 1;
                    }
                }
            }

            /**
             * 尝试获取写锁
             * @return
             */
            final boolean tryWriteLock() {
                Thread current = Thread.currentThread();
                int c = getState();
                if (c != 0) { // 当前有`读锁`或者`写锁`
                    int w = exclusiveCount(c); // `写锁`持有状态
                    if (w == 0 || current != getExclusiveOwnerThread())
                        // 1.w == 0: `读锁`被持有, 不允许在获取`写锁`, 因为不允许`锁升级`
                        // 2.current != getExclusiveOwnerThread(): 当前线程为持有`写锁`
                        return false;
                    if (w == MAX_COUNT)
                        throw new Error("Maximum lock count exceeded");
                }
                if (!compareAndSetState(c, c + 1)) // 设置失败, 说明其他线程抢占了锁
                    return false;
                setExclusiveOwnerThread(current);
                return true;
            }

            /**
             * 尝试获取读锁: 自旋获取
             * @return
             */
            final boolean tryReadLock() {
                Thread current = Thread.currentThread();
                for (;;) {
                    int c = getState();
                    if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current) // 写锁被占用且不是当前线程, 获取失败
                        return false;
                    int r = sharedCount(c);
                    if (r == MAX_COUNT)
                        throw new Error("Maximum lock count exceeded");
                    if (compareAndSetState(c, c + SHARED_UNIT)) {
                        if (r == 0) {
                            firstReader = current;
                            firstReaderHoldCount = 1;
                        } else if (firstReader == current) {
                            firstReaderHoldCount++;
                        } else {
                            HoldCounter rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current))
                                cachedHoldCounter = rh = readHolds.get();
                            else if (rh.count == 0)
                                readHolds.set(rh);
                            rh.count++;
                        }
                        return true;
                    }
                }
            }

            protected final boolean isHeldExclusively() {
                return getExclusiveOwnerThread() == Thread.currentThread();
            }

            // Methods relayed to outer class

            final ConditionObject newCondition() {
                return new ConditionObject();
            }

            final Thread getOwner() {
                return ((exclusiveCount(getState()) == 0) ? null : getExclusiveOwnerThread());
            }

            final int getReadLockCount() {
                return sharedCount(getState());
            }

            final boolean isWriteLocked() {
                return exclusiveCount(getState()) != 0;
            }

            final int getWriteHoldCount() {
                return isHeldExclusively() ? exclusiveCount(getState()) : 0;
            }

            /**
             * 当前线程获取的`读锁`数量
             * @return
             */
            final int getReadHoldCount() {
                if (getReadLockCount() == 0) // `读锁`未被任何线程获取
                    return 0;

                Thread current = Thread.currentThread();
                if (firstReader == current) // 当前线程是`firstReader`
                    return firstReaderHoldCount;

                HoldCounter rh = cachedHoldCounter;
                if (rh != null && rh.tid == getThreadId(current)) // 当前线程是`cachedHoldCounter`
                    return rh.count;

                int count = readHolds.get().count; // 从`ThreadLocal`中获取
                if (count == 0) readHolds.remove();
                return count;
            }

            final int getCount() { return getState(); }
        }

        /**
         * 非公平锁实现
         */
        final class NonfairSync extends Sync {
            private static final long serialVersionUID = -8159625535654395037L;
            final boolean writerShouldBlock() { // 非公平的锁: 获取锁时不阻塞
                return false;
            }
            final boolean readerShouldBlock() {
                // 同步队列中第一个节点是`Exclusive`的则返回`true`, 即: 第一个等待锁的节点是`写锁`时, 则应该阻塞
                return apparentlyFirstQueuedIsExclusive();
            }
        }

        /**
         * 公平锁实现: 严格地按顺序获取锁
         */
        final class FairSync extends Sync {
            private static final long serialVersionUID = -2274990926593161451L;
            final boolean writerShouldBlock() { // 公平的锁: 如果当前`同步队列`有节点正在等待, 则必须阻塞
                return hasQueuedPredecessors();
            }
            final boolean readerShouldBlock() { // 公平的锁: 如果当前`同步队列`有节点正在等待, 则必须阻塞
                return hasQueuedPredecessors();
            }
        }

        /**
         * 读锁
         */
        class ReadLock implements Lock, java.io.Serializable {
            private static final long serialVersionUID = -5992448646407690164L;
            private final Sync sync;

            protected ReadLock(ReentrantReadWriteLock lock) {
                sync = lock.sync;
            }

            public void lock() {
                sync.acquireShared(1);
            }

            public boolean tryLock() {
                return sync.tryReadLock();
            }

            public void unlock() {
                sync.releaseShared(1);
            }

            /**
             * 读锁不支持`Condition`
             */
            public Condition newCondition() {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * 写锁
         */
        class WriteLock implements Lock, java.io.Serializable {
            private static final long serialVersionUID = -4992448646407690164L;
            private final Sync sync;

            protected WriteLock(ReentrantReadWriteLock lock) {
                sync = lock.sync;
            }

            public void lock() {
                sync.acquire(1);
            }

            public boolean tryLock( ) {
                return sync.tryWriteLock();
            }

            public void unlock() {
                sync.release(1);
            }

            public Condition newCondition() {
                return sync.newCondition();
            }

            public boolean isHeldByCurrentThread() {
                return sync.isHeldExclusively();
            }

            public int getHoldCount() {
                return sync.getWriteHoldCount();
            }
        }

    }

}
