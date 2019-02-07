package com.demos.java.jdkanalyzer.concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/18
 *
 * ===== 执行任务: ScheduledFutureTask.run()
 * -> 1.判断当前线程池状态: 是否可以继续执行
 * -> 2.非周期任务: 直接执行
 * -> 3.周期任务: 执行并重置state
 * -> 4.设置下一次执行周期任务的时间
 * === 固定间隔: 下一次执行时间 = 本次执行时间 + period
 * === 固定延迟: 下一次执行时间 = 当前时间 + |period|
 * -> 5.将再次执行的任务加入阻塞队列
 * ===== 获取任务: DelayedWorkQueue.take(): 使用堆来管理任务: 最先执行的任务在堆顶
 * -> 1.拿到堆顶部的任务
 * -> 2.计算任务执行时间与当前时间的差值
 * -> 3.如果差值<=0: 说明当前任务可以执行, 重新调整堆, 并返回堆顶元素
 * -> 4.如果线程不为null, 说明已经超时等待过一次, 则这次不再超时等待, 而是直接调用await()等待唤醒
 * -> 5.超时等待: 直到任务可以执行
 */
public class ScheduledExecutorAnalyzer {

    static class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {

    /*
     * This class specializes ThreadPoolExecutor implementation by
     *
     * 1. Using a custom task type, ScheduledFutureTask for
     *    tasks, even those that don't require scheduling (i.e.,
     *    those submitted using ExecutorService execute, not
     *    ScheduledExecutorService methods) which are treated as
     *    delayed tasks with a delay of zero.
     *
     * 2. Using a custom queue (DelayedWorkQueue), a variant of
     *    unbounded DelayQueue. The lack of capacity constraint and
     *    the fact that corePoolSize and maximumPoolSize are
     *    effectively identical simplifies some execution mechanics
     *    (see delayedExecute) compared to ThreadPoolExecutor.
     *
     * 3. Supporting optional run-after-shutdown parameters, which
     *    leads to overrides of shutdown methods to remove and cancel
     *    tasks that should NOT be run after shutdown, as well as
     *    different recheck logic when task (re)submission overlaps
     *    with a shutdown.
     *
     * 4. Task decoration methods to allow interception and
     *    instrumentation, which are needed because subclasses cannot
     *    otherwise override submit methods to get this effect. These
     *    don't have any impact on pool control logic though.
     */

        /**
         * False if should cancel/suppress periodic tasks on shutdown.
         */
        private volatile boolean continueExistingPeriodicTasksAfterShutdown;

        /**
         * False if should cancel non-periodic tasks on shutdown.
         */
        private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

        /**
         * True if ScheduledFutureTask.cancel should remove from queue
         */
        private volatile boolean removeOnCancel = false;

        /**
         * Sequence number to break scheduling ties, and in turn to
         * guarantee FIFO order among tied entries.
         */
        private static final AtomicLong sequencer = new AtomicLong();

        /**
         * Returns current nanosecond time.
         */
        final long now() {
            return System.nanoTime();
        }

        /**
         * 调度任务类
         */
        private class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

            /** Sequence number to break ties FIFO */
            private final long sequenceNumber;

            /** The time the task is enabled to execute in nanoTime units */
            // 当前任务可以执行的时间
            private long time;

            /**
             * Period in nanoseconds for repeating tasks.  A positive
             * value indicates fixed-rate execution.  A negative value
             * indicates fixed-delay execution.  A value of 0 indicates a
             * non-repeating task.
             */
            /* 重复执行任务的时间: 纳秒
            正数: 以固定间隔执行
            负数: 以固定延迟执行
            0: 不重复执行
             */
            private final long period;

            /** The actual task to be re-enqueued by reExecutePeriodic */
            // 再次执行的任务
            RunnableScheduledFuture<V> outerTask = this;

            /**
             * Index into delay queue, to support faster cancellation.
             */
            int heapIndex;

            /**
             * Creates a one-shot action with given nanoTime-based trigger time.
             */
            ScheduledFutureTask(Runnable r, V result, long ns) {
                super(r, result);
                this.time = ns;
                this.period = 0;
                this.sequenceNumber = sequencer.getAndIncrement();
            }

