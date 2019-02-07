package com.demo.java.timerdemo.jdk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/19
 */
public class JdkTimer {

    public static void main(String[] args) throws Exception {
        timer();
        scheduledExecutor();
    }

    static class PrintTimerTask extends TimerTask {
        String desc;
        long sleepTime;
        PrintTimerTask(String desc, long sleepTime) {
            this.desc = desc;
            this.sleepTime = sleepTime;
        }
        @Override
        public void run() {
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
            System.out.println("Timer Task: desc = " + desc + ", time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
    }

    /**
     * Timer: 定时执行
     * === 不同于ScheduledThreadPoolExecutor, Timer的时间计算在Task.run()之前
     */
    public static void timer() {
        Timer timer = new Timer();
        // 定时执行-执行一次
        timer.schedule(new PrintTimerTask("定时执行", 0), new Date(new Date().getTime() + 2000));
        // 固定延时
        timer.schedule(new PrintTimerTask("固定延时", 2000), new Date(), 3000);
        // 固定间隔
        timer.scheduleAtFixedRate(new PrintTimerTask("固定间隔", 2000), new Date(), 3000);
    }

    /**
     * ScheduledThreadPoolExecutor: 调度执行器
     * -> 固定间隔: 下一次执行时间 = 本次执行时间 + period
     * -> 固定延迟: 下一次执行时间 = 当前时间 + |period|
     *
     * @throws Exception
     */
    public static void scheduledExecutor() throws Exception {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 执行一次
        executor.schedule(() -> {
            System.out.println("ScheduledThreadPool schedule: time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }, 3L, TimeUnit.SECONDS);
        // 以固定频率执行: lastTime + period
        executor.scheduleAtFixedRate(() -> {
            // 设置下一次执行的任务需要在当前任务执行完成: sleepTime > period, 因此时间间隔为: sleepTime = 5s
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            System.out.println("ScheduledThreadPool fixedRate: time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }, 3L, 3L, TimeUnit.SECONDS);
        // 以固定延迟执行: now + period
        executor.scheduleWithFixedDelay(() -> {
            // 设置下一次执行的任务需要在当前任务执行完成: now = lastTime + sleepTime, 因此时间间隔为: sleepTime + period = 8s
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            System.out.println("ScheduledThreadPool fixedDelay: time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }, 3L, 3L, TimeUnit.SECONDS);
    }

}
