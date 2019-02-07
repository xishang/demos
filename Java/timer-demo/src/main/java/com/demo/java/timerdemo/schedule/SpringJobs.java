package com.demo.java.timerdemo.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/20
 *
 * Spring Schedule
 */
@Component
public class SpringJobs {

    /**
     * cron表达式形式
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void cronJob() {
        System.out.println("cron job: time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    /**
     * 固定间隔时间
     */
    @Scheduled(fixedRate = 5000)
    public void rateJob() {
        System.out.println("rate job: time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    /**
     * 固定延迟时间
     */
    @Scheduled(fixedDelay = 5000)
    public void delayJob() {
        System.out.println("delay job: time = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

}
