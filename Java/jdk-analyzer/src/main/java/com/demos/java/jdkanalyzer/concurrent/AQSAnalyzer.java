package com.demos.java.jdkanalyzer.concurrent;

import java.util.concurrent.locks.LockSupport;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/7
 * <p>
 * AbstractQueuedSynchronizer分析器:
 * <p>
 * -> 方法列表:
 * ===== 同步状态设置
 * -> getState
 * -> setState
 * -> compareAndSetState
 * <p>
 * ===== 子类同步器实现
 * -> tryAcquire
 * -> tryRelease
 * -> tryAcquireShared
 * -> tryReleaseShared
 * <p>
 * ===== Exclusive模式
 * -> acquire
 * -> release
 * -> acquireQueued
 * -> addWaiter
 * -> enq
 * -> setHead
 * -> unparkSuccessor
 * -> cancelAcquire
 * -> shouldParkAfterFailedAcquire
 * -> selfInterrupt
 * -> parkAndCheckInterrupt
 * -> isHeldExclusively
 * <p>
 * ===== Shared模式
 * -> acquireShared
 * -> releaseShared
 * -> doAcquireShared
 * -> setHeadAndPropagate
 * -> doReleaseShared
 * -> fullyRelease
 * <p>
 * ===== 设置`head`, `tail`, `waitStatus`
 * -> compareAndSetHead
 * -> compareAndSetTail
 * -> compareAndSetWaitStatus
 * -> compareAndSetNext
 * <p>
 * ===== 其他方法
 * -> hasQueuedThreads
 * -> getQueueLength
 * -> getQueuedThreads
 * -> hasWaiters
 * -> getWaitQueueLength
 * -> getWaitingThreads
 * -> acquireInterruptibly
 * -> tryAcquireNanos
 * -> isQueued
 * -> doAcquireInterruptibly
 * -> doAcquireNanos
 * -> doAcquireSharedInterruptibly
 * -> doAcquireSharedNanos
 * -> acquireSharedInterruptibly
 * -> tryAcquireSharedNanos
 * -> hasContended
 * -> getFirstQueuedThread
 * -> fullGetFirstQueuedThread
 * -> apparentlyFirstQueuedIsExclusive
 * -> hasQueuedPredecessors
 * -> getExclusiveQueuedThreads
 * -> getSharedQueuedThreads
 * -> isOnSyncQueue
 * -> findNodeFromTail
 * -> transferForSignal
 * -> transferAfterCancelledWait
 * -> owns
 */
public abstract class AQSAnalyzer {

    /**
     * 参照`CLH`队列, 因为每个节点通过它的`prev`节点的`waitStatus`来决定自己是否应该被唤醒(获得执行权)
     */
    static final class Node {
        // 共享模式标记
        static final Node SHARED = new Node();
        // 互斥模式标记
        static final Node EXCLUSIVE = null;

        // waitStatus值: 表示该节点的线程被取消
        static final int CANCELLED = 1;
        // waitStatus值: 表示后继节点的线程需要被唤醒(unpark)
        static final int SIGNAL = -1;
        // waitStatus值: 表示该节点的线程正在等待条件, 即: 当前节点正处于条件队列当中(阻塞状态)
        static final int CONDITION = -2;
        // waitStatus值: 值对`head`节点设置, 表示`release`事件应该传播下去
        static final int PROPAGATE = -3;

        // 同步状态域: 取值如上
        volatile int waitStatus;

        // `同步队列`中的前驱节点, 应对被取消的节点或加入`同步队列`时三部曲的非原子性, 从`tail`向前遍历能保证遍历到刚加入的节点
        volatile Node prev;

        // `同步队列`中的后继节点, 方便向后遍历唤醒后继节点
        volatile Node next;

        // 节点持有的线程
        volatile Thread thread;

        // `条件队列`中下一个节点, 若为`EXCLUSIVE(null)`则表示未在`条件队列`中, 若为`SHARED`则表示共享模式
        Node nextWaiter;

        /**
         * 该节点是否是共享模式
         * @return
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

    }

    /*------------------------------ AQS成员变量: head, tail, state ------------------------------*/

    // 同步队列的头节点: 只能被setHead()方法修改, 且如果head节点存在, 就必须保证它的`waitStatus`不为`CANCELLED`
    private transient volatile Node head;

