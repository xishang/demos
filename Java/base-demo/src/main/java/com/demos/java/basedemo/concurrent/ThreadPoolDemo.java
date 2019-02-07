package com.demos.java.basedemo.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/5
 */
public class ThreadPoolDemo {

    public static void main(String[] args) {
        threadError();
        afterExecute();
    }

    public static void afterExecute() {
        // 新建线程池: 重写ThreadPoolExecutor.afterExecute()方法
        ExecutorService threadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                System.out.println("执行完毕: " + t);
            }
        };
        // 运行一个正常的任务
        threadPool.execute(() -> {
            System.out.println("first task");
        });
        // 运行一个异常的任务
        threadPool.execute(() -> {
            System.out.println("error task");
            throw new NullPointerException();
        });
    }

    public static void threadError() {
        // 新建一个异常运行的线程
        Thread thread = new Thread() {
            @Override
            public void run() {
                System.out.println("Thread run!");
                throw new NullPointerException();
            }
        };
        // 默认线程异常退出处理器: 线程因为未捕获的异常而退出时调用
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.out.println("Default handler: " + e);
        });
        // 设置当前线程的异常退出处理器
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.out.println("By Hand handler: " + e.getMessage());
        });
        thread.start();
    }

}
