package com.demos.java.jdkanalyzer.spring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/16
 *
 * 线程池解析
 * ===== 核心方法:
 * -> execute()
 * -> addWorker()
 * -> runWorker()
 * -> getTask()
 * -> processWorkerExit()
 * -> tryTerminate()
 * ===== 勾子方法:
 * -> beforeExecute()
 * -> afterExecute()
 * -> terminated()
 * ===== 终结方法:
 * -> shutdown()
 * -> shutdownNow()
 * -> awaitTermination()
 * ===== 拒绝策略:
 * -> CallerRunsPolicy
 * -> AbortPolicy
 * -> DiscardPolicy
 * -> DiscardOldestPolicy
 */
public class ThreadPoolExecutorAnalyzer extends AbstractExecutorService {

    /* 线程池`状态控制`(最高3位)和`有效线程数`(低29位)字段
    ===== 线程池状态:
    -> RUNNING: 正常运行, 可以接收新任务和处理队列中的任务
    -> SHUTDOWN: 不能接收新任务, 但可以处理队列中的任务
    -> STOP: 不能接收新任务, 也不可以处理队列中的任务, 并且会尝试中断正在执行中的任务
    -> TIDYING: 所有任务已终止, 且工作线程数 workerCount=0, 进入`TIDYING`状态后会执行`terminated()`勾子方法
    -> TERMINATED: 已终止, `terminated()`勾子方法已执行完毕, 调用`awaitTermination()`的线程会在线程池到达`TERMINATED`状态时返回
     */
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    // 线程池运行状态
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;

    // 获取线程池运行状态
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    // 获取线程池工作线程数
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    private static boolean runStateLessThan(int c, int s) { return c < s; }

    // 判断状态值, 如: runStateAtLeast(ctl.get(), STOP)
    private static boolean runStateAtLeast(int c, int s) { return c >= s; }

    private static boolean isRunning(int c) { return c < SHUTDOWN; }

    // 尝试以CAS方式设置`workerCount`加1
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    // 尝试以CAS方式设置`workerCount`减1
    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    // 执行`workerCount`减1直到成功
    private void decrementWorkerCount() {
        do {} while (! compareAndDecrementWorkerCount(ctl.get()));
    }

    // 任务队列: 阻塞队列
    // `消费者`(工作线程)通常使用阻塞式的`take()`获取任务, 以免循环中断而退出
    // `生产者`(调用线程)通常使用非阻塞式的`offer()`提交任务, 以免阻塞调用线程, 若任务队列已满, 则`RejectedExecutionHandler`执行拒绝策略
    private BlockingQueue<Runnable> workQueue;

    // 用来进行同步控制
    private final ReentrantLock mainLock = new ReentrantLock();

    // 工作线程集合, 该set(HashSet)由`mainLock`控制, 以避免同步问题
    private final HashSet<Worker> workers = new HashSet<>();

    // 等待条件, 为了支持`awaitTermination()`方法
    private final Condition termination = mainLock.newCondition();

    // 曾经达到的最大线程数
    private int largestPoolSize;

    // 线程池已完成的任务数: 只会在工作线程退出时更新, 因此不是实时的
    private long completedTaskCount;

    /* 所有由用户控制的参数均为`volatile`变量, 以便参数值改变时能立即看到最新的值, 而不需要加锁 */

    // 线程创建工厂
    private volatile ThreadFactory threadFactory;

    // 拒绝策略: 调用`execute`方法时, 若`阻塞队列已满`或者`线程池已经shutdown`则调用
    private volatile RejectedExecutionHandler handler;

    // 线程空闲超时时间
    private volatile long keepAliveTime;

    // 是否允许核心线程空闲超时
    private volatile boolean allowCoreThreadTimeOut;

    // 核心线程数
    private volatile int corePoolSize;

    // 最大线程数
    private volatile int maximumPoolSize;

    // 默认拒绝策略: 抛出`RejectedExecutionException`异常
    private static final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * Worker类: 工作线程, 负责执行阻塞队列中的任务
     * -> 继承`AbstractQueuedSynchronizer`, 实现了简单的`不可重入`的`互斥锁`, 用来控制线程的阻塞和唤醒操作
     */
    private final class Worker extends AbstractQueuedSynchronizer implements Runnable {

        // 任务执行线程
        final Thread thread;
        // 初始化`Worker`后执行的第一个任务, 可以为null
        Runnable firstTask;
        // 线程已经执行完的任务数
        volatile long completedTasks;

