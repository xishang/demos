package com.demos.spring.completedemo.shiro.manage;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * 自定义授权缓存管理类--redis单机模式
 */
public class RedisCacheManager implements CacheManager {

    private String cacheKeyPrefix = "shiro_cache_";

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return new RedisCache<>(cacheKeyPrefix + name);
    }

    public String getCacheKeyPrefix() {
        return cacheKeyPrefix;
    }

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }

}