            /**
             * Creates a periodic action with given nano time and period.
             */
            ScheduledFutureTask(Runnable r, V result, long ns, long period) {
                super(r, result);
                this.time = ns;
                this.period = period;
                this.sequenceNumber = sequencer.getAndIncrement();
            }

            /**
             * Creates a one-shot action with given nanoTime-based trigger time.
             */
            ScheduledFutureTask(Callable<V> callable, long ns) {
                super(callable);
                this.time = ns;
                this.period = 0;
                this.sequenceNumber = sequencer.getAndIncrement();
            }

            public long getDelay(TimeUnit unit) {
                return unit.convert(time - now(), NANOSECONDS);
            }

            public int compareTo(Delayed other) {
                if (other == this) // compare zero if same object
                    return 0;
                if (other instanceof ScheduledFutureTask) {
                    ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
                    long diff = time - x.time;
                    if (diff < 0)
                        return -1;
                    else if (diff > 0)
                        return 1;
                    else if (sequenceNumber < x.sequenceNumber)
                        return -1;
                    else
                        return 1;
                }
                long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
                return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
            }

            /**
             * Returns {@code true} if this is a periodic (not a one-shot) action.
             *
             * @return {@code true} if periodic
             */
            public boolean isPeriodic() {
                return period != 0;
            }

            /**
             * 设置下一次执行周期任务的时间:
             * -> 固定间隔: 下一次执行时间 = 本次执行时间 + period
             * -> 固定延迟: 下一次执行时间 = 当前时间 + |period|
             */
            private void setNextRunTime() {
                long p = period;
                // period > 0: 固定间隔: 下一次执行时间 = 本次执行时间 + period
                if (p > 0)
                    time += p;
                // 固定延迟: 下一次执行时间 = 当前时间 + |period|
                else
                    time = triggerTime(-p);
            }

            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = super.cancel(mayInterruptIfRunning);
                if (cancelled && removeOnCancel && heapIndex >= 0)
                    remove(this);
                return cancelled;
            }

