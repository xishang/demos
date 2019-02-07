package com.demos.java.jdkanalyzer.concurrent;

import java.util.Collection;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/17
 *
 * ===== 核心方法
 * -> take(), poll(), peek()
 * -> put(), offer(), add()
 * -> remove()
 */
public class BlockingQueueAnalyzer {

    /*------------------------------ ArrayBlockingQueue ------------------------------*/
    // ===== 关键: ReentrantLock(fair), notEmpty, notFull, await(), awaitNanos()
    // ===== 元素数组: RingBuffer, takeIndex, putIndex, 但生产、消费数据使用同一个锁, 效率不高; 不同于`Disruptor`中的RingBuffer

    class ArrayBlockingQueue<E> {

        // 队列中的元素: RingBuffer
        final Object[] items;

        // 下一次获取元素的索引: take(), poll(), peek() or remove()
        int takeIndex;

        // 下一次加入元素的索引: put(), offer(), or add()
        int putIndex;

        // 队列中元素的个数
        int count;

        // 用来做同步控制
        final ReentrantLock lock;

        // 等待获取元素的`Condition`, 消费者等待
        private final Condition notEmpty;

        // 等待加入元素的`Condition`, 生产者等待
        private final Condition notFull;

        /**
         * 在`putIndex`处插入元素
         */
        private void enqueue(E x) {
            // assert lock.getHoldCount() == 1;
            // assert items[putIndex] == null;
            final Object[] items = this.items;
            items[putIndex] = x;
            if (++putIndex == items.length) // RingBuffer
                putIndex = 0;
            count++;
            notEmpty.signal(); // 通知消费者
        }

        /**
         * 在`takeIndex`处消费元素
         * @return
         */
        private E dequeue() {
            // assert lock.getHoldCount() == 1;
            // assert items[takeIndex] != null;
            final Object[] items = this.items;
            @SuppressWarnings("unchecked")
            E x = (E) items[takeIndex];
            items[takeIndex] = null;
            if (++takeIndex == items.length) // RingBuffer
                takeIndex = 0;
            count--;
            if (itrs != null)
                itrs.elementDequeued();
            notFull.signal(); // 通知生产者
            return x;
        }

        // 默认以非公平的方式等待
        public ArrayBlockingQueue(int capacity) { this(capacity, false); }

        /**
         * 可以选择以`公平`或`非公平`的方式等待: `new ReentrantLock(fair)`
         */
        public ArrayBlockingQueue(int capacity, boolean fair) {
            if (capacity <= 0)
                throw new IllegalArgumentException();
            this.items = new Object[capacity];
            lock = new ReentrantLock(fair);
            notEmpty = lock.newCondition();
            notFull =  lock.newCondition();
        }

        /* ========== 添加元素的方法 ========== */
        /**
         * 添加元素(立即返回): 该方法会立即返回, 实际调用的是`offer(e)`, 如果队列已满会抛出`IllegalStateException`异常
         */
        public boolean add(E e) {
            return super.add(e);
        }

        /**
         * 添加元素(立即返回): 该方法会立即返回, 如果队列已满则返回false
         */
        public boolean offer(E e) {
            checkNotNull(e);
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (count == items.length)
                    return false; // 如果队列已满则返回false
                else {
                    enqueue(e);
                    return true;
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * 添加元素(等待): 如果队列已满则会阻塞等待
         */
        public void put(E e) throws InterruptedException {
            checkNotNull(e);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly(); // 加锁: 保护条件谓词
            try {
                while (count == items.length) // 这就是条件谓词
                    notFull.await(); // 只要队列还是满的, 就再次等待
                enqueue(e);
            } finally {
                lock.unlock();
            }
        }

        /**
         * 添加元素(等待): 如果队列已满则会进行超时等待
         */
        public boolean offer(E e, long timeout, TimeUnit unit)
                throws InterruptedException {

            checkNotNull(e);
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                while (count == items.length) {
                    if (nanos <= 0)
                        return false;
                    nanos = notFull.awaitNanos(nanos); // 如果队列已满, 且等待时间大于0, 则超时等待
                }
                enqueue(e);
                return true;
            } finally {
                lock.unlock();
            }
        }


        /* ========== 消费元素的方法 ========== */

        /**
         * 消费元素(立即返回): 如果队列为空则返回null
         * @return
         */
        public E poll() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return (count == 0) ? null : dequeue(); // 队列为空则返回null
            } finally {
                lock.unlock();
            }
        }

