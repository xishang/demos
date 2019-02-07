package com.demo.java.timerdemo.quartz;

import org.quartz.*;

import java.util.Date;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/20
 */
public class PrintJob implements Job {

    private String name;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail detail = context.getJobDetail();
        // 从Context获取JobDataMap中的数据
        JobDataMap map = detail.getJobDataMap();
        // 打印数据
        System.out.println("print job: name=" + name + ", age=" + map.getInt("age") + ", time=" + new Date());
    }

    /**
     * 属性的setter方法: 会将JobDataMap的属性自动注入
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
