package com.demos.java.basedemo.concurrent.lock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 * <p>
 * MCS: 取自发明人 John Mellor-Crummey and Michael Scott
 * <p>
 * 基于链表的可扩展、高性能、公平的自旋锁, 申请线程只在本地变量上自旋, 直接前驱负责通知其结束自旋, 减少了处理器缓存同步的次数
 */
public class MCSLock {

    public static class MCSNode {
        private volatile MCSNode next;
        // 默认是在等待锁
        private volatile boolean isBlock = true;
    }

    // 指向最后一个申请锁的MCSNode
    private volatile MCSNode tail;

    private static final AtomicReferenceFieldUpdater<MCSLock, MCSNode> updater =
            AtomicReferenceFieldUpdater.newUpdater(MCSLock.class, MCSNode.class, "tail");

    public void lock(MCSNode node) {
        // step1: 获取当前的tail节点作为自己的前驱节点
        MCSNode pre = updater.getAndSet(this, node);
        if (pre != null) { // 前驱节点不为空
            // step2: 将前驱节点的next域指向自己
            pre.next = node;
            // step3: 自旋等待, 知道前驱节点将
            while (node.isBlock) {
            }
        } else { // 前驱节点为空, 当前只有自己在请求锁, 主动将自己的isBlock设置为false
            node.isBlock = false;
        }
    }

    public void unlock(MCSNode node) {
        // 持有锁的线程才能释放锁
        if (node.isBlock) {
            return;
        }
        // 先检查是否有人排在自己后面
        if (node.next == null) {
            // step4: 检查队列中是否只有自己, 释放对当前节点的引用, help GC
            if (updater.compareAndSet(this, node, null)) {
                // 若返回true, 则表示确实没有后继节点
                return;
            } else {
                // 并发情况, 此时突然有一个线程执行完step1将自己设为了tail, 但还没来得及执行step2, 将自己设置为当前节点的后继节点
                // step5: 等待并发线程将自己设置为当前节点的后继节点
                while (node.next == null) {
                }
            }
        }
        // 将后继节点的isBlock设置为false, 通知其可以获取锁
        node.next.isBlock = false;
        node.next = null; // help GC
    }

}