            /**
             * 执行任务:
             * -> 1.判断当前线程池状态: 是否可以继续执行
             * -> 2.非周期任务: 直接执行
             * -> 3.周期任务: 执行并重置state
             * -> 4.设置下一次执行周期任务的时间
             * === 固定间隔: 下一次执行时间 = 本次执行时间 + period
             * === 固定延迟: 下一次执行时间 = 当前时间 + |period|
             * -> 5.将再次执行的任务加入阻塞队列
             */
            public void run() {
                // 是否周期执行
                boolean periodic = isPeriodic();
                // 判断当前线程池状态: 是否可以继续执行
                if (!canRunInCurrentRunState(periodic))
                    cancel(false);
                // 非周期任务: 直接执行
                else if (!periodic)
                    ScheduledFutureTask.super.run();
                // 周期任务: 执行并重置state
                else if (ScheduledFutureTask.super.runAndReset()) {
                    // 设置下一次执行周期任务的时间
                    setNextRunTime();
                    // 将再次执行的任务加入阻塞队列
                    reExecutePeriodic(outerTask);
                }
            }
        }

        /**
         * Returns true if can run a task given current run state
         * and run-after-shutdown parameters.
         *
         * @param periodic true if this task periodic, false if delayed
         */
        boolean canRunInCurrentRunState(boolean periodic) {
            return isRunningOrShutdown(periodic ?
                    continueExistingPeriodicTasksAfterShutdown :
                    executeExistingDelayedTasksAfterShutdown);
        }

        /**
         * Main execution method for delayed or periodic tasks.  If pool
         * is shut down, rejects the task. Otherwise adds task to queue
         * and starts a thread, if necessary, to run it.  (We cannot
         * prestart the thread to run the task because the task (probably)
         * shouldn't be run yet.)  If the pool is shut down while the task
         * is being added, cancel and remove it if required by state and
         * run-after-shutdown parameters.
         *
         * @param task the task
         */
        private void delayedExecute(RunnableScheduledFuture<?> task) {
            if (isShutdown())
                reject(task);
            else {
                super.getQueue().add(task);
                if (isShutdown() &&
                        !canRunInCurrentRunState(task.isPeriodic()) &&
                        remove(task))
                    task.cancel(false);
                else
                    ensurePrestart();
            }
        }

        /**
         * Requeues a periodic task unless current run state precludes it.
         * Same idea as delayedExecute except drops task rather than rejecting.
         *
         * @param task the task
         */
        void reExecutePeriodic(RunnableScheduledFuture<?> task) {
            if (canRunInCurrentRunState(true)) {
                super.getQueue().add(task);
                if (!canRunInCurrentRunState(true) && remove(task))
                    task.cancel(false);
                else
                    ensurePrestart();
            }
        }

        /**
         * Cancels and clears the queue of all tasks that should not be run
         * due to shutdown policy.  Invoked within super.shutdown.
         */
        void onShutdown() {
            BlockingQueue<Runnable> q = super.getQueue();
            boolean keepDelayed =
                    getExecuteExistingDelayedTasksAfterShutdownPolicy();
            boolean keepPeriodic =
                    getContinueExistingPeriodicTasksAfterShutdownPolicy();
            if (!keepDelayed && !keepPeriodic) {
                for (Object e : q.toArray())
                    if (e instanceof RunnableScheduledFuture<?>)
                        ((RunnableScheduledFuture<?>) e).cancel(false);
                q.clear();
            }
            else {
                // Traverse snapshot to avoid iterator exceptions
                for (Object e : q.toArray()) {
                    if (e instanceof RunnableScheduledFuture) {
                        RunnableScheduledFuture<?> t =
                                (RunnableScheduledFuture<?>)e;
                        if ((t.isPeriodic() ? !keepPeriodic : !keepDelayed) ||
                                t.isCancelled()) { // also remove if already cancelled
                            if (q.remove(t))
                                t.cancel(false);
                        }
                    }
                }
            }
            tryTerminate();
        }

        /**
         * Modifies or replaces the task used to execute a runnable.
         * This method can be used to override the concrete
         * class used for managing internal tasks.
         * The default implementation simply returns the given task.
         *
         * @param runnable the submitted Runnable
         * @param task the task created to execute the runnable
         * @param <V> the type of the task's result
         * @return a task that can execute the runnable
         * @since 1.6
         */
        protected <V> RunnableScheduledFuture<V> decorateTask(
                Runnable runnable, RunnableScheduledFuture<V> task) {
            return task;
        }

        /**
         * Modifies or replaces the task used to execute a callable.
         * This method can be used to override the concrete
         * class used for managing internal tasks.
         * The default implementation simply returns the given task.
         *
         * @param callable the submitted Callable
         * @param task the task created to execute the callable
         * @param <V> the type of the task's result
         * @return a task that can execute the callable
         * @since 1.6
         */
        protected <V> RunnableScheduledFuture<V> decorateTask(
                Callable<V> callable, RunnableScheduledFuture<V> task) {
            return task;
        }

        /**
         * Creates a new {@code ScheduledThreadPoolExecutor} with the
         * given core pool size.
         *
         * @param corePoolSize the number of threads to keep in the pool, even
         *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
         * @throws IllegalArgumentException if {@code corePoolSize < 0}
         */
        public ScheduledThreadPoolExecutor(int corePoolSize) {
            super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
                    new DelayedWorkQueue());
        }

        /**
         * Returns the trigger time of a delayed action.
         */
        private long triggerTime(long delay, TimeUnit unit) {
            return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
        }

        /**
         * Returns the trigger time of a delayed action.
         */
        long triggerTime(long delay) {
            return now() +
                    ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
        }

        /**
         * Constrains the values of all delays in the queue to be within
         * Long.MAX_VALUE of each other, to avoid overflow in compareTo.
         * This may occur if a task is eligible to be dequeued, but has
         * not yet been, while some other task is added with a delay of
         * Long.MAX_VALUE.
         */
        private long overflowFree(long delay) {
            Delayed head = (Delayed) super.getQueue().peek();
            if (head != null) {
                long headDelay = head.getDelay(NANOSECONDS);
                if (headDelay < 0 && (delay - headDelay < 0))
                    delay = Long.MAX_VALUE + headDelay;
            }
            return delay;
        }

        /**
         * @throws RejectedExecutionException {@inheritDoc}
         * @throws NullPointerException       {@inheritDoc}
         */
        public ScheduledFuture<?> schedule(Runnable command,
                                           long delay,
                                           TimeUnit unit) {
            if (command == null || unit == null)
                throw new NullPointerException();
            RunnableScheduledFuture<?> t = decorateTask(command,
                    new ScheduledFutureTask<Void>(command, null,
                            triggerTime(delay, unit)));
            delayedExecute(t);
            return t;
        }

        /**
         * @throws RejectedExecutionException {@inheritDoc}
         * @throws NullPointerException       {@inheritDoc}
         */
        public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                               long delay,
                                               TimeUnit unit) {
            if (callable == null || unit == null)
                throw new NullPointerException();
            RunnableScheduledFuture<V> t = decorateTask(callable,
                    new ScheduledFutureTask<V>(callable,
                            triggerTime(delay, unit)));
            delayedExecute(t);
            return t;
        }

        /**
         * @throws RejectedExecutionException {@inheritDoc}
         * @throws NullPointerException       {@inheritDoc}
         * @throws IllegalArgumentException   {@inheritDoc}
         */
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                      long initialDelay,
                                                      long period,
                                                      TimeUnit unit) {
            if (command == null || unit == null)
                throw new NullPointerException();
            if (period <= 0)
                throw new IllegalArgumentException();
            ScheduledFutureTask<Void> sft =
                    new ScheduledFutureTask<Void>(command,
                            null,
                            triggerTime(initialDelay, unit),
                            unit.toNanos(period));
            RunnableScheduledFuture<Void> t = decorateTask(command, sft);
            sft.outerTask = t;
            delayedExecute(t);
            return t;
        }

        /**
         * @throws RejectedExecutionException {@inheritDoc}
         * @throws NullPointerException       {@inheritDoc}
         * @throws IllegalArgumentException   {@inheritDoc}
         */
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                         long initialDelay,
                                                         long delay,
                                                         TimeUnit unit) {
            if (command == null || unit == null)
                throw new NullPointerException();
            if (delay <= 0)
                throw new IllegalArgumentException();
            ScheduledFutureTask<Void> sft =
                    new ScheduledFutureTask<Void>(command,
                            null,
                            triggerTime(initialDelay, unit),
                            unit.toNanos(-delay));
            RunnableScheduledFuture<Void> t = decorateTask(command, sft);
            sft.outerTask = t;
            delayedExecute(t);
            return t;
        }

        /**
         * Executes {@code command} with zero required delay.
         * This has effect equivalent to
         * {@link #schedule(Runnable,long,TimeUnit) schedule(command, 0, anyUnit)}.
         * Note that inspections of the queue and of the list returned by
         * {@code shutdownNow} will access the zero-delayed
         * {@link ScheduledFuture}, not the {@code command} itself.
         *
         * <p>A consequence of the use of {@code ScheduledFuture} objects is
         * that {@link ThreadPoolExecutor#afterExecute afterExecute} is always
         * called with a null second {@code Throwable} argument, even if the
         * {@code command} terminated abruptly.  Instead, the {@code Throwable}
         * thrown by such a task can be obtained via {@link Future#get}.
         *
         * @throws RejectedExecutionException at discretion of
         *         {@code RejectedExecutionHandler}, if the task
         *         cannot be accepted for execution because the
         *         executor has been shut down
         * @throws NullPointerException {@inheritDoc}
         */
        public void execute(Runnable command) {
            schedule(command, 0, NANOSECONDS);
        }

        // Override AbstractExecutorService methods

        /**
         * @throws RejectedExecutionException {@inheritDoc}
         * @throws NullPointerException       {@inheritDoc}
         */
        public Future<?> submit(Runnable task) {
            return schedule(task, 0, NANOSECONDS);
        }

        /**
         * @throws RejectedExecutionException {@inheritDoc}
         * @throws NullPointerException       {@inheritDoc}
         */
        public <T> Future<T> submit(Runnable task, T result) {
            return schedule(Executors.callable(task, result), 0, NANOSECONDS);
        }

        /**
         * @throws RejectedExecutionException {@inheritDoc}
         * @throws NullPointerException       {@inheritDoc}
         */
        public <T> Future<T> submit(Callable<T> task) {
            return schedule(task, 0, NANOSECONDS);
        }

        /* ========== DelayedWorkQueue start ========== */
        // 内部使用`堆`来管理任务
        /**
         * Specialized delay queue. To mesh with TPE declarations, this
         * class must be declared as a BlockingQueue<Runnable> even though
         * it can only hold RunnableScheduledFutures.
         */
        static class DelayedWorkQueue {

        /*
         * A DelayedWorkQueue is based on a heap-based data structure
         * like those in DelayQueue and PriorityQueue, except that
         * every ScheduledFutureTask also records its index into the
         * heap array. This eliminates the need to find a task upon
         * cancellation, greatly speeding up removal (down from O(n)
         * to O(log n)), and reducing garbage retention that would
         * otherwise occur by waiting for the element to rise to top
         * before clearing. But because the queue may also hold
         * RunnableScheduledFutures that are not ScheduledFutureTasks,
         * we are not guaranteed to have such indices available, in
         * which case we fall back to linear search. (We expect that
         * most tasks will not be decorated, and that the faster cases
         * will be much more common.)
         *
         * All heap operations must record index changes -- mainly
         * within siftUp and siftDown. Upon removal, a task's
         * heapIndex is set to -1. Note that ScheduledFutureTasks can
         * appear at most once in the queue (this need not be true for
         * other kinds of tasks or work queues), so are uniquely
         * identified by heapIndex.
         */

            private static final int INITIAL_CAPACITY = 16;
            private RunnableScheduledFuture<?>[] queue =
                    new RunnableScheduledFuture<?>[INITIAL_CAPACITY];
            private final ReentrantLock lock = new ReentrantLock();
            private int size = 0;

            /**
             * Thread designated to wait for the task at the head of the
             * queue.  This variant of the Leader-Follower pattern
             * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to
             * minimize unnecessary timed waiting.  When a thread becomes
             * the leader, it waits only for the next delay to elapse, but
             * other threads await indefinitely.  The leader thread must
             * signal some other thread before returning from take() or
             * poll(...), unless some other thread becomes leader in the
             * interim.  Whenever the head of the queue is replaced with a
             * task with an earlier expiration time, the leader field is
             * invalidated by being reset to null, and some waiting
             * thread, but not necessarily the current leader, is
             * signalled.  So waiting threads must be prepared to acquire
             * and lose leadership while waiting.
             */
            private Thread leader = null;

            /**
             * Condition signalled when a newer task becomes available at the
             * head of the queue or a new thread may need to become leader.
             */
            private final Condition available = lock.newCondition();

            /**
             * Sets f's heapIndex if it is a ScheduledFutureTask.
             */
            private void setIndex(RunnableScheduledFuture<?> f, int idx) {
                if (f instanceof ScheduledFutureTask)
                    ((ScheduledFutureTask)f).heapIndex = idx;
            }

            /**
             * Sifts element added at bottom up to its heap-ordered spot.
             * Call only when holding lock.
             */
            private void siftUp(int k, RunnableScheduledFuture<?> key) {
                while (k > 0) {
                    int parent = (k - 1) >>> 1;
                    RunnableScheduledFuture<?> e = queue[parent];
                    if (key.compareTo(e) >= 0)
                        break;
                    queue[k] = e;
                    setIndex(e, k);
                    k = parent;
                }
                queue[k] = key;
                setIndex(key, k);
            }

            /**
             * Sifts element added at top down to its heap-ordered spot.
             * Call only when holding lock.
             */
            private void siftDown(int k, RunnableScheduledFuture<?> key) {
                int half = size >>> 1;
                while (k < half) {
                    int child = (k << 1) + 1;
                    RunnableScheduledFuture<?> c = queue[child];
                    int right = child + 1;
                    if (right < size && c.compareTo(queue[right]) > 0)
                        c = queue[child = right];
                    if (key.compareTo(c) <= 0)
                        break;
                    queue[k] = c;
                    setIndex(c, k);
                    k = child;
                }
                queue[k] = key;
                setIndex(key, k);
            }

            /**
             * Resizes the heap array.  Call only when holding lock.
             */
            private void grow() {
                int oldCapacity = queue.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
                if (newCapacity < 0) // overflow
                    newCapacity = Integer.MAX_VALUE;
                queue = Arrays.copyOf(queue, newCapacity);
            }

            public boolean offer(Runnable x) {
                if (x == null)
                    throw new NullPointerException();
                RunnableScheduledFuture<?> e = (RunnableScheduledFuture<?>)x;
                final ReentrantLock lock = this.lock;
                lock.lock();
                try {
                    int i = size;
                    if (i >= queue.length)
                        grow();
                    size = i + 1;
                    if (i == 0) {
                        queue[0] = e;
                        setIndex(e, 0);
                    } else {
                        siftUp(i, e);
                    }
                    if (queue[0] == e) {
                        leader = null;
                        available.signal();
                    }
                } finally {
                    lock.unlock();
                }
                return true;
            }

            public void put(Runnable e) {
                offer(e);
            }

            public boolean add(Runnable e) {
                return offer(e);
            }

            public boolean offer(Runnable e, long timeout, TimeUnit unit) {
                return offer(e);
            }

            /**
             * Performs common bookkeeping for poll and take: Replaces
             * first element with last and sifts it down.  Call only when
             * holding lock.
             * @param f the task to remove and return
             */
            private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> f) {
                int s = --size;
                RunnableScheduledFuture<?> x = queue[s];
                queue[s] = null;
                if (s != 0)
                    siftDown(0, x);
                setIndex(f, -1);
                return f;
            }

            public RunnableScheduledFuture<?> poll() {
                final ReentrantLock lock = this.lock;
                lock.lock();
                try {
                    RunnableScheduledFuture<?> first = queue[0];
                    if (first == null || first.getDelay(NANOSECONDS) > 0)
                        return null;
                    else
                        return finishPoll(first);
                } finally {
                    lock.unlock();
                }
            }

            /**
             * 获取任务: 使用堆来管理任务: 最先执行的任务在堆顶
             * -> 1.拿到堆顶部的任务
             * -> 2.计算任务执行时间与当前时间的差值
             * -> 3.如果差值<=0: 说明当前任务可以执行, 重新调整堆, 并返回堆顶元素
             * -> 4.如果线程不为null, 说明已经超时等待过一次, 则这次不再超时等待, 而是直接调用await()等待唤醒
             * -> 5.超时等待: 直到任务可以执行
             * @return
             * @throws InterruptedException
             */
            public RunnableScheduledFuture<?> take() throws InterruptedException {
                final ReentrantLock lock = this.lock;
                lock.lockInterruptibly();
                try {
                    for (;;) {
                        // 使用堆来管理任务: 最先执行的任务在堆顶
                        // 拿到堆顶部的任务
                        RunnableScheduledFuture<?> first = queue[0];
                        if (first == null)
                            available.await();
                        else {
                            // 计算任务执行时间与当前时间的差值
                            long delay = first.getDelay(NANOSECONDS);
                            // 如果差值<=0: 说明当前任务可以执行, 重新调整堆, 并返回堆顶元素
                            if (delay <= 0)
                                return finishPoll(first);
                            first = null; // don't retain ref while waiting
                            // 如果线程不为null, 说明已经超时等待过一次, 则这次不再超时等待, 而是直接调用await()等待唤醒
                            if (leader != null)
                                available.await();
                            else {
                                Thread thisThread = Thread.currentThread();
                                leader = thisThread;
                                try {
                                    // 超时等待: 直到任务可以执行
                                    available.awaitNanos(delay);
                                } finally {
                                    if (leader == thisThread)
                                        leader = null;
                                }
                            }
                        }
                    }
                } finally {
                    if (leader == null && queue[0] != null)
                        available.signal();
                    lock.unlock();
                }
            }

            public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit)
                    throws InterruptedException {
                long nanos = unit.toNanos(timeout);
                final ReentrantLock lock = this.lock;
                lock.lockInterruptibly();
                try {
                    for (;;) {
                        RunnableScheduledFuture<?> first = queue[0];
                        if (first == null) {
                            if (nanos <= 0)
                                return null;
                            else
                                nanos = available.awaitNanos(nanos);
                        } else {
                            long delay = first.getDelay(NANOSECONDS);
                            if (delay <= 0)
                                return finishPoll(first);
                            if (nanos <= 0)
                                return null;
                            first = null; // don't retain ref while waiting
                            if (nanos < delay || leader != null)
                                nanos = available.awaitNanos(nanos);
                            else {
                                Thread thisThread = Thread.currentThread();
                                leader = thisThread;
                                try {
                                    long timeLeft = available.awaitNanos(delay);
                                    nanos -= delay - timeLeft;
                                } finally {
                                    if (leader == thisThread)
                                        leader = null;
                                }
                            }
                        }
                    }
                } finally {
                    if (leader == null && queue[0] != null)
                        available.signal();
                    lock.unlock();
                }
            }
        }
        /* ========== DelayedWorkQueue start ========== */
    }

}
