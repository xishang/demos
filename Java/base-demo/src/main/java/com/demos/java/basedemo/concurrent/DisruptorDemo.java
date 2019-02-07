package com.demos.java.basedemo.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/5
 */
public class DisruptorDemo {

    public static void incrementSingle() {
        long curTs = System.currentTimeMillis();
        Long index = 0l;
        for (int i = 0; i < 50000000; i++) {
            index++;
        }
        System.out.println("单线程自增5亿次, 耗时: " + (System.currentTimeMillis() - curTs));
    }

    public static void singleAndLock() {
        long curTs = System.currentTimeMillis();
        Long index = 0l;
        for (int i = 0; i < 50000000; i++) {
            synchronized (DisruptorDemo.class) {
                index++;
            }
        }
        System.out.println("单线程+Lock自增5亿次, 耗时: " + (System.currentTimeMillis() - curTs));
    }

    public static void twoThread() {
        long curTs = System.currentTimeMillis();
        AtomicLong index = new AtomicLong();
        new Thread(() -> {
            while (index.get() < 50000000) {
                index.getAndIncrement();
            }
            System.out.println("多线程自增5亿次, 耗时: " + (System.currentTimeMillis() - curTs));
        }).start();
        while (index.get() < 50000000) {
            index.getAndIncrement();
        }
        System.out.println("多线程自增5亿次, 耗时: " + (System.currentTimeMillis() - curTs));

    }

    public static void main(String[] args) {
//        incrementSingle();
//        singleAndLock();
//        twoThread();
        falseSharing();
    }


    public static void falseSharing() {
        // 伪共享
        FalseSharing falseSharing = new FalseSharing();
        new Thread(() -> {
            long curTs = System.currentTimeMillis();
            while (falseSharing.head < 200000000) {
                long fHead = falseSharing.head;
                falseSharing.head = fHead + 1;
            }
            System.out.println("伪共享冲突: 耗时 = " + (System.currentTimeMillis() - curTs));
        }).start();
        new Thread(() -> {
            long curTs = System.currentTimeMillis();
            while (falseSharing.tail < 200000000) {
                long fTail = falseSharing.tail;
                falseSharing.tail = fTail + 1;
            }
            System.out.println("伪共享冲突: 耗时 = " + (System.currentTimeMillis() - curTs));
        }).start();
        // 无竞争
        CacheLinePadding cacheLinePadding = new CacheLinePadding();
        new Thread(() -> {
            long curTs = System.currentTimeMillis();
            while (cacheLinePadding.head < 200000000) {
                long cHead = cacheLinePadding.head;
                cacheLinePadding.head = cHead + 1;
            }
            System.out.println("无竞争: 耗时 = " + (System.currentTimeMillis() - curTs));
        }).start();
        new Thread(() -> {
            long curTs = System.currentTimeMillis();
            while (cacheLinePadding.tail < 200000000) {
                long cTail = cacheLinePadding.tail;
                cacheLinePadding.tail = cTail + 1;
            }
            System.out.println("无竞争: 耗时 = " + (System.currentTimeMillis() - curTs));
        }).start();
    }

    public static class FalseSharing {
        public volatile long head;
        public volatile long tail;
    }

    public static class CacheLinePadding {
        public volatile long head;
        private long p1, p2, p3, p4, p5, p6, p7;
        public volatile long tail;
    }

}
