/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 * <p>
 * 锁
 * <p>
 * 特性:
 * -> 是否可重入: `synchronized`和`ReentrantLock`都是可重入锁; 要实现可重入, 在锁的实现中加入一个当前持有锁的线程变量即可.
 * -> 是否可中断: `synchronized`是不可中断的, `ReentrantLock`实现了`lockInterruptibly()`, 可以在等待锁时被其他线程中断并抛出InterruptedException
 * -> 是否公平: `synchronized`是非公平的, `ReentrantLock`提供了公平和非公平地获取锁的方式
 * -> 读写锁: `ReentrantReadWriteLock`提供了读写锁的实现
 * <p>
 * 实现方式:
 * -> Spin Lock: 自旋锁, 无法保证公平性, 总线流量压力大
 * -> Ticket Lock: 解决了公平性问题, 总线流量压力大
 * -> CLH Lock: 基于链表的可扩展、高性能、公平的自旋锁, 在前驱节点的属性上自旋
 * -> MCS Lock: 基于链表的可扩展、高性能、公平的自旋锁, 申请线程只在本地变量上自旋, 直接前驱负责通知其结束自旋
 */
package com.demos.java.basedemo.concurrent.lock;