package com.demos.spring.completedemo.util;

import com.demos.spring.completedemo.redis.RedisDao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/10/5
 */
public class IdGenerator {

    private static RedisDao redisDao = SpringContextHolder.getBean(RedisDao.class);

    /**
     * 生成订单号：yyyyMMddHH(10)+type(1)+index(6)+Xxx(userId后3位)
     * @param type
     * @param userId
     * @param date
     * @return
     */
    public static String generateOrderId(String type, int userId, Date date) {
        String prefix = getFormatDatePrefix(date);
        String orderIdIndexKey = "order_id_index" + prefix;
        Long index = redisDao.incr(orderIdIndexKey);
        // orderIdIndexKey到小时，设置过期时间为1小时即可
        redisDao.expire(orderIdIndexKey, 3600);
        // String.format(): %[index$][标识][最少宽度][.精度]转换方式
        String formatIndex = String.format("%06d", index);
        String formatUserId = String.format("%03d", userId);
        // 截取3位
        formatUserId = formatUserId.substring(formatUserId.length() - 3);
        return prefix + type + formatIndex + formatUserId;
    }

    /**
     * 生成订单号：date_format+type(1)+index(6)
     *
     * @param type
     * @return
     */
    public static String generateOrderId(String type) {
        /* 生成订单号前缀 */
        String prefix = getMinDatePrefix(new Date());
        String orderIdIndexKey = "order_id_index" + prefix;
        Long index = redisDao.incr(orderIdIndexKey);
        // orderIdIndexKey到小时，设置过期时间为1小时即可
        redisDao.expire(orderIdIndexKey, 3600);
        String formatIndex = String.format("%06d", index);
        return prefix + type + formatIndex;
    }

    /**
     * 生成短的时间前缀：year(2)+day(3)+hour(2)
     *
     * @param date
     * @return
     */
    private static String getMinDatePrefix(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int year = c.get(Calendar.YEAR);
        int day = c.get(Calendar.DAY_OF_YEAR);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String formatDay = String.format("%03d", day);
        String formatHour = String.format("%02d", hour);
        return (year - 2000) + formatDay + formatHour;
    }

    /**
     * 生成格式化的时间前缀：yyyyMMddHH
     *
     * @param date
     * @return
     */
    private static String getFormatDatePrefix(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
        return dateFormat.format(date);
    }

}
