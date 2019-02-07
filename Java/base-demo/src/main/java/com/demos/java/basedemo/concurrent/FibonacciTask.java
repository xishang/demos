package com.demos.java.basedemo.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/17
 * <p>
 * JDK关于RecursiveTask的例子
 */
public class FibonacciTask extends RecursiveTask<Integer> {

    final int n;

    public FibonacciTask(int n) {
        this.n = n;
    }

    @Override
    protected Integer compute() {
        if (n <= 1) {
            return n;
        }
        FibonacciTask task1 = new FibonacciTask(n - 1);
        FibonacciTask task2 = new FibonacciTask(n - 2);
        task1.fork();
        return task2.compute() + task1.join();
    }

    public static void main(String[] args) throws Exception {
        ForkJoinPool pool = new ForkJoinPool();
        Integer res = pool.invoke(new FibonacciTask(2));
        System.out.println(res);
        System.out.println(1);
    }

}
