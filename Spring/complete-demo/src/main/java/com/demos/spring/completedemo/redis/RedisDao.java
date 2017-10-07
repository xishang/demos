package com.demos.spring.completedemo.redis;

import com.demos.spring.completedemo.util.SerializeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * Redis基本操作
 *
 * @author xishang
 * @version 1.0
 * @date 2017/10/5
 */
@Repository
public class RedisDao {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public Long hIncrBy(final String hash, final String key, final Long delta, final Long maxValue) {
        Long result = redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] hashBytes = hash.getBytes();
                byte[] keyBytes = key.getBytes();
                Long value = connection.hIncrBy(hashBytes, keyBytes, delta);
                if (value > maxValue) { // value大于max,重新开始计数
                    connection.hDel(hashBytes, keyBytes);
                }
                return value;
            }
        });
        return result;
    }

    public Serializable hGet(final String hash, final String key) {
        Serializable result = redisTemplate.execute(new RedisCallback<Serializable>() {
            public Serializable doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] hashBytes = hash.getBytes();
                byte[] keyBytes = key.getBytes();
                byte[] valueBytes = connection.hGet(hashBytes, keyBytes);
                return SerializeUtils.deserialize(valueBytes);
            }
        });
        return result;
    }

    public boolean hSet(final String hash, final String key, final Serializable value) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] hashBytes = hash.getBytes();
                byte[] keyBytes = key.getBytes();
                byte[] valueBytes = SerializeUtils.serialize(value);
                return connection.hSet(hashBytes, keyBytes, valueBytes);
            }
        });
        return result;
    }

    public boolean hDel(final String hash, final String key) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] hashBytes = hash.getBytes();
                byte[] keyBytes = key.getBytes();
                Long delCount = connection.hDel(hashBytes, keyBytes);
                return delCount == 1L;
            }
        });
        return result;
    }

    public boolean setEx(final String key, final Serializable value, final long seconds) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                byte[] valueBytes = SerializeUtils.serialize(value);
                connection.setEx(keyBytes, seconds, valueBytes);
                return true;
            }
        });
        return result;
    }

    public Serializable get(final String key) {
        Serializable result = redisTemplate.execute(new RedisCallback<Serializable>() {
            public Serializable doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                return SerializeUtils.deserialize(connection.get(keyBytes));
            }
        });
        return result;
    }

    public boolean del(final String key) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                Long delCount = connection.del(keyBytes);
                return delCount == 1L;
            }
        });
        return result;
    }

    public boolean expire(final String key, final long seconds) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                return connection.expire(keyBytes, seconds);
            }
        });
        return result;
    }

    public boolean lPush(final String key, final Serializable value) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                byte[] valueBytes = SerializeUtils.serialize(value);
                long count = connection.lPush(keyBytes, valueBytes);
                return count == 1;
            }
        });
        return result;
    }

    public boolean lPush(final String key, final List<? extends Serializable> list) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                byte[][] valueBytes = new byte[list.size()][];
                for (int i = 0; i < list.size(); i++) {
                    Serializable value = list.get(i);
                    valueBytes[i] = SerializeUtils.serialize(value);
                }
                long count = connection.lPush(keyBytes, valueBytes);
                return count == list.size();
            }
        });
        return result;
    }

    public Serializable rPop(final String key) {
        Serializable result = redisTemplate.execute(new RedisCallback<Serializable>() {
            public Serializable doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                return SerializeUtils.deserialize(connection.rPop(keyBytes));
            }
        });
        return result;
    }

    public Long incr(final String key) {
        Long result = redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = key.getBytes();
                Long value = connection.incr(keyBytes);
                return value;
            }
        });
        return result;
    }

}
