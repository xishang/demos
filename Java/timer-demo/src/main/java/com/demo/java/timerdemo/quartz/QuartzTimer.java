package com.demo.java.timerdemo.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/20
 * <p>
 * Quartz核心类:
 * -> Scheduler: 调度器
 * -> Trigger: 定义触发条件
 * -> JobDetail: 定义任务数据
 * -> Job: 真正的执行逻辑
 * === 设计成`JobDetail & Job`而不直接使用`Job`的原因: 任务是有可能并发执行, 如果Scheduler直接使用Job, 就会存在对同一个Job实例并发访问的问题
 * === `JobDetail & Job`的方式: Scheduler每次执行都会根据JobDetail创建一个新的Job实例, 这样就可以规避并发访问的问题
 */
public class QuartzTimer {

    public static void main(String[] args) {
        /* === 表达式
            位置	时间域    允许值	    特殊值
            1	秒        0-59	    , - * /
            2	分钟      0-59	    , - * /
            3	小时      0-23	    , - * /
            4	日期      1-31       , - * ? / L W C
            5	月份      1-12       , - * /
            6	星期      1-7	    , - * ? / L C #
            7	年份(可选) 1-31	    , - * /
           === 含义
           星号(*)：可用在所有字段中，表示对应时间域的每一个时刻，例如， 在分钟字段时，表示“每分钟”
           问号（?）：该字符只在日期和星期字段中使用，它通常指定为“无意义的值”，相当于点位符
           减号(-)：表达一个范围，如在小时字段中使用“10-12”，则表示从10到12点，即10,11,12
           逗号(,)：表达一个列表值，如在星期字段中使用“MON,WED,FRI”，则表示星期一，星期三和星期五
           斜杠(/)：x/y表达一个等步长序列，x为起始值，y为增量步长值。如在分钟字段中使用0/15，则表示为0,15,30和45秒，而5/15在分钟字段中表示5,20,35,50，你也可以使用`*\/y`，它等同于0/y
           L：该字符只在日期和星期字段中使用，代表“Last”的意思，但它在两个字段中意思不同。L在日期字段中，表示这个月份的最后一天，如一月的31号，非闰年二月的28号；如果L用在星期中，则表示星期六，等同于7。但是，如果L出现在星期字段里，而且在前面有一个数值X，则表示“这个月的最后X天”，例如，6L表示该月的最后星期五；
           W：该字符只能出现在日期字段里，是对前导日期的修饰，表示离该日期最近的工作日。例如15W表示离该月15号最近的工作日，如果该月15号是星期六，则匹配14号星期五；如果15日是星期日，则匹配16号星期一；如果15号是星期二，那结果就是15号星期二。但必须注意关联的匹配日期不能够跨月，如你指定1W，如果1号是星期六，结果匹配的是3号星期一，而非上个月最后的那天。W字符串只能指定单一日期，而不能指定日期范围；
           LW组合：在日期字段可以组合使用LW，它的意思是当月的最后一个工作日；
           井号(#)：该字符只能在星期字段中使用，表示当月某个工作日。如6#3表示当月的第三个星期五(6表示星期五，#3表示当前的第三个)，而4#5表示当月的第五个星期三，假设当月没有第五个星期三，忽略不触发；
           C：该字符只在日期和星期字段中使用，代表“Calendar”的意思。它的意思是计划所关联的日期，如果日期没有被关联，则相当于日历中所有日期。例如5C在日期字段中就相当于日历5日以后的第一天。1C在星期字段中相当于星期日后的第一天。
           === Cron表达式对特殊字符的大小写不敏感，对代表星期的缩写英文大小写也不敏感
        */
        // cron表达式: 每2秒触发一次
        String cron = "0/2 * * * * ?";
        // 定时任务的name和group
        String name = "print-job";
        String group = "print-job-group";
        try {
            // 创建scheduler
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // Trigger构建
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
            // 设置name和group
            triggerBuilder.withIdentity(name, group);
            // 加入scheduler后立即生效
            triggerBuilder.startNow();
            // 设置调度方式: 使用cron表达式
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            // 生成Trigger
            Trigger trigger = triggerBuilder.build();

            // Job构建, 定义Job类
            JobBuilder jobBuilder = JobBuilder.newJob(PrintJob.class);
            // 设置name和group
            jobBuilder.withIdentity(name, group);
            // 添加数据
            jobBuilder.usingJobData("name", "quartz");
            // 生成JobDetail
            JobDetail jobDetail = jobBuilder.build();
            // 获取JobDetail中的JobDataMap并添加数据
            jobDetail.getJobDataMap().put("age", 20);

            // 加入调度
            scheduler.scheduleJob(jobDetail, trigger);

            // 启动调度
            scheduler.start();

            // 运行一段时间后关闭
            Thread.sleep(10000);
            scheduler.shutdown(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
