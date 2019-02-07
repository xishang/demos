package com.demos.java.basedemo.security;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/16
 */
public class Test {

    public static void main(String[] args) {
        /*Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date monday = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date st1 = cal.getTime();*/

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date todayBegin = cal.getTime(); // 今天起始时间
        cal.add(Calendar.DATE, 1);
        Date tomorrowBegin = cal.getTime(); // 明天起始时间
        cal.add(Calendar.DATE, -2);
        Date yesterdayBegin = cal.getTime(); // 昨天起始时间
        cal.add(Calendar.DATE, -5);
        Date sixDaysAgo = cal.getTime(); // 6天前的起始时间，用于统计七天内数据
        cal.add(Calendar.DATE, 6);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekBegin = cal.getTime(); // 本周起始时间
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date monthBegin = cal.getTime(); // 本月起始时间

        System.out.printf("todayBegin: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(todayBegin));
        System.out.printf("tomorrowBegin: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tomorrowBegin));
        System.out.printf("yesterdayBegin: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(yesterdayBegin));
        System.out.printf("sixDaysAgo: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sixDaysAgo));
        System.out.printf("weekBegin: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(weekBegin));
        System.out.printf("monthBegin: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(monthBegin));
    }


}
