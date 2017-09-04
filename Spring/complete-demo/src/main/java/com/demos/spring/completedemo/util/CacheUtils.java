package com.demos.spring.completedemo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 通用缓存工具类
 */
public class CacheUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtils.class);
    public static String PREFIX = PropertiesUtils.getValue("redis.keyPrefix");

    public static RedisTemplate<String, Object> redisTemplate = SpringContextHolder.getBean("redisTemplate");
    public static StringRedisTemplate stringRedisTemplate = SpringContextHolder.getBean("stringRedisTemplate");

    private CacheUtils() {
    }

    /**
     * 删除缓存<br>
     * 根据key精确匹配删除
     *
     * @param key
     */
    public static void del(String... key) {
        LOGGER.warn("delete cache, keys in ({})", merge(key));
        for (String k : key) {
            redisTemplate.delete(PREFIX + k);
        }
    }

    /**
     * 批量删除<br>
     * （该操作会执行模糊查询，请尽量不要使用，以免影响性能或误删）
     *
     * @param pattern
     */
    public static void batchDel(String... pattern) {
        LOGGER.warn("batchDel cache, pattern in ({})", merge(pattern));
        for (String kp : pattern) {
            redisTemplate.delete(redisTemplate.keys(PREFIX + kp + "*"));
        }
    }

    /**
     * 取得缓存（int型）
     *
     * @param key
     * @return
     */
    public static Integer getInt(String key) {
        String value = stringRedisTemplate.boundValueOps(PREFIX + key).get();
        if (StringUtils.isNotBlank(value)) {
            return Integer.valueOf(value);
        }
        return 0;
    }

    /**
     * 取得缓存（long型）
     *
     * @param key
     * @return
     */
    public static Long getLong(String key) {
        String value = stringRedisTemplate.boundValueOps(PREFIX + key).get();
        if (StringUtils.isNotBlank(value)) {
            return Long.valueOf(value);
        }
        return 0l;
    }

    /**
     * 取得缓存（字符串类型）
     *
     * @param key
     * @return
     */
    public static String getStr(String key) {
        return stringRedisTemplate.boundValueOps(PREFIX + key).get();
    }

    /**
     * 取得缓存（字符串类型）
     *
     * @param key
     * @return
     */
    public static String getStr(String key, boolean retain) {
        String value = stringRedisTemplate.boundValueOps(PREFIX + key).get();
        if (!retain) {
            stringRedisTemplate.delete(PREFIX + key);
        }
        return value;
    }

    /**
     * 获取缓存<br>
     * 注：基本数据类型(Character除外)，请直接使用get(String key, Class<T> clazz)取值
     *
     * @param key
     * @return
     */
    public static Object getObj(String key) {
        return redisTemplate.boundValueOps(PREFIX + key).get();
    }

    /**
     * 获取缓存<br>
     * 注：java 8种基本类型的数据请直接使用get(String key, Class<T> clazz)取值
     *
     * @param key
     * @param retain 是否保留
     * @return
     */
    public static Object getObj(String key, boolean retain) {
        Object obj = redisTemplate.boundValueOps(PREFIX + key).get();
        if (!retain && obj != null) {
            redisTemplate.delete(PREFIX + key);
        }
        return obj;
    }

    /**
     * 获取缓存<br>
     * 注：慎用java基本数据类型进行转换（可能会出现空值，转换报错）
     *
     * @param key   key
     * @param clazz 类型
     * @return
     */
    public static <T> T get(String key, Class<T> clazz) {
        key = PREFIX + key;
        if (clazz.equals(String.class)) {
            return (T) stringRedisTemplate.boundValueOps(key).get();
        } else if (clazz.equals(Integer.class) || clazz.equals(Long.class)) {
            return (T) stringRedisTemplate.boundValueOps(key).get();
        } else if (clazz.equals(Double.class) || clazz.equals(Float.class)) {
            return (T) stringRedisTemplate.boundValueOps(key).get();
        } else if (clazz.equals(Short.class) || clazz.equals(Boolean.class)) {
            return (T) stringRedisTemplate.boundValueOps(key).get();
        }
        return (T) redisTemplate.boundValueOps(key).get();
    }

    /**
     * 获取缓存json对象<br>
     *
     * @param key   key
     * @param clazz 类型
     * @return
     */
    public static <T> T getJson(String key, Class<T> clazz) {
        return JsonMapper.fromJsonString(stringRedisTemplate.boundValueOps(PREFIX + key).get(), clazz);
    }

    /**
     * 将value对象写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 失效时间(秒)
     */
    public static void set(String key, Object value, int expireTime) {
        if (null == key || null == value) {
            throw new RuntimeException("key or value must not null");
        }
        key = PREFIX + key;
        if (value instanceof String) {
            stringRedisTemplate.opsForValue().set(key, value.toString());
        } else if (value instanceof Integer || value instanceof Long) {
            stringRedisTemplate.opsForValue().set(key, value.toString());
        } else if (value instanceof Double || value instanceof Float) {
            stringRedisTemplate.opsForValue().set(key, value.toString());
        } else if (value instanceof Short || value instanceof Boolean) {
            stringRedisTemplate.opsForValue().set(key, value.toString());
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
        if (expireTime > 0) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 将value对象以JSON格式写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 失效时间(秒)
     */
    public static void setJson(String key, Object value, int expireTime) {
        key = PREFIX + key;
        stringRedisTemplate.opsForValue().set(key, JsonMapper.toJsonString(value));
        if (expireTime > 0) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 更新key对象field的值
     *
     * @param key   缓存key
     * @param field 缓存对象field
     * @param value 缓存对象field值
     */
    public static void setJsonField(String key, String field, String value) {
        JSONObject obj = JSON.parseObject(stringRedisTemplate.boundValueOps(PREFIX + key).get());
        obj.put(field, value);
        stringRedisTemplate.opsForValue().set(PREFIX + key, obj.toJSONString());
    }

    /**
     * 递减操作
     *
     * @param key
     * @param by
     * @return
     */
    public static double decr(String key, double by) {
        return redisTemplate.opsForValue().increment(PREFIX + key, -by);
    }

    /**
     * 递增操作
     *
     * @param key
     * @param by
     * @return
     */
    public static double incr(String key, double by) {
        return redisTemplate.opsForValue().increment(PREFIX + key, by);
    }

    /**
     * 递减操作
     *
     * @param key
     * @param by
     * @return
     */
    public static long decr(String key, long by) {
        return redisTemplate.opsForValue().increment(PREFIX + key, -by);
    }

    /**
     * 递增操作
     *
     * @param key
     * @param by
     * @return
     */
    public static long incr(String key, long by) {
        return redisTemplate.opsForValue().increment(PREFIX + key, by);
    }

    /**
     * 获取double类型值
     *
     * @param key
     * @return
     */
    public static double getDouble(String key) {
        String value = stringRedisTemplate.boundValueOps(PREFIX + key).get();
        if (StringUtils.isNotBlank(value)) {
            return Double.valueOf(value);
        }
        return 0d;
    }

    /**
     * 设置double类型值
     *
     * @param key
     * @param value
     * @param expireTime 失效时间(秒)
     */
    public static void setDouble(String key, double value, int expireTime) {
        stringRedisTemplate.opsForValue().set(PREFIX + key, String.valueOf(value));
        if (expireTime > 0) {
            stringRedisTemplate.expire(PREFIX + key, expireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 将map写入缓存
     *
     * @param key
     * @param map
     */
    public static <T> void setMap(String key, Map<String, T> map) {
        redisTemplate.opsForHash().putAll(PREFIX + key, map);
    }

    /**
     * 将map写入缓存
     *
     * @param key
     * @param obj
     */
    @SuppressWarnings("unchecked")
    public static <T> void setMap(String key, T obj) {
        Map<String, String> map = (Map<String, String>) JsonMapper.parseObject(obj, Map.class);
        redisTemplate.opsForHash().putAll(PREFIX + key, map);
    }

    /**
     * 向key对应的map中添加缓存对象
     *
     * @param key
     * @param map
     */
    public static <T> void addMap(String key, Map<String, T> map) {
        redisTemplate.opsForHash().putAll(PREFIX + key, map);
    }

    /**
     * 向key对应的map中添加缓存对象
     *
     * @param key   cache对象key
     * @param field map对应的key
     * @param value 值
     */
    public static void addMap(String key, String field, String value) {
        redisTemplate.opsForHash().put(PREFIX + key, field, value);
    }

    /**
     * 向key对应的map中添加缓存对象
     *
     * @param key   cache对象key
     * @param field map对应的key
     * @param obj   对象
     */
    public static <T> void addMap(String key, String field, T obj) {
        redisTemplate.opsForHash().put(PREFIX + key, field, obj);
    }

    /**
     * 获取map缓存
     *
     * @param key
     * @param clazz
     * @return
     */
    public static <T> Map<String, T> mget(String key, Class<T> clazz) {
        BoundHashOperations<String, String, T> boundHashOperations = redisTemplate.boundHashOps(PREFIX + key);
        return boundHashOperations.entries();
    }

    /**
     * 获取map缓存，并转为<T>类型对象
     *
     * @param key
     * @param clazz
     * @return
     */
    public static <T> T getMap(String key, Class<T> clazz) {
        BoundHashOperations<String, String, String> boundHashOperations = redisTemplate.boundHashOps(PREFIX + key);
        Map<String, String> map = boundHashOperations.entries();
        return JsonMapper.parseObject(map, clazz);
    }

    /**
     * 获取map缓存中的某个对象
     *
     * @param key
     * @param field
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getMapField(String key, String field, Class<T> clazz) {
        return (T) redisTemplate.boundHashOps(PREFIX + key).get(field);
    }

    /**
     * 删除map中的某个对象
     *
     * @param key   map对应的key
     * @param field map中该对象的key
     */
    public static void delMapField(String key, String... field) {
        redisTemplate.opsForHash().delete(PREFIX + key, field);
    }

    /**
     * 为哈希表key中的域field的值
     *
     * @param key   键
     * @param field map域
     * @param value 增量值
     * @return
     */
    public static long hincr(String key, String field, long value) {
        return redisTemplate.opsForHash().increment(PREFIX + key, field, value);
    }

    public static void hset(String key, String field, Object value) {
        redisTemplate.opsForHash().put(PREFIX + key, field, value);
    }

    public static Object hget(String key, String field) {
        return redisTemplate.boundHashOps(PREFIX + key).get(field);
    }

    public static void hdel(String key, String... fields) {
        if (fields == null || fields.length == 0) {
            redisTemplate.delete(PREFIX + key);
        } else {
            redisTemplate.opsForHash().delete(PREFIX + key, fields);
        }
    }

    public static Long hlen(String key) {
        return redisTemplate.boundHashOps(PREFIX + key).size();
    }

    public static <T> Set<T> hkeys(String key) {
        return (Set<T>) redisTemplate.boundHashOps(PREFIX + key).keys();
    }

    public static <T> List<T> hvals(String key) {
        return (List<T>) redisTemplate.boundHashOps(PREFIX + key).values();
    }

    /**
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public static boolean setnx(String key, String value, int expireTime) {
        boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(PREFIX + key, value);
        if (expireTime > 0) {
            redisTemplate.expire(PREFIX + key, expireTime, TimeUnit.SECONDS);
        }
        return flag;
    }

    /**
     * 指定缓存的失效时间
     *
     * @param key        缓存KEY
     * @param expireTime 失效时间(秒)
     */
    public static void expire(String key, int expireTime) {
        redisTemplate.expire(PREFIX + key, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 添加set
     *
     * @param key
     * @param value
     */
    public static void sadd(String key, String... value) {
        redisTemplate.boundSetOps(PREFIX + key).add(value);
    }

    /**
     * 删除set集合中的对象
     *
     * @param key
     * @param value
     */
    public static void srem(String key, String... value) {
        redisTemplate.boundSetOps(PREFIX + key).remove(value);
    }

    /**
     * 短信缓存
     *
     * @param key
     * @param value
     * @param time
     */
    public static void setIntForPhone(String key, Object value, int time) {
        stringRedisTemplate.opsForValue().set(PREFIX + key, JsonMapper.toJsonString(value));
        if (time > 0) {
            stringRedisTemplate.expire(PREFIX + key, time, TimeUnit.SECONDS);
        }
    }

    /**
     * 判断key对应的缓存是否存在
     *
     * @param key
     * @return
     */
    public static boolean exists(String key) {
        return redisTemplate.hasKey(PREFIX + key);
    }

    /**
     * 模糊查询keys
     *
     * @param pattern
     * @return
     */
    public static Set<String> keys(String pattern) {
        return redisTemplate.keys(PREFIX + pattern);
    }

    /**
     * 合并数组为字符串
     *
     * @param strings
     * @return
     */
    private static String merge(String... strings) {
        if (strings == null || strings.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        int len = strings.length;
        for (int i = 0; i < len; i++) {
            sb.append(PREFIX + strings[i]);
            if (len != i + 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