    // 同步队列的尾节点:
    private transient volatile Node tail;

    // 同步状态值
    private volatile int state;

    /*------------------------------ 同步状态(state)处理方法 ------------------------------*/

    /**
     * 返回state值
     *
     * @return
     */
    protected final int getState() {
        return state;
    }

    /**
     * 设置state值
     *
     * @param newState
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * 以`CAS`方式设置state值
     *
     * @param expect
     * @param update
     * @return
     */
    protected final boolean compareAndSetState(int expect, int update) {
        return true;
    }


    /*------------------------------ acquire ------------------------------*/

    /**
     * 以`独占`的方式获取锁
     *
     * @param arg
     */
    public final void acquire(int arg) {
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

    /**
     * 入队后以循环的方式获取锁
     *
     * @param node
     * @param arg
     * @return
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            // 循环获取锁, 一直进行尝试获取锁和阻塞的过程, 直到成功获取锁
            // 该循环只有一个显式的退出方式, 即: 成功获取锁, 否则只可能因为异常而退出
            for (; ; ) {
                final Node p = node.predecessor();
                // 若前驱节点是头节点, 且尝试获取锁成功, 则返回以执行后续的逻辑
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                // 本次循环未成功获取锁, 将前驱节点状态设为`SIGNAL`以便前驱节点唤醒自己来获取锁
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed) // 未成功获取到锁, 且因为异常退出循环, 则取消获取锁
                cancelAcquire(node);
        }
    }

    /**
     * 以`mode`模式创建新节点并加入`同步队列`
     *
     * @param mode
     * @return
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // 尝试快速入队: 1.将前驱节点指向`tail`节点, 2.将自己设置为`tail`节点, 3.将原`tail`节点的后继节点指向自己
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }

    /**
     * 节点加入同步队列: 加入的过程中可能会有释放操作, 因此在查找时有时从`tail`开始往前找, 该方法返回入队后的`前驱节点`
     * 1.将前驱节点指向`tail`节点
     * 2.将自己设置为`tail`节点
     * 3.将原`tail`节点的后继节点指向自己
     *
     * @param node
     * @return
     */
    private Node enq(final Node node) {
        // 使用自旋(循环)的方式加入队列
        for (; ; ) {
            Node t = tail;
            if (t == null) { // 若当前同步队列为空, 则创建一个新的空节点作为头节点
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * 当节点获取锁失败后进行处理: 返回`true`则表示该线程应该阻塞, 返回`false`则会继续循环尝试获取锁
     * 该方法是在`acquire`方法的循环中进行控制的主要方法
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        // 前驱节点的状态已经是`SIGNAL`, 则该前驱节点会通知本节点获取锁, 所以本节点可以进行阻塞
        if (ws == Node.SIGNAL)
            return true;
        // ----- 注意: 只要前驱节点的状态不是`SIGNAL`, 都会返回false, 表示不阻塞尝试一下获取

        // 前驱节点的状态大于0, 表示该前驱节点已经取消, 则向前找到第一个未取消的节点, 将该节点设置为自己的前驱节点
        if (ws > 0) {
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            // 将新的前驱节点的后继节点指向自己, 即: 中间被取消的节点都会被删除(解除队列的链接关系: pred, next)
            pred.next = node;
        } else {
            // 除了`大于0`或`SIGNAL`的情况, 要进入这个方法, 前置节点的`waitStatus`只可能是`0`或者`PROPAGATE`, 表示可以当前节点可以获取锁
            // 这里尝试将前置节点设置为`SIGNAL`, 并尝试再次获取锁
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * 取消尝试获取锁的节点
     *
     * @param node
     */
    private void cancelAcquire(Node node) {
        // 节点不存在则忽略
        if (node == null)
            return;
        // 将节点的`thread`设为null
        node.thread = null;
        // 跳过该节点之前已经取消的节点
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;
        // 将`前驱节点的后继节点`指向`自己的后继节点`, 即: 中间被取消的节点都被删除(包括自己)
        Node predNext = pred.next;
        // 将自己的状态设为取消`CANCELLED`
        node.waitStatus = Node.CANCELLED;
        // 如果该节点是`tail`节点, 则把前驱节点设置为新的`tail`节点
        if (node == tail && compareAndSetTail(node, pred)) {
            // 把前驱节点的后继节点设置为null
            compareAndSetNext(pred, predNext, null);
        } else {
            // 将前驱节点的状态设置为`SIGNAL`, 并把`前驱节点的后继节点`设置为`自己的后继节点`, 以便前驱节点可以唤醒后继节点
            int ws;
            if (pred != head &&
                    ((ws = pred.waitStatus) == Node.SIGNAL ||
                            (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                    pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                // 否则, 唤醒后继节点
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }

    /*------------------------------ release ------------------------------*/

    /**
     * 以独占的方式释放锁: head.waitStatus=SIGNAL
     *
     * @param arg
     * @return
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            // 独占模式下, 只有head的`waitStatus`不为0(此时应该是`SIGNAL`)才会唤醒后继节点
            if (h != null && h.waitStatus != 0)
                // `head`节点不为null, 唤醒`head`节点的后继节点
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    /**
     * 唤醒后继节点
     *
     * @param node
     */
    private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0) // 将当前节点的状态设置为0, 表示正在处理
            compareAndSetWaitStatus(node, ws, 0);

        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            // 后继节点为null或已被取消, 从`tail`节点开始往前搜索`当前节点`的`下一个节点`
            // 这是因为节点加入时会执行操作: 1.将前驱节点指向`tail`节点, 2.将自己设置为`tail`节点, 3.将原`tail`节点的后继节点指向自己
            // 有可能加入的节点执行完`步骤1`和`步骤2`, 但还没执行`步骤3`, 因此从`tail`节点往前搜索能保证刚加入的节点也被处理
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null) // 唤醒后继节点
            LockSupport.unpark(s.thread);
    }


    /*------------------------------ acquireShared/releaseShared ------------------------------*/

    /**
     * 以共享模式获取锁, 且忽略中断
     *
     * @param arg
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0) // tryAcquireShared返回值小于0表示获取锁失败
            doAcquireShared(arg);
    }

    /**
     * 以共享模式获取锁, 且忽略中断
     * 与`acquireQueued`的区别:
     * -> `acquireQueued`方法中: 调用`setHead()`, 即: 只有当前节点获取锁
     * -> `doAcquireShared`方法中: 调用`setHeadAndPropagate()`, 若当前节点成功获取锁, 且`tryAcquireShared`返回值大于0, 则继续唤醒后继节点
     * -> 因此: 在代码逻辑上, `独占`和`共享`模式的区别体现在: 节点从`同步队列`中成功获取到锁之后, 是否继续唤醒其后继节点
     *
     * @param arg
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head) { // 如果`前驱节点`是`head`节点, 则再次尝试获取锁
                    int r = tryAcquireShared(arg);
                    if (r >= 0) { // 成功获取锁且可以继续获取(若`r>0`)
                        // 设置当前节点为`head`节点, 且若`r>0`, 则继续唤醒后继节点
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 设置头节点, 并继续唤醒后继节点: 这里是`PROPAGATE`真正的使用场景
     * -> propagate>0: 还可以继续获取锁
     * -> waitStatus=PROPAGATE: 自己调用`tryAcquireShared()`之后有人调用了`releaseShared()`
     *      此时可能有其他节点`tryAcquireShared`失败正在入队, 而且还没来得及设置head的`waitStatus`为`SIGNAL`, 因此也需要在此时去唤醒该后继节点
     * @param node
     * @param propagate 调用`tryAcquireShared()`的返回值
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);
        // 当满足下列条件之一时, 继续唤醒后继节点:
        // 1.propagate > 0: 说明还可以继续获取锁, 此时应该唤醒后继节点
        // 2.`head`节点的`waitStatus`小于0: 此时应该是`PROPAGATE`
        // => 说明在自己调用`tryAcquireShared()`之后有人调用了`releaseShared()`, 因此在这里也需要继续唤醒后继节点
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
                (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            // 如果`后继节点`为null(防止此时有节点加入队列, 还没来得及将head的next指向自己)或者是`共享模式`, 则继续释放
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    /**
     * 共享模式释放锁
     *
     * @param arg
     * @return
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    /**
     * 共享模式下, head.waitStatus取值情况:
     * -> SIGNAL: 有后继节点需要唤醒
     * -> 0: 无后继节点需要唤醒, 需要将`head.waitStatus`设置为`PROPAGATE`, 并发的`doAcquireShared()`需要继续唤醒后继节点
     */
    private void doReleaseShared() {
        for (; ; ) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) { // 如果节点状态是`SIGNAL`, 说明应该唤醒其后继节点
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                } else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    // 如果`head.waitStatus=0`, 说明此时已经没有需要唤醒的后继节点了, 但此时可能有并发的入队动作, 因此将`head`节点设置为`PROPAGATE`, 表示应该继续唤醒
                    // 如果设置头节点为`PROPAGATE`失败则继续循环
                    continue;                // loop on failed CAS
            }
            if (h == head) // `head`不变, 说明已经处理完成
                break;
        }
    }

    /*------------------------------ 条件队列(Condition)处理方法 ------------------------------*/

    /**
     * 条件队列实现, 只能用于`独占方式`
     * 核心方法: await(), signal()/signalAll()
     */
    public class ConditionObject implements Condition, java.io.Serializable {
        // 条件队列中第一个节点
        private transient Node firstWaiter;
        // 条件队列中最后一个节点
        private transient Node lastWaiter;

        /**
         * 可中断的等待: 线程被中断时抛出`InterruptedException`异常
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted()) // 线程被中断则抛出`InterruptedException`异常
                throw new InterruptedException();
            // 把当前线程加入条件队列
            Node node = addConditionWaiter();
            // 完全释放锁: 因为锁时可重入的
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            // 如果没在`同步队列`中, 就休眠, 这是条件等待的核心逻辑
            // 注意: 节点从`条件队列`向`同步队列`转移时, 线程依然阻塞在这里
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                // 线程被唤醒: 可能有三种原因:
                // 1.在`条件队列`中, 其他节点调用`signal/signalAll`时失败, 出现: 1).前驱节点已被取消, 2).设置前驱节点状态为`SIGNAL`失败, 此时是直接调用`LockSupport.unpark()`唤醒了该线程
                // 2.在`同步队列`中, 前驱节点(pred)释放锁后唤醒了当前节点
                // 3.响应了`Thread.interrupt()`中断而被唤醒
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            // 线程被唤醒, 以独占的方式去获取锁
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            // 若`条件队列`中自己的下一个节点不为null, 则清理`条件队列`
            // 若线程在`signal`之前被中断, 则会出现`nextWaiter != null`的情况, 因此这里清理一下`条件队列`有利于去掉链接关系
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0) // 处理中断事件
                reportInterruptAfterWait(interruptMode);
        }

        /**
         * 当前节点加入条件队列
         *
         * @return
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // 如果`lastWaiter`节点已取消, 则遍历条件队列, 将所有已取消的节点删除
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter; // 将新的`lastWaiter`节点赋值给t
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null) // `lastWaiter`节点为空, 说明条件队列为空
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**
         * 完全释放节点: 释放节点已获取的所有状态值
         *
         * @param node
         * @return
         */
        final int fullyRelease(Node node) {
            boolean failed = true;
            try {
                int savedState = getState();
                if (release(savedState)) {
                    failed = false;
                    return savedState;
                } else {
                    throw new IllegalMonitorStateException();
                }
            } finally {
                if (failed)
                    node.waitStatus = Node.CANCELLED;
            }
        }

        /**
         * 节点是否已经在`同步队列`中
         * 节点从`条件队列`移除, 进入`同步队列`, 这个操作并非是原子性的
         *
         * @param node
         * @return
         */
        final boolean isOnSyncQueue(Node node) {
            // 节点状态是条件等待`CONDITION`或`prev`前驱节点为空, 则显然还没有进入`同步队列`
            if (node.waitStatus == Node.CONDITION || node.prev == null)
                return false;
            // 如果已经存在后继节点: 则已经有其他节点进入同步队列, 且已经将自己设置为前驱节点
            if (node.next != null) // If has successor, it must be on queue
                return true;
            // 从`tail`节点向前遍历, 查看节点是否已经在`同步队列`
            return findNodeFromTail(node);
        }

        /**
         * 从`tail`节点向前遍历, 查看节点是否已经在`同步队列`
         * 进入`同步队列`三部曲:
         * -> 1.将前驱节点指向`tail`节点
         * -> 2.将自己设置为`tail`节点
         * -> 3.将原`tail`节点的后继节点指向自己
         * 只进行到`步骤1`还不算进入`同步队列`, 因为无法被其前驱节点唤醒
         * 进行到`步骤2`时已经可以算入队成功, 因为前驱节点唤醒后继节点时, 若其`next`指针为null时, 会尝试从`tail`向前遍历找出后继节点
         *
         * @param node
         * @return
         */
        private boolean findNodeFromTail(Node node) {
            Node t = tail;
            for (; ; ) {
                if (t == node)
                    return true;
                if (t == null)
                    return false;
                t = t.prev;
            }
        }

        /**
         * 检查节点阻塞期间的中断状态:
         * -> 0: 未中断
         * -> THROW_IE: 发生了中断且节点状态为`CONDITION`, 说明节点在`signal()`调用之前发生了中断
         * -> REINTERRUPT: 发生了中断且节点状态不是`CONDITION`, 说明节点在`signal()`调用之后发生了中断
         *
         * @param node
         * @return
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                    (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                    0;
        }

        /**
         * 节点是否在被唤醒(调用`signal()`)之前中断
         */
        final boolean transferAfterCancelledWait(Node node) {
            // 如果CAS设置成功, 说明当前节点状态为`CONDITION`, 即: 该节点未被唤醒(signal/signalAll)就取消了等待
            if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
                enq(node); // 重新将节点加入同步队列
                return true;
            }
            // 当前节点在`signal/signalAll`之后被中断, 需要确保该节点已经处于同步队列之中
            while (!isOnSyncQueue(node)) // 由于节点状态已经不是`CONDITION`, 说明此时可能正在加入同步队列, 自旋等待即可
                Thread.yield();
            return false;
        }

        /**
         * 遍历条件队列, 将所有已取消的节点删除
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                } else
                    trail = t;
                t = next;
            }
        }

        /**
         * 等待中断之后的处理
         *
         * @param interruptMode
         * @throws InterruptedException
         */
        private void reportInterruptAfterWait(int interruptMode)
                throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * 重新设置当前线程为`中断状态`
         */
        private void selfInterrupt() {
            Thread.currentThread().interrupt();
        }

        /**
         * 唤醒条件队列第一个`未被取消`节点
         */
        public final void signal() {
            if (!isHeldExclusively()) // 如果当前线程未持有锁(独占模式), 则抛出`IllegalMonitorStateException`异常
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first); // 从条件队列头节点开始, 唤醒第一个`未被取消`节点
        }

        /**
         * 唤醒条件队列所有节点
         */
        public final void signalAll() {
            if (!isHeldExclusively()) // 如果当前线程未持有锁(独占模式), 则抛出`IllegalMonitorStateException`异常
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first); // 从`头节点`开始唤醒条件队列所有节点
        }

        /**
         * 唤醒`first`节点之后第一个`未被取消的节点`
         *
         * @param first
         */
        private void doSignal(Node first) {
            do {
                if ((firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) && // 从`条件队列`到`同步队列`转移节点失败(节点状态不是`CONDITION`), 则向后继续进行
                    (first = firstWaiter) != null);
        }

        /**
         * 唤醒条件队列中所有节点: 从`条件队列`转移到`同步队列`
         *
         * @param first
         */
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * 将节点从`条件队列`转移到`同步队列`
         * -> 注意: 转移只是节点的转移, 即: `prev`, `next`, `nextWaiter`指针的变化, 逻辑依然是在`await()`方法中阻塞
         *
         * @param node
         * @return
         */
        final boolean transferForSignal(Node node) {
            // CAS更新节点状态失败, 说明节点状态已经不是`CONDITION`, 即: 已取消
            if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
                return false;
            // 已将节点状态从`CONDITION`改为`0`, 且调用该方法时已经将节点的`nextWaiter`设为null, 即: 已经从`条件队列`出队
            // 节点加入`同步队列`, 返回入队后的`前驱节点`
            Node p = enq(node);
            int ws = p.waitStatus;
            // 如果: 1.前驱节点已被取消, 或者 2.设置前驱节点状态为`SIGNAL`失败, 则主动唤醒当前节点线程, 让它继续执行
            if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
                LockSupport.unpark(node.thread);
            return true;
        }

    }

    /*------------------------------ 其他方法 ------------------------------*/

    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

}