        /**
         * 消费元素(等待): 以阻塞等待的方式获取元素
         */
        public E take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                while (count == 0) // 如果当前队列为空, 则在`notEmpty`上等待
                    notEmpty.await();
                return dequeue();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 消费元素(等待): 若队列为空则会进行超时等待
         */
        public E poll(long timeout, TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                while (count == 0) {
                    if (nanos <= 0) // 如果当前队列为空, 且超时时间小于等于0, 直接返回null
                        return null;
                    nanos = notEmpty.awaitNanos(nanos); // 如果当前队列为空, 且超时时间大于0, 超时等待
                }
                return dequeue();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 返回`takeIndex`位置上的元素(立即返回): 如果队列为空则返回null
         * 与其他`消费元素`的方法不同的是: `peek()`方法不会移除`takeIndex`位置上的元素
         * @return
         */
        public E peek() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return itemAt(takeIndex); // null when queue is empty
            } finally {
                lock.unlock();
            }
        }

    }

    /*------------------------------ ArrayBlockingQueue ------------------------------*/
    // 双向链表: 可以同时在表头和表尾操作, 即: 可以同时消费元素和添加元素
    // head, last, takeLock, putLock, notEmpty, notFull
    // 避免竞争: 使用`signal()`每次唤醒一个生产者或消费者

    class LinkedBlockingQueue<E> {

        class Node<E> {
            E item;

            Node<E> next;

            Node(E x) { item = x; }
        }

        // 链表的容量, 默认为: Integer.MAX_VALUE
        private final int capacity;

        // 当前队列元素个数
        private final AtomicInteger count = new AtomicInteger();

        // 链表的头节点: head.item == null, 第一个元素为`head.next.item`
        transient Node<E> head;

        // 链表的尾节点: last.next == null, 最后一个元素为`last.item`
        private transient Node<E> last;

        // 获取元素的操作持有的锁
        private final ReentrantLock takeLock = new ReentrantLock();

        // `take`操作等待的条件
        private final Condition notEmpty = takeLock.newCondition();

        // 添加元素的操作持有的锁
        private final ReentrantLock putLock = new ReentrantLock();

        // `put`操作等待的条件
        private final Condition notFull = putLock.newCondition();

        /**
         * 唤醒消费者: 为避免竞争, 每次只唤醒一个消费者, 在`take`操作时如果队列不为空, 会继续唤醒其他消费者
         */
        private void signalNotEmpty() {
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lock();
            try {
                notEmpty.signal(); // 只唤醒一个消费者: 避免竞争
            } finally {
                takeLock.unlock();
            }
        }

        /**
         * 唤醒生产者: 为避免竞争, 每次只唤醒一个生产者, 在`put`操作时如果队列未满, 会继续唤醒其他生产者
         */
        private void signalNotFull() {
            final ReentrantLock putLock = this.putLock;
            putLock.lock();
            try {
                notFull.signal(); // 只唤醒一个生产者: 避免竞争
            } finally {
                putLock.unlock();
            }
        }

        /**
         * 在队尾添加节点
         */
        private void enqueue(Node<E> node) {
            // assert putLock.isHeldByCurrentThread();
            // assert last.next == null;
            last = last.next = node;
        }

        /**
         * 在表头移除节点: 消费节点
         */
        private E dequeue() {
            // assert takeLock.isHeldByCurrentThread();
            // assert head.item == null;
            Node<E> h = head;
            Node<E> first = h.next; // `head.next`是第一个元素
            h.next = h; // help GC
            head = first;
            E x = first.item;
            first.item = null;
            return x;
        }

        /**
         * 默认构造器: 队列容量为`Integer.MAX_VALUE`
         */
        public LinkedBlockingQueue() {
            this(Integer.MAX_VALUE);
        }

        /**
         * 指定`capacity`为队列容量
         * @param capacity
         */
        public LinkedBlockingQueue(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException();
            this.capacity = capacity;
            last = head = new Node<E>(null);
        }

        /**
         * 队列中当前元素个数
         */
        public int size() {
            return count.get();
        }

        /* ========== 添加元素的方法 ========== */
        /**
         * 添加元素(等待): 如果队列已满则阻塞等待
         */
        public void put(E e) throws InterruptedException {
            if (e == null) throw new NullPointerException();
            int c = -1; // 初始为-1, 如果添加元素失败则不会唤醒消费者
            Node<E> node = new Node<E>(e);
            final ReentrantLock putLock = this.putLock;
            final AtomicInteger count = this.count;
            putLock.lockInterruptibly(); // 对`putLock`加锁
            try {
                while (count.get() == capacity) { // 队列已满则阻塞等待
                    notFull.await();
                }
                enqueue(node); // 入队
                c = count.getAndIncrement(); // 队列元素个数加1
                if (c + 1 < capacity) // 如果未到容量大小, 继续唤醒其他生产者(由于在`signalNotFull()`只会唤醒一个生产者)
                    notFull.signal();
            } finally {
                putLock.unlock();
            }
            if (c == 0) // c == 0说明添加元素前队列是空的, 所以这里需要唤醒一个消费者, 并由消费者继续唤醒其他消费者
                signalNotEmpty();
        }

        /**
         * 添加元素(等待): 如果队列已满则超时等待
         */
        public boolean offer(E e, long timeout, TimeUnit unit)
                throws InterruptedException {

            if (e == null) throw new NullPointerException();
            long nanos = unit.toNanos(timeout);
            int c = -1;
            final ReentrantLock putLock = this.putLock;
            final AtomicInteger count = this.count;
            putLock.lockInterruptibly(); // 对`putLock`加锁
            try {
                while (count.get() == capacity) {
                    if (nanos <= 0)
                        return false;
                    nanos = notFull.awaitNanos(nanos); // 如果队列已满且超时时间大于0, 则进入超时等待
                }
                enqueue(new Node<E>(e)); // 元素加入队尾
                c = count.getAndIncrement();
                if (c + 1 < capacity) // 继续唤醒一个其他生产者
                    notFull.signal();
            } finally {
                putLock.unlock();
            }
            if (c == 0) // c == 0说明添加元素前队列是空的, 唤醒一个消费者
                signalNotEmpty();
            return true;
        }

        /**
         * 添加元素(立即返回): 如果队列已满则返回false
         */
        public boolean offer(E e) {
            if (e == null) throw new NullPointerException();
            final AtomicInteger count = this.count;
            if (count.get() == capacity) // 队列已满则返回false
                return false;
            int c = -1;
            Node<E> node = new Node<E>(e);
            final ReentrantLock putLock = this.putLock;
            putLock.lock(); // 对`putLock`加锁
            try {
                if (count.get() < capacity) { // 如果队列未满则添加元素
                    enqueue(node);
                    c = count.getAndIncrement();
                    if (c + 1 < capacity) // 继续唤醒一个其他生产者
                        notFull.signal();
                }
            } finally {
                putLock.unlock();
            }
            if (c == 0) // c == 0说明添加元素前队列是空的, 唤醒一个消费者
                signalNotEmpty();
            return c >= 0; // c >= 0: 添加成功, -1: 添加失败
        }


        /* ========== 消费元素的方法 ========== */
        /**
         * 消费元素(等待): 以阻塞等待的方式获取元素
         */
        public E take() throws InterruptedException {
            E x;
            int c = -1;
            final AtomicInteger count = this.count;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lockInterruptibly(); // 获取`takeLock`
            try {
                while (count.get() == 0) { // 队列为空则阻塞等待
                    notEmpty.await();
                }
                x = dequeue(); // 消费队列元素
                c = count.getAndDecrement(); // 元素个数减1
                if (c > 1) // 如果还有元素, 则继续唤醒下一个消费者
                    notEmpty.signal();
            } finally {
                takeLock.unlock();
            }
            if (c == capacity) // 消费元素之前队列是满的, 唤醒一个生产者
                signalNotFull();
            return x;
        }

        /**
         * 消费元素(等待): 以超时等待的方式获取元素
         */
        public E poll(long timeout, TimeUnit unit) throws InterruptedException {
            E x = null;
            int c = -1;
            long nanos = unit.toNanos(timeout);
            final AtomicInteger count = this.count;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lockInterruptibly(); // 获取`takeLock`
            try {
                while (count.get() == 0) {
                    if (nanos <= 0) // 队列为空且超时时间小于0, 直接返回返回null
                        return null;
                    nanos = notEmpty.awaitNanos(nanos); // 超时等待
                }
                x = dequeue(); // 消费队列元素
                c = count.getAndDecrement();
                if (c > 1) // 如果还有元素, 则继续唤醒下一个消费者
                    notEmpty.signal();
            } finally {
                takeLock.unlock();
            }
            if (c == capacity) // 消费元素之前队列是满的, 唤醒一个生产者
                signalNotFull();
            return x;
        }

        /**
         * 消费元素(立即返回): 队列为空则返回null
         */
        public E poll() {
            final AtomicInteger count = this.count;
            if (count.get() == 0)
                return null;
            E x = null;
            int c = -1;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lock(); // 获取`takeLock`
            try {
                if (count.get() > 0) {
                    x = dequeue(); // 消费队列元素
                    c = count.getAndDecrement();
                    if (c > 1) // 如果还有元素, 则继续唤醒下一个消费者
                        notEmpty.signal();
                }
            } finally {
                takeLock.unlock();
            }
            if (c == capacity) // 消费元素之前队列是满的, 唤醒一个生产者
                signalNotFull();
            return x;
        }

        /**
         * 返回队头的元素: 不移除该元素, 队列为空则返回null
         */
        public E peek() {
            if (count.get() == 0) // 如果队列为空则直接返回null
                return null;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lock();
            try {
                Node<E> first = head.next;
                if (first == null) // head.next == null, 说明此时队列为空, 返回null
                    return null;
                else
                    return first.item; // 否则直接返回第一个元素, 且不移除该元素
            } finally {
                takeLock.unlock();
            }
        }

    }

    /*------------------------------ SynchronousQueue ------------------------------*/

    static class SynchronousQueue<E> {
        /**
         * Shared internal API for dual stacks and queues.
         */
        abstract static class Transferer<E> {
            /**
             * Performs a put or take.
             * 参数`e`为null时表明当前线程是一个消费者, 否则表明当前线程是一个生产者
             */
            abstract E transfer(E e, boolean timed, long nanos);
        }

        // 及其CPU数, 用来做自旋控制
        static final int NCPUS = Runtime.getRuntime().availableProcessors();

        // 线程超时等待之前的自旋次数: 如果是`单处理器`则不自旋, 否则最多自旋32次
        static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32;

        // 线程阻塞等待之前的自旋次数: 比超时等待的自旋次数要大, 因为阻塞等待之前的自旋不需要检查超时条件, 所以会更快一些
        static final int maxUntimedSpins = maxTimedSpins * 16;

        /**
         * The number of nanoseconds for which it is faster to spin
         * rather than to use timed park. A rough estimate suffices.
         */
        static final long spinForTimeoutThreshold = 1000L;

        /** Dual stack */
        static final class TransferStack<E> extends Transferer<E> {
        /*
         * This extends Scherer-Scott dual stack algorithm, differing,
         * among other ways, by using "covering" nodes rather than
         * bit-marked pointers: Fulfilling operations push on marker
         * nodes (with FULFILLING bit set in mode) to reserve a spot
         * to match a waiting node.
         */

        /* Modes for SNodes, ORed together in node fields */
            // 表示一个没有得到数据的消费者节点
            static final int REQUEST    = 0;
            // 表示一个没有交出数据的生产者节点
            static final int DATA       = 1;
            // 表示正在匹配另一个生产者或者消费者的节点
            static final int FULFILLING = 2;

            // 判断是否正在等待的标记
            static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; }

            /* ---------- TransferStack的`Node`类定义 start ---------- */
            static final class SNode {
                volatile SNode next; // 栈中的下一个节点
                volatile SNode match; // 跟当前节点完成匹配的节点
                volatile Thread waiter; // 当前节点的线程, 用来控制`park/unpark`
                Object item; // null: 消费者, non-null: 生产者的数据
                int mode; // 节点模式: REQUEST or DATA
                // `item`和`mode`域不需要声明为`volatile`, 因为他们总是在其他的`volatile/atomic`操作之前写, 之后读

                SNode(Object item) {
                    this.item = item;
                }

                /**
                 * 将当前节点的`next`域设置为`val`节点
                 */
                boolean casNext(SNode cmp, SNode val) {
                    return cmp == next && UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
                }

                /**
                 * 尝试匹配`s`节点: 将当前节点的`match`域设置为`s`节点
                 */
                boolean tryMatch(SNode s) {
                    // 当前节点的`match`域为null, 且设置当前节点的`match`域为`s`节点成功
                    if (match == null && UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) {
                        Thread w = waiter;
                        if (w != null) {    // waiters need at most one unpark
                            waiter = null; // help GC
                            LockSupport.unpark(w); // 唤醒当前节点的线程
                        }
                        return true;
                    }
                    return match == s; // 否则返回`match`是否已经设置`s`节点, 即: 是否已经完成匹配
                }

                /**
                 * 尝试取消当前节点: 将`match`域设置为自身
                 */
                void tryCancel() {
                    UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
                }

                boolean isCancelled() {
                    return match == this;
                }

                // Unsafe mechanics
                private static final sun.misc.Unsafe UNSAFE;
                private static final long matchOffset;
                private static final long nextOffset;

                static {
                    try {
                        UNSAFE = sun.misc.Unsafe.getUnsafe();
                        Class<?> k = SNode.class;
                        matchOffset = UNSAFE.objectFieldOffset
                                (k.getDeclaredField("match"));
                        nextOffset = UNSAFE.objectFieldOffset
                                (k.getDeclaredField("next"));
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
            }
            /* ---------- TransferStack的`Node`类定义 end ---------- */

            // `TransferStack`栈顶元素: 首先匹配的节点
            volatile SNode head;

            /**
             * 将栈顶元素设置为节点`nh`
             */
            boolean casHead(SNode h, SNode nh) {
                return h == head &&
                        UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
            }

            /**
             * Creates or resets fields of a node. Called only from transfer
             * where the node to push on stack is lazily created and
             * reused when possible to help reduce intervals between reads
             * and CASes of head and to avoid surges of garbage when CASes
             * to push nodes fail due to contention.
             */
            static SNode snode(SNode s, Object e, SNode next, int mode) {
                if (s == null) s = new SNode(e);
                s.mode = mode;
                s.next = next;
                return s;
            }

            /**
             * Puts or takes an item.
             */
            @SuppressWarnings("unchecked")
            E transfer(E e, boolean timed, long nanos) {
            /*
             * Basic algorithm is to loop trying one of three actions:
             *
             * 1. If apparently empty or already containing nodes of same
             *    mode, try to push node on stack and wait for a match,
             *    returning it, or null if cancelled.
             *
             * 2. If apparently containing node of complementary mode,
             *    try to push a fulfilling node on to stack, match
             *    with corresponding waiting node, pop both from
             *    stack, and return matched item. The matching or
             *    unlinking might not actually be necessary because of
             *    other threads performing action 3:
             *
             * 3. If top of stack already holds another fulfilling node,
             *    help it out by doing its match and/or pop
             *    operations, and then continue. The code for helping
             *    is essentially the same as for fulfilling, except
             *    that it doesn't return the item.
             */

                // 因为要自旋进行尝试, 为了避免重复创建节点, 重用s
                SNode s = null; // constructed/reused as needed
                int mode = (e == null) ? REQUEST : DATA; // 如果参数`e`为空则为消费者模式, 否则`e`为生产者的数据

                for (;;) {
                    SNode h = head;
                    if (h == null || h.mode == mode) { // 栈为空或者`h`跟当前节点的模式相同: 把当前节点加入栈顶
                        if (timed && nanos <= 0) { // 超时等待且等待时间小于0: 不需要等待
                            if (h != null && h.isCancelled())
                                casHead(h, h.next); // 如果`h`不为空且已经取消, 弹出`h`
                            else
                                return null; // 否则直接返回null
                        } else if (casHead(h, s = snode(s, e, h, mode))) {
                            // 如果需要等待, 尝试把`head`节点设置为当前节点, 使用`snode`方法更新节点, 避免重复创建节点造成浪费
                            // 如果调用`cahHead()`将当前节点设置为`head`失败, 则会自旋重试
                            SNode m = awaitFulfill(s, timed, nanos); // 主要的等待逻辑(自旋或阻塞), 返回节点为已匹配的节点或自身(表示已取消)
                            if (m == s) { // m == s: 返回了自身, 当前节点已取消
                                clean(s); // 清理当前节点: 主要是清理链接
                                return null;
                            }
                            // 到这里说明已经返回了匹配的节点
                            if ((h = head) != null && h.next == s)
                                // 之前已经将`head`设置成了自身, 如果此时`head.next`等于自身, 此时的`head`是自己的匹配节点
                                casHead(h, s.next); // 将自己的`next`节点设置为新的`head`节点, 即: 自己和匹配节点一起出队了
                            return (E) ((mode == REQUEST) ? m.item : s.item); // 如果是消费者模式则返回匹配节点的数据, 否则返回自己的数据
                        }
                    } else if (!isFulfilling(h.mode)) { // 到这里: 说明栈顶元素`h`没有在匹配, 并且`h`跟自己是不同模式, 可以尝试匹配`h`
                        if (h.isCancelled()) // 如果`h`已经取消了就把`h`的下一个节点设置为`head`
                            casHead(h, h.next);         // pop and retry
                        else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) { // 否则: 先将自己设置为`head`, 并把模式设置为`正在匹配`
                            for (;;) { // loop until matched or waiters disappear
                                SNode m = s.next; // `m`是当前节点`s`的`next`节点, 他们正在匹配
                                if (m == null) { // 如果`m`被其他节点匹配走了, 将`s`置为null, 回到主循环再来一次
                                    casHead(s, null); // 弹出当前节点`s`
                                    s = null; // 创建一个新的节点再来一次
                                    break; // 回到主循环重新开始
                                }
                                SNode mn = m.next;
                                if (m.tryMatch(s)) { // 否则, 尝试匹配`m`, 将自己设置为`m`的`match`域, 且会唤醒`m`的线程
                                    casHead(s, mn); // 将自己和`m`都弹出栈, 将`m`的下一个节点设置为`head`
                                    return (E) ((mode == REQUEST) ? m.item : s.item); // 如果是消费者模式则返回匹配节点的数据, 否则返回自己的数据
                                } else // 如果匹配失败, 则将自己的`next`域设置为`m`的下一个节点, 继续尝试匹配
                                    s.casNext(m, mn);
                            }
                        }
                    } else { // 到这里说明栈顶元素正在匹配: 由于匹配总是栈顶的两个元素匹配, 因此这里需要设置新的`head`
                        SNode m = h.next; // 将栈顶两个元素进行匹配
                        if (m == null) // h.next为空, 匹配失败
                            casHead(h, null);           // pop fulfilling node
                        else {
                            SNode mn = m.next;
                            if (m.tryMatch(h))          // help match
                                casHead(h, mn);         // pop both h and m
                            else                        // lost match
                                h.casNext(m, mn);       // help unlink
                        }
                    }
                }
            }

            /**
             * Spins/blocks until node s is matched by a fulfill operation.
             *
             * @param s the waiting node
             * @param timed true if timed wait
             * @param nanos timeout value
             * @return matched node, or s if cancelled
             */
            SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            /*
             * When a node/thread is about to block, it sets its waiter
             * field and then rechecks state at least one more time
             * before actually parking, thus covering race vs
             * fulfiller noticing that waiter is non-null so should be
             * woken.
             *
             * When invoked by nodes that appear at the point of call
             * to be at the head of the stack, calls to park are
             * preceded by spins to avoid blocking when producers and
             * consumers are arriving very close in time.  This can
             * happen enough to bother only on multiprocessors.
             *
             * The order of checks for returning out of main loop
             * reflects fact that interrupts have precedence over
             * normal returns, which have precedence over
             * timeouts. (So, on timeout, one last check for match is
             * done before giving up.) Except that calls from untimed
             * SynchronousQueue.{poll/offer} don't check interrupts
             * and don't wait at all, so are trapped in transfer
             * method rather than calling awaitFulfill.
             */
                final long deadline = timed ? System.nanoTime() + nanos : 0L;
                Thread w = Thread.currentThread();
                int spins = (shouldSpin(s) ?
                        (timed ? maxTimedSpins : maxUntimedSpins) : 0);
                for (;;) {
                    if (w.isInterrupted())
                        s.tryCancel();
                    SNode m = s.match;
                    if (m != null)
                        return m;
                    if (timed) {
                        nanos = deadline - System.nanoTime();
                        if (nanos <= 0L) {
                            s.tryCancel();
                            continue;
                        }
                    }
                    if (spins > 0)
                        spins = shouldSpin(s) ? (spins-1) : 0;
                    else if (s.waiter == null)
                        s.waiter = w; // establish waiter so can park next iter
                    else if (!timed)
                        LockSupport.park(this);
                    else if (nanos > spinForTimeoutThreshold)
                        LockSupport.parkNanos(this, nanos);
                }
            }

            /**
             * Returns true if node s is at head or there is an active
             * fulfiller.
             */
            boolean shouldSpin(SNode s) {
                SNode h = head;
                return (h == s || h == null || isFulfilling(h.mode));
            }

            /**
             * Unlinks s from the stack.
             */
            void clean(SNode s) {
                s.item = null;   // forget item
                s.waiter = null; // forget thread

            /*
             * At worst we may need to traverse entire stack to unlink
             * s. If there are multiple concurrent calls to clean, we
             * might not see s if another thread has already removed
             * it. But we can stop when we see any node known to
             * follow s. We use s.next unless it too is cancelled, in
             * which case we try the node one past. We don't check any
             * further because we don't want to doubly traverse just to
             * find sentinel.
             */

                SNode past = s.next;
                if (past != null && past.isCancelled())
                    past = past.next;

                // Absorb cancelled nodes at head
                SNode p;
                while ((p = head) != null && p != past && p.isCancelled())
                    casHead(p, p.next);

                // Unsplice embedded nodes
                while (p != null && p != past) {
                    SNode n = p.next;
                    if (n != null && n.isCancelled())
                        p.casNext(n, n.next);
                    else
                        p = n;
                }
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long headOffset;
            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = TransferStack.class;
                    headOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("head"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** Dual Queue */
        static final class TransferQueue<E> extends Transferer<E> {
        /*
         * This extends Scherer-Scott dual queue algorithm, differing,
         * among other ways, by using modes within nodes rather than
         * marked pointers. The algorithm is a little simpler than
         * that for stacks because fulfillers do not need explicit
         * nodes, and matching is done by CAS'ing QNode.item field
         * from non-null to null (for put) or vice versa (for take).
         */

            /** Node class for TransferQueue. */
            static final class QNode {
                volatile QNode next;          // next node in queue
                volatile Object item;         // CAS'ed to or from null
                volatile Thread waiter;       // to control park/unpark
                final boolean isData;

                QNode(Object item, boolean isData) {
                    this.item = item;
                    this.isData = isData;
                }

                boolean casNext(QNode cmp, QNode val) {
                    return next == cmp &&
                            UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
                }

                boolean casItem(Object cmp, Object val) {
                    return item == cmp &&
                            UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
                }

                /**
                 * Tries to cancel by CAS'ing ref to this as item.
                 */
                void tryCancel(Object cmp) {
                    UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
                }

                boolean isCancelled() {
                    return item == this;
                }

                /**
                 * Returns true if this node is known to be off the queue
                 * because its next pointer has been forgotten due to
                 * an advanceHead operation.
                 */
                boolean isOffList() {
                    return next == this;
                }

                // Unsafe mechanics
                private static final sun.misc.Unsafe UNSAFE;
                private static final long itemOffset;
                private static final long nextOffset;

                static {
                    try {
                        UNSAFE = sun.misc.Unsafe.getUnsafe();
                        Class<?> k = QNode.class;
                        itemOffset = UNSAFE.objectFieldOffset
                                (k.getDeclaredField("item"));
                        nextOffset = UNSAFE.objectFieldOffset
                                (k.getDeclaredField("next"));
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
            }

            /** Head of queue */
            transient volatile QNode head;
            /** Tail of queue */
            transient volatile QNode tail;
            /**
             * Reference to a cancelled node that might not yet have been
             * unlinked from queue because it was the last inserted node
             * when it was cancelled.
             */
            transient volatile QNode cleanMe;

            TransferQueue() {
                QNode h = new QNode(null, false); // initialize to dummy node.
                head = h;
                tail = h;
            }

            /**
             * Tries to cas nh as new head; if successful, unlink
             * old head's next node to avoid garbage retention.
             */
            void advanceHead(QNode h, QNode nh) {
                if (h == head &&
                        UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                    h.next = h; // forget old next
            }

            /**
             * Tries to cas nt as new tail.
             */
            void advanceTail(QNode t, QNode nt) {
                if (tail == t)
                    UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
            }

            /**
             * Tries to CAS cleanMe slot.
             */
            boolean casCleanMe(QNode cmp, QNode val) {
                return cleanMe == cmp &&
                        UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
            }

            /**
             * Puts or takes an item.
             */
            @SuppressWarnings("unchecked")
            E transfer(E e, boolean timed, long nanos) {
            /* Basic algorithm is to loop trying to take either of
             * two actions:
             *
             * 1. If queue apparently empty or holding same-mode nodes,
             *    try to add node to queue of waiters, wait to be
             *    fulfilled (or cancelled) and return matching item.
             *
             * 2. If queue apparently contains waiting items, and this
             *    call is of complementary mode, try to fulfill by CAS'ing
             *    item field of waiting node and dequeuing it, and then
             *    returning matching item.
             *
             * In each case, along the way, check for and try to help
             * advance head and tail on behalf of other stalled/slow
             * threads.
             *
             * The loop starts off with a null check guarding against
             * seeing uninitialized head or tail values. This never
             * happens in current SynchronousQueue, but could if
             * callers held non-volatile/final ref to the
             * transferer. The check is here anyway because it places
             * null checks at top of loop, which is usually faster
             * than having them implicitly interspersed.
             */

                QNode s = null; // constructed/reused as needed
                boolean isData = (e != null);

                for (;;) {
                    QNode t = tail;
                    QNode h = head;
                    if (t == null || h == null)         // saw uninitialized value
                        continue;                       // spin

                    if (h == t || t.isData == isData) { // empty or same-mode
                        QNode tn = t.next;
                        if (t != tail)                  // inconsistent read
                            continue;
                        if (tn != null) {               // lagging tail
                            advanceTail(t, tn);
                            continue;
                        }
                        if (timed && nanos <= 0)        // can't wait
                            return null;
                        if (s == null)
                            s = new QNode(e, isData);
                        if (!t.casNext(null, s))        // failed to link in
                            continue;

                        advanceTail(t, s);              // swing tail and wait
                        Object x = awaitFulfill(s, e, timed, nanos);
                        if (x == s) {                   // wait was cancelled
                            clean(t, s);
                            return null;
                        }

                        if (!s.isOffList()) {           // not already unlinked
                            advanceHead(t, s);          // unlink if head
                            if (x != null)              // and forget fields
                                s.item = s;
                            s.waiter = null;
                        }
                        return (x != null) ? (E)x : e;

                    } else {                            // complementary-mode
                        QNode m = h.next;               // node to fulfill
                        if (t != tail || m == null || h != head)
                            continue;                   // inconsistent read

                        Object x = m.item;
                        if (isData == (x != null) ||    // m already fulfilled
                                x == m ||                   // m cancelled
                                !m.casItem(x, e)) {         // lost CAS
                            advanceHead(h, m);          // dequeue and retry
                            continue;
                        }

                        advanceHead(h, m);              // successfully fulfilled
                        LockSupport.unpark(m.waiter);
                        return (x != null) ? (E)x : e;
                    }
                }
            }

            /**
             * Spins/blocks until node s is fulfilled.
             *
             * @param s the waiting node
             * @param e the comparison value for checking match
             * @param timed true if timed wait
             * @param nanos timeout value
             * @return matched item, or s if cancelled
             */
            Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            /* Same idea as TransferStack.awaitFulfill */
                final long deadline = timed ? System.nanoTime() + nanos : 0L;
                Thread w = Thread.currentThread();
                int spins = ((head.next == s) ?
                        (timed ? maxTimedSpins : maxUntimedSpins) : 0);
                for (;;) {
                    if (w.isInterrupted())
                        s.tryCancel(e);
                    Object x = s.item;
                    if (x != e)
                        return x;
                    if (timed) {
                        nanos = deadline - System.nanoTime();
                        if (nanos <= 0L) {
                            s.tryCancel(e);
                            continue;
                        }
                    }
                    if (spins > 0)
                        --spins;
                    else if (s.waiter == null)
                        s.waiter = w;
                    else if (!timed)
                        LockSupport.park(this);
                    else if (nanos > spinForTimeoutThreshold)
                        LockSupport.parkNanos(this, nanos);
                }
            }

            /**
             * Gets rid of cancelled node s with original predecessor pred.
             */
            void clean(QNode pred, QNode s) {
                s.waiter = null; // forget thread
            /*
             * At any given time, exactly one node on list cannot be
             * deleted -- the last inserted node. To accommodate this,
             * if we cannot delete s, we save its predecessor as
             * "cleanMe", deleting the previously saved version
             * first. At least one of node s or the node previously
             * saved can always be deleted, so this always terminates.
             */
                while (pred.next == s) { // Return early if already unlinked
                    QNode h = head;
                    QNode hn = h.next;   // Absorb cancelled first node as head
                    if (hn != null && hn.isCancelled()) {
                        advanceHead(h, hn);
                        continue;
                    }
                    QNode t = tail;      // Ensure consistent read for tail
                    if (t == h)
                        return;
                    QNode tn = t.next;
                    if (t != tail)
                        continue;
                    if (tn != null) {
                        advanceTail(t, tn);
                        continue;
                    }
                    if (s != t) {        // If not tail, try to unsplice
                        QNode sn = s.next;
                        if (sn == s || pred.casNext(s, sn))
                            return;
                    }
                    QNode dp = cleanMe;
                    if (dp != null) {    // Try unlinking previous cancelled node
                        QNode d = dp.next;
                        QNode dn;
                        if (d == null ||               // d is gone or
                                d == dp ||                 // d is off list or
                                !d.isCancelled() ||        // d not cancelled or
                                (d != t &&                 // d not tail and
                                        (dn = d.next) != null &&  //   has successor
                                        dn != d &&                //   that is on list
                                        dp.casNext(d, dn)))       // d unspliced
                            casCleanMe(dp, null);
                        if (dp == pred)
                            return;      // s is already saved node
                    } else if (casCleanMe(null, pred))
                        return;          // Postpone cleaning s
                }
            }

            private static final sun.misc.Unsafe UNSAFE;
            private static final long headOffset;
            private static final long tailOffset;
            private static final long cleanMeOffset;
            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = TransferQueue.class;
                    headOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("head"));
                    tailOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("tail"));
                    cleanMeOffset = UNSAFE.objectFieldOffset
                            (k.getDeclaredField("cleanMe"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /**
         * The transferer. Set only in constructor, but cannot be declared
         * as final without further complicating serialization.  Since
         * this is accessed only at most once per public method, there
         * isn't a noticeable performance penalty for using volatile
         * instead of final here.
         */
        // 核心: 所有的`takes`和`puts`操作都是通过`Transferer.transfer()`完成
        private transient volatile Transferer<E> transferer;

        /**
         * 默认为非公平模式: TransferStack
         */
        public SynchronousQueue() {
            this(false);
        }

        /**
         * 创建SynchronousQueue: 公平模式(队列), 非公平模式(栈)
         */
        public SynchronousQueue(boolean fair) {
            transferer = fair ? new TransferQueue<E>() : new TransferStack<E>();
        }

        /**
         * Adds the specified element to this queue, waiting if necessary for
         * another thread to receive it.
         *
         * @throws InterruptedException {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        public void put(E e) throws InterruptedException {
            if (e == null) throw new NullPointerException();
            if (transferer.transfer(e, false, 0) == null) {
                Thread.interrupted();
                throw new InterruptedException();
            }
        }

        /**
         * Inserts the specified element into this queue, waiting if necessary
         * up to the specified wait time for another thread to receive it.
         *
         * @return {@code true} if successful, or {@code false} if the
         *         specified waiting time elapses before a consumer appears
         * @throws InterruptedException {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        public boolean offer(E e, long timeout, TimeUnit unit)
                throws InterruptedException {
            if (e == null) throw new NullPointerException();
            if (transferer.transfer(e, true, unit.toNanos(timeout)) != null)
                return true;
            if (!Thread.interrupted())
                return false;
            throw new InterruptedException();
        }

        /**
         * Inserts the specified element into this queue, if another thread is
         * waiting to receive it.
         *
         * @param e the element to add
         * @return {@code true} if the element was added to this queue, else
         *         {@code false}
         * @throws NullPointerException if the specified element is null
         */
        public boolean offer(E e) {
            if (e == null) throw new NullPointerException();
            return transferer.transfer(e, true, 0) != null;
        }

        /**
         * Retrieves and removes the head of this queue, waiting if necessary
         * for another thread to insert it.
         *
         * @return the head of this queue
         * @throws InterruptedException {@inheritDoc}
         */
        public E take() throws InterruptedException {
            E e = transferer.transfer(null, false, 0);
            if (e != null)
                return e;
            Thread.interrupted();
            throw new InterruptedException();
        }

        /**
         * Retrieves and removes the head of this queue, waiting
         * if necessary up to the specified wait time, for another thread
         * to insert it.
         *
         * @return the head of this queue, or {@code null} if the
         *         specified waiting time elapses before an element is present
         * @throws InterruptedException {@inheritDoc}
         */
        public E poll(long timeout, TimeUnit unit) throws InterruptedException {
            E e = transferer.transfer(null, true, unit.toNanos(timeout));
            if (e != null || !Thread.interrupted())
                return e;
            throw new InterruptedException();
        }

        /**
         * 消费元素(立即返回): 如果此时没有生产者则返回null
         * @return
         */
        public E poll() {
            return transferer.transfer(null, true, 0);
        }

        /**
         * `SynchronousQueue`的容量为0, 其中不包含任何元素, 所以`isEmpty()`方法总是返回false
         */
        public boolean isEmpty() {
            return true;
        }

        /**
         * `SynchronousQueue`的容量为0, 其中不包含任何元素, 所以`size()`方法总是返回0
         */
        public int size() {
            return 0;
        }

        /**
         * `SynchronousQueue`的容量为0, 其中不包含任何元素, 所以`clear`操作不需要做任何事情
         */
        public void clear() {
        }

        /**
         * `SynchronousQueue`的容量为0, 其中不包含任何元素, 所以`contains`操作总是返回false
         */
        public boolean contains(Object o) {
            return false;
        }

        /**
         * `SynchronousQueue`的容量为0, 其中不包含任何元素, 所以`remove`操作总是返回false
         */
        public boolean remove(Object o) {
            return false;
        }

        /**
         * `SynchronousQueue`只能通过线程之间手递手的方式传递数据, 在获取数据的同时就会移除数据, 因此`peek()`始终返回null
         */
        public E peek() {
            return null;
        }

    }


    /*------------------------------ DelayQueue ------------------------------*/

    class DelayQueue<E extends Delayed> {

        private final transient ReentrantLock lock = new ReentrantLock();
        private final PriorityQueue<E> q = new PriorityQueue<E>();

        /**
         * Thread designated to wait for the element at the head of
         * the queue.  This variant of the Leader-Follower pattern
         * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to
         * minimize unnecessary timed waiting.  When a thread becomes
         * the leader, it waits only for the next delay to elapse, but
         * other threads await indefinitely.  The leader thread must
         * signal some other thread before returning from take() or
         * poll(...), unless some other thread becomes leader in the
         * interim.  Whenever the head of the queue is replaced with
         * an element with an earlier expiration time, the leader
         * field is invalidated by being reset to null, and some
         * waiting thread, but not necessarily the current leader, is
         * signalled.  So waiting threads must be prepared to acquire
         * and lose leadership while waiting.
         */
        private Thread leader = null;

        /**
         * Condition signalled when a newer element becomes available
         * at the head of the queue or a new thread may need to
         * become leader.
         */
        private final Condition available = lock.newCondition();

        /**
         * Creates a new {@code DelayQueue} that is initially empty.
         */
        public DelayQueue() {}

        /**
         * Creates a {@code DelayQueue} initially containing the elements of the
         * given collection of {@link Delayed} instances.
         *
         * @param c the collection of elements to initially contain
         * @throws NullPointerException if the specified collection or any
         *         of its elements are null
         */
        public DelayQueue(Collection<? extends E> c) {
            this.addAll(c);
        }

        /**
         * Inserts the specified element into this delay queue.
         *
         * @param e the element to add
         * @return {@code true} (as specified by {@link Collection#add})
         * @throws NullPointerException if the specified element is null
         */
        public boolean add(E e) {
            return offer(e);
        }

        /**
         * Inserts the specified element into this delay queue.
         *
         * @param e the element to add
         * @return {@code true}
         * @throws NullPointerException if the specified element is null
         */
        public boolean offer(E e) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                q.offer(e);
                if (q.peek() == e) {
                    leader = null;
                    available.signal();
                }
                return true;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Inserts the specified element into this delay queue. As the queue is
         * unbounded this method will never block.
         *
         * @param e the element to add
         * @throws NullPointerException {@inheritDoc}
         */
        public void put(E e) {
            offer(e);
        }

        /**
         * Inserts the specified element into this delay queue. As the queue is
         * unbounded this method will never block.
         *
         * @param e the element to add
         * @param timeout This parameter is ignored as the method never blocks
         * @param unit This parameter is ignored as the method never blocks
         * @return {@code true}
         * @throws NullPointerException {@inheritDoc}
         */
        public boolean offer(E e, long timeout, TimeUnit unit) {
            return offer(e);
        }

        /**
         * Retrieves and removes the head of this queue, or returns {@code null}
         * if this queue has no elements with an expired delay.
         *
         * @return the head of this queue, or {@code null} if this
         *         queue has no elements with an expired delay
         */
        public E poll() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                E first = q.peek();
                if (first == null || first.getDelay(NANOSECONDS) > 0)
                    return null;
                else
                    return q.poll();
            } finally {
                lock.unlock();
            }
        }

        /**
         * Retrieves and removes the head of this queue, waiting if necessary
         * until an element with an expired delay is available on this queue.
         *
         * @return the head of this queue
         * @throws InterruptedException {@inheritDoc}
         */
        public E take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (;;) {
                    E first = q.peek();
                    if (first == null)
                        available.await();
                    else {
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0)
                            return q.poll();
                        first = null; // don't retain ref while waiting
                        if (leader != null)
                            available.await();
                        else {
                            Thread thisThread = Thread.currentThread();
                            leader = thisThread;
                            try {
                                available.awaitNanos(delay);
                            } finally {
                                if (leader == thisThread)
                                    leader = null;
                            }
                        }
                    }
                }
            } finally {
                if (leader == null && q.peek() != null)
                    available.signal();
                lock.unlock();
            }
        }

        /**
         * Retrieves and removes the head of this queue, waiting if necessary
         * until an element with an expired delay is available on this queue,
         * or the specified wait time expires.
         *
         * @return the head of this queue, or {@code null} if the
         *         specified waiting time elapses before an element with
         *         an expired delay becomes available
         * @throws InterruptedException {@inheritDoc}
         */
        public E poll(long timeout, TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (;;) {
                    E first = q.peek();
                    if (first == null) {
                        if (nanos <= 0)
                            return null;
                        else
                            nanos = available.awaitNanos(nanos);
                    } else {
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0)
                            return q.poll();
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
                if (leader == null && q.peek() != null)
                    available.signal();
                lock.unlock();
            }
        }

        /**
         * Retrieves, but does not remove, the head of this queue, or
         * returns {@code null} if this queue is empty.  Unlike
         * {@code poll}, if no expired elements are available in the queue,
         * this method returns the element that will expire next,
         * if one exists.
         *
         * @return the head of this queue, or {@code null} if this
         *         queue is empty
         */
        public E peek() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.peek();
            } finally {
                lock.unlock();
            }
        }

    }



}
