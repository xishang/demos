package com.demos.spring.completedemo.shiro.manage;

import com.demos.spring.completedemo.util.CacheUtils;
import com.demos.spring.completedemo.util.JsonMapper;
import com.demos.spring.completedemo.util.WebUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Set;

/**
 * 自定义redis实现的shiro缓存操作
 */
public class RedisCache<K, V> implements Cache<K, V> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String cacheKeyName = null;

    public RedisCache(String cacheKeyName) {
        this.cacheKeyName = cacheKeyName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) throws CacheException {
        if (key == null) {
            return null;
        }
        String field = getFieldJsonStr(key);
        V v = null;
        HttpServletRequest request = WebUtils.getRequest();
        if (request != null) {
            v = (V) request.getAttribute(cacheKeyName);
            if (v != null) {
                return v;
            }
        }

        V value = null;
        try {
            value = (V) CacheUtils.hget(cacheKeyName, field);
            logger.debug("get {} {} {}", cacheKeyName, field, request != null ? request.getRequestURI() : "");
        } catch (Exception e) {
            logger.error("get {} {} {}", cacheKeyName, field, request != null ? request.getRequestURI() : "", e);
        }

        if (request != null && value != null) {
            request.setAttribute(cacheKeyName, value);
        }

        return value;
    }

    @Override
    public V put(K key, V value) throws CacheException {
        if (key == null) {
            return null;
        }
        String field = getFieldJsonStr(key);
        try {
            CacheUtils.hset(cacheKeyName, field, value);
            logger.debug("put {} {} = {}", cacheKeyName, field, value);
        } catch (Exception e) {
            logger.error("put {} {}", cacheKeyName, field, e);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(K key) throws CacheException {
        V value = null;
        String field = getFieldJsonStr(key);
        try {
            value = (V) CacheUtils.hget(cacheKeyName, field);
            CacheUtils.hdel(cacheKeyName, field);
            logger.debug("remove {} {}", cacheKeyName, field);
        } catch (Exception e) {
            logger.warn("remove {} {}", cacheKeyName, field, e);
        }
        return value;
    }

    @Override
    public void clear() throws CacheException {
        try {
            CacheUtils.del(cacheKeyName);
            logger.debug("clear key={}", cacheKeyName);
        } catch (Exception e) {
            logger.error("clear {}", cacheKeyName, e);
        }
    }

    @Override
    public int size() {
        int size = 0;
        try {
            size = CacheUtils.hlen(cacheKeyName).intValue();
            logger.debug("size {} {} ", cacheKeyName, size);
            return size;
        } catch (Exception e) {
            logger.error("clear {}", cacheKeyName, e);
        }
        return size;
    }

    @Override
    public Set<K> keys() {
        try {
            logger.debug("keys {} ", cacheKeyName);
            Set<K> set = CacheUtils.hkeys(cacheKeyName);
            return set;
        } catch (Exception e) {
            logger.error("keys {}", cacheKeyName, e);
        }
        return Sets.newConcurrentHashSet();
    }

    @Override
    public Collection<V> values() {
        Collection<V> vals = Lists.newArrayList();
        try {
            logger.debug("values {} {} ", cacheKeyName, vals);
            return CacheUtils.hvals(cacheKeyName);
        } catch (Exception e) {
            logger.error("values {}", cacheKeyName, e);
        }
        return vals;
    }

    private String getFieldJsonStr(K key) {
        return JsonMapper.toJsonString(key);
    }

}
