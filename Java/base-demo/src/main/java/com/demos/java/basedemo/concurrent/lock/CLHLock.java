package com.demos.java.basedemo.concurrent.lock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 * <p>
 * CLH: 取自发明人 Craig, Landin, and Hagersten
 * <p>
 * 基于链表的可扩展、高性能、公平的自旋锁, 在前驱节点的属性上自旋
 */
public class CLHLock {

    public static class CLHNode {
        // 默认是在等待锁
        private volatile boolean isLocked = true;
    }

    private volatile CLHNode tail;

    private static final AtomicReferenceFieldUpdater<CLHLock, CLHNode> updater =
            AtomicReferenceFieldUpdater.newUpdater(CLHLock.class, CLHNode.class, "tail");

    public void lock(CLHNode node) {
        // 获取当前的tail节点作为自己的前驱节点(隐式链), 并把自己设置为新的tail
        CLHNode pre = updater.getAndSet(this, node);
        if (pre != null) { // 不是唯一一个请求获取锁的节点
            // 前驱节点还未释放锁, 自旋等待
            while (pre.isLocked) {
            }
        }
    }

    public void unlock(CLHNode node) {
        // 若队列中只有当前节点, 则释放对当前节点的引用, help GC
        if (!updater.compareAndSet(this, node, null)) {
            // 若有后继节点, 则改变状态, 让后继节点结束自旋
            node.isLocked = false;
        }
    }

}