        Worker(Runnable firstTask) {
            setState(-1); // 初始化时锁的`state`为-1, 不允许在此时中断, 直到`runWorker()`时释放
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        public void run() {
            runWorker(this);
        }


        protected boolean isHeldExclusively() { // 0: unlocked, 1: locked
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) { // 执行释放, 不判断是否持有该锁
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
    /*------------------------------ Worker类结束 ------------------------------*/

    /**
     * 提升运行状态: RUNNING < SHUTDOWN < STOP < TIDYING < TERMINATED
     */
    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) // `当前线程池状态`已经大于`要设置的状态`, 则不需要再设置
                    || ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c)))) // 设置状态成功则退出
                break;
        }
    }

    /**
     * 尝试进行终止工作: 根据运行状态来进行处理, 即: 如果是`RUNNING`状态则并不会终止
     */
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            if (isRunning(c) ||
                    runStateAtLeast(c, TIDYING) ||
                    (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                // 如果: 1)线程池正在运行, 2)线程池已经到达`TIDYING`, 不再需要额外的处理, 3)已调用`shutdown()`但阻塞队列不为空, 即: 不需要此时终止所有线程
                // 则: 可以安全退出
                return;
            // 进入这里, 说明线程池正在停止中, 需要做清理工作
            if (workerCountOf(c) != 0) { // 如果工作线程数不为0, 中断正在阻塞等待任务的线程, 以防止终止状态不被响应
                interruptIdleWorkers(ONLY_ONE); //  只需要中断一个阻塞中的线程, 该线程唤醒后会处理终止事件
                return;
            }
            // 否则: 已经没有工作线程, 清理工作已经处理完毕
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) { // 设置运行状态为`TIDYING`
                    try {
                        terminated(); // 调用`terminated()`勾子方法
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0)); // 调用完`terminated()`方法后, 将运行状态设置为`TERMINATED`, 至此, 线程池已经终结
                        termination.signalAll(); // 线程池已终结, 唤醒调用`awaitTermination()`方法阻塞的线程
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }

    // 修改线程的权限
    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

    /**
     * 调用者是否有终结线程的权限
     */
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * 中断所有工作线程
     */
    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断正阻塞等待任务的线程: 主要是防止所有线程都在等待任务而不能相应终止事件
     * 因此可以只中断一个线程, 该线程从中断中唤醒后, 如果发现线程池已停止, 则会做相应的终止工作
     */
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) { // 如果线程不是已中断状态且获取该线程的锁成功
                    try {
                        t.interrupt(); // 中断该线程
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne) // 如果只需要中断一个线程则可以就此返回
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断所有正阻塞等待任务的线程
     */
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    /*
     * Misc utilities, most of which are also exported to
     * ScheduledThreadPoolExecutor
     */

    /**
     * 拒绝任务
     */
    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * 调用`shutdown()`时的勾子方法, `ScheduledThreadPoolExecutor`可以在这里取消延时的任务
     */
    void onShutdown() {
    }

    /**
     * 从阻塞队列中移除所有任务并返回
     * @return
     */
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList); // 从阻塞队列中移除所有任务并加入`taskList`以返回
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

    /*
     * Methods for creating, running and cleaning up after workers
     */

    /**
     * 添加新的`Worker`: 若线程池已停止或`ThreadFactory`创建线程失败则返回false
     * @param firstTask
     * @param core
     * @return 是否添加成功
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c); // 线程池运行状态

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN &&
                    ! (rs == SHUTDOWN &&
                            firstTask == null &&
                            ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c); // 当前工作线程数
                if (wc >= CAPACITY ||
                        wc >= (core ? corePoolSize : maximumPoolSize)) // 线程数大于容量值(2^29-1)或者大于允许的最大线程数, 则返回false
                    return false;
                if (compareAndIncrementWorkerCount(c)) // `workerCount`加1成功, 进入创建线程阶段
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs) // 如果线程池运行状态发生了改变, 重新进入外层循环判断运行状态
                    continue retry;
                // 到这里说明`workerCount`加1失败, 内层循环继续尝试加1操作
            }
        }

        // 创建线程
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask); // 创建一个`Worker`
            final Thread t = w.thread;
            if (t != null) { // 如果`Worker`的线程`thread`为null, 说明`ThreadFactory`创建线程失败
                final ReentrantLock mainLock = this.mainLock; // 使用锁进行同步控制
                mainLock.lock();
                try {
                    // 成功获取到锁之后再次进行`运行状态`的判断
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                            (rs == SHUTDOWN && firstTask == null)) { // 如果正在运行或者已经调用`shutdown()`但是任务已提交, 可以继续执行完该任务
                        if (t.isAlive()) // 如果此时线程已经是活跃状态, 则说明线程状态异常, 因为此时尚未调用`Thread.run()`方法
                            throw new IllegalThreadStateException();
                        workers.add(w); // 将`Worker`加入工作线程集合
                        int s = workers.size();
                        if (s > largestPoolSize) // 如果加入`Worker`后线程集合的大小大于`largestPoolSize`, 则更新`largestPoolSize`值
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    t.start(); // 执行`Thread.run()`, 开启新的工作线程
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w); // 如果添加`Worker`失败, 则做失败处理
        }
        return workerStarted;
    }

    /**
     * 添加`Worker`失败处理
     */
    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null)
                workers.remove(w); // 从工作线程集合中移除`Worker`
            decrementWorkerCount(); // `workerCount`减1
            tryTerminate(); // 尝试进行终止
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 处理工作线程退出: 每个线程线程退出时都会执行该方法, 如果线程池还没到`STOP`状态, 需要保证有足够的工作线程来执行任务
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) // 如果是异常退出, 此时`workerCount`还没有减1, 因此这里先执行减1操作
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks; // 更新线程池已执行任务数
            workers.remove(w); // 从工作线程集合中移除`Worker`
        } finally {
            mainLock.unlock();
        }

        tryTerminate(); // 尝试进行终止, 每个线程结束时都会尝试进行终止

        int c = ctl.get();
        if (runStateLessThan(c, STOP)) { // 如果还没到`STOP`, 仍然需要保证已提交的任务执行完毕
            if (!completedAbruptly) { // 如果是非异常终止的
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                if (workerCountOf(c) >= min) // 有足够的工作线程尚在运行, 可以保证任务执行, 因此此处可以返回
                    return;
            }
            addWorker(null, false); // 否则需要添加新的`Worker`以保证已提交任务执行完毕
        }
    }

    /**
     * 以`阻塞`或者`超时等待`的方式获取任务, 若出现以下情况:
     * -> 1.当前工作线程数超过允许存在的最大线程数
     * -> 2.线程池进入`STOP`状态, 即: 调用了`shutdownNow()`方法
     * -> 3.线程池进入`SHUTDOWN`状态(调用了`shutdown()`方法)且已经没有任务(`workQueue`为空)
     * -> 4.线程获取任务应该超时退出, 且上一次获取任务超时
     * 应该返回`null`已退出工作线程
     * @return
     */
    private Runnable getTask() {
        boolean timedOut = false; // 最后一次`poll()`是否超时

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c); // 运行状态

            // 如果线程池已经停止, 返回null以退出Worker线程
            // 即: 调用了`shutdown()`方法且`workQueue`已经为空(SHUTDOWN), 或者调用了`showdownNow()`方法(STOP)
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount(); // `workCount`减1
                return null;
            }

            int wc = workerCountOf(c); // 当前工作线程数

            // 是否超时退出: `允许核心线程超时`或者`当前工作线程数大于核心线程数`, 则超时退出
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            if ((wc > maximumPoolSize || (timed && timedOut)) // 工作线程数大于允许的最大线程数, 或者上一次`poll()`超时且设置了超时退出
                    && (wc > 1 || workQueue.isEmpty())) { // 并且工作线程数大于1或者`workQueue`为空, 则允许退出, 即: 若`workQueue`不为空, 则不允许工作线程全部退出
                // 尝试`workCount`减1, 成功则退出, 这里不调用`decrementWorkerCount()`方法是为了防止并发的线程退出导致工作线程全部退出
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue; // `workCount`减1失败则继续循环进行判断
            }

            try {
                Runnable r = timed ?
                        workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : // 如果应该超时则调用可超时的`poll()`方法
                        workQueue.take(); // 不应该超时则调用阻塞的`take()`方法
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    /**
     * 核心方法: 工作线程执行任务的方法, 循环从阻塞队列获取任务并执行
     * -> 每次任务执行前都需要获取锁, 以免正在执行的任务被中断
     * -> 勾子方法: 每次执行任务前调用`beforeExecute()`, 每次任务执行完毕或抛出异常时调用`afterExecute()`, 异常会导致工作线程结束
     * @param w
     */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // 释放锁, 允许中断
        boolean completedAbruptly = true; // 工作线程是否异常终止
        try {
            // getTask(): 从阻塞队列获取任务, 若没有任务将一直阻塞, 或者线程池终止、超时退出等情况返回null, 则线程结束
            while (task != null || (task = getTask()) != null) {
                w.lock(); // 执行前加锁
                // 如果运行状态大于`STOP`, 需要终止当前正在执行的任务
                // 运行到这一步, 工作线程已经拿到任务, 若此时线程池终止`STOP`, 仍然可以退出
                if ((runStateAtLeast(ctl.get(), STOP) ||
                        (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) &&
                        !wt.isInterrupted())
                    wt.interrupt(); // 设置线程中断状态
                try {
                    beforeExecute(wt, task); // 任务执行前勾子方法
                    Throwable thrown = null;
                    try {
                        task.run(); // 执行任务: 直接调用`run()`方法
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown); // 任务执行完毕或异常的勾子方法
                    }
                } finally {
                    task = null;
                    w.completedTasks++; // 已执行任务数加1
                    w.unlock();
                }
            }
            completedAbruptly = false; // 工作线程正常工作完毕, 设置异常终止为false
        } finally {
            processWorkerExit(w, completedAbruptly); // 处理工作线程退出工作
        }
    }

    /**
     * 提交任务: 如果任务不能被提交并执行, 将使用`RejectedExecutionHandler`处理
     * -> `阻塞队列已满`或者`线程池已停止`都会导致提交失败
     * 提交任务三部曲:
     * -> 1.若`工作线程数`小于`核心线程数`: 直接创建新的`Worker`(核心线程)并运行任务
     * -> 2.尝试以`非阻塞`(offer)方式将任务加入`阻塞队列`, 入队成功后会再次检查线程池运行状态
     * -> 3.若加入`阻塞队列`失败: 则添加新的`Worker`(非核心线程)并运行任务
     * -> 若以上过程失败, 则执行`RejectedExecutionHandler`处理
     * @param command
     */
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) { // 当前线程数小于`核心线程数`, 直接添加新线程
            if (addWorker(command, true)) // 添加新的`Worker`(工作线程), 并将任务作为Worker的`firstTask`
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) { // 如果线程池正在运行, 且任务入队(阻塞队列未满)成功(非阻塞方式入队)
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command)) // 入队之后再次检查运行状态, 如果线程池已经不在运行状态则将任务移出队列并拒绝任务
                reject(command);
            else if (workerCountOf(recheck) == 0) // 如果工作线程数为0, 则添加一个新的`Worker`, 且`firstTask`为null, 因为此时任务已经入队
                addWorker(null, false);
        }
        else if (!addWorker(command, false)) // 如果入队失败(可能是队列已满), 则尝试添加一个新的`Worker`(非核心线程), 并将任务作为其`firstTask`
            reject(command); // 添加新`Worker`失败则拒绝任务
    }

    /**
     * shutdown: 不再接收新任务, 但可以继续执行阻塞队列中的任务
     */
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess(); // 检查调用者是否有权限终止线程
            advanceRunState(SHUTDOWN); // 提升运行状态为`SHUTDOWN`
            interruptIdleWorkers(); // 中断所有正在阻塞待等待任务的线程
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        tryTerminate(); // 尝试终止线程池
    }

    /**
     * 立即终止: 不再接收新的任务, 且尝试终止所有正在运行中的任务
     * @return
     */
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess(); // 检查调用者是否有权限终止线程
            advanceRunState(STOP); // 提升运行状态为`STOP`
            interruptWorkers(); // 中断所有工作线程
            tasks = drainQueue(); // 从阻塞队列中移除所有任务
        } finally {
            mainLock.unlock();
        }
        tryTerminate(); // 尝试终止线程池
        return tasks;
    }

    public boolean isShutdown() {
        return ! isRunning(ctl.get());
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    /**
     * 阻塞等待线程池终结, 进入`TERMINATED`状态后会调用该方法
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runStateAtLeast(ctl.get(), TERMINATED))
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 通常作为终结方法, 如: 在`spring`中配置线程池`destroy-method=finalize`
     */
    protected void finalize() {
        shutdown();
    }

    /**
     * 返回线程工厂, 用来创建新线程
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /* User-level queue utilities */

    /**
     * 获取阻塞队列
     */
    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * 移除阻塞队列中的一个任务
     */
    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // 为防止阻塞队列此时突然变为空, 此处调用`tryTerminate()`防止无法终结的情况
        return removed;
    }

    /* Extension hooks */

    /**
     * 任务执行之前的勾子方法
     */
    protected void beforeExecute(Thread t, Runnable r) { }

    /**
     * 任务执行完毕的勾子方法
     */
    protected void afterExecute(Runnable r, Throwable t) { }

    /**
     * 线程池进入`TIDYING`状态后的勾子方法, 执行完该方法后线程池进入`TERMINATED`状态, 并会唤醒所有在`awaitTermination()`方法上阻塞的线程
     */
    protected void terminated() { }

    /* Predefined RejectedExecutionHandlers */

    /**
     * CallerRunsPolicy: 在调用者线程执行当前任务
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run(); // 直接调用当前任务的`run()`, 即: 在调用者线程执行当前任务
            }
        }
    }

    /**
     * AbortPolicy: 丢弃当前任务并抛出异常
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + e.toString());
        }
    }

    /**
     * DiscardPolicy: 丢弃当前任务
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {
        /**
         * 不做任何处理, 即: 简单丢弃当前任务`r`
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    /**
     * DiscardOldestPolicy: 丢弃最早任务
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll(); // 取出队列中第一个任务(最早入队)且不做任何处理(丢弃最早任务)
                e.execute(r); // 再次尝试执行当前任务`r`
            }
        }
    }

}
