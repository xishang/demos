package com.demo.framework.netty.echo;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/14
 */
public class Main {

    public static void main1(String[] args) throws Exception {
        Semaphore semaphore = new Semaphore(5);
        new Thread(() -> {
            try {
                semaphore.acquire(4);
                System.out.println("acquire 4 permits!");
                Thread.sleep(5000);
                System.out.println("release 4 permits!");
                semaphore.release(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(500);
                semaphore.acquire(2);
                System.out.println("acquire 2 permits!");
                semaphore.release(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(200);
                semaphore.acquire(3);
                System.out.println("acquire 3 permits!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
//        new Thread(() -> {
//            try {
//                Thread.sleep(1000);
//                semaphore.acquire(1);
//                System.out.println("acquire 1 permits!");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    public static void main2(String[] args) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

        new Thread(() -> {
            try {
                Thread.sleep(200);
                lock.readLock().lock();
                System.out.println("获取读锁: 1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(500);
                lock.readLock().lock();
                System.out.println("获取写锁: 2");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                lock.readLock().lock();
                System.out.println("获取读锁: 3");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(-1 << 29));
        System.out.println(Integer.toBinaryString(0 << 29));
        System.out.println(Integer.toBinaryString(1 << 29));
        System.out.println(Integer.toBinaryString(2 << 29));
        System.out.println(Integer.toBinaryString(3 << 29));
    }

}
