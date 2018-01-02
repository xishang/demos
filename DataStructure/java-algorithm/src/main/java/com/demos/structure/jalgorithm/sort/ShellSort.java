package com.demos.structure.jalgorithm.sort;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 希尔排序: 不稳定排序
 * 基本思路:
 * 时间复杂度: 最好: O(n), 最坏: O(nlog2n), 平均: O(nlog2n)
 * 空间复杂度: O(1)
 */
public class ShellSort {

    public static void sort(int[] array) {

        // fork(分叉)/join(合并)
        ForkJoinPool pool = new ForkJoinPool();
        // resultless ForkJoinTask
        RecursiveAction action;
        // result-bearing ForkJoinTask
        RecursiveTask task;

        // 计时同步-----start
        Phaser phaser = new Phaser(1);
        CountDownLatch latch = new CountDownLatch(1); // 不可重用
        CyclicBarrier barrier = new CyclicBarrier(1); // 可重用
        Semaphore semaphore = new Semaphore(1);
        // 计时同步-----end

        ThreadLocal local;
        AtomicInteger i;
    }

    public static void main(String[] args) {
        int a = 5;
        int b = 10;
        b = a = 8;
        System.out.println(a);
        System.out.println(b);
    }

}
