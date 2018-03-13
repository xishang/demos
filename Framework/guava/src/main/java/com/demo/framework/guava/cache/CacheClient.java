package com.demo.framework.guava.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/19
 */
public class CacheClient<K, V> {

    @FunctionalInterface
    public interface DataLoader<K, V> {
        V load(K key);
    }

    // 缓存接口LoadingCache在缓存项不存在时可以自动加载缓存
    LoadingCache<K, V> loadingCache;

    public CacheClient(DataLoader<K, V> loader) {
        loadingCache = CacheBuilder.newBuilder()
                // 设置并发级别, 并发级别是指可以同时写缓存的线程数
                .concurrencyLevel(8)
                // 设置写缓存后过期时间
                .expireAfterWrite(300, TimeUnit.SECONDS)
                // 设置缓存容器的初始容量为10
                .initialCapacity(10)
                // 设置缓存最大容量为100, 超过100之后就会按照LRU最近虽少使用算法来移除缓存项
                .maximumSize(Integer.MAX_VALUE)
                // 设置要统计缓存的命中率
                .recordStats()
                // 设置缓存的移除通知
                .removalListener(notification -> {
                    System.out.println(notification.getKey() + " was removed, cause is " + notification.getCause());
                })
                // build方法中可以指定CacheLoader, 在缓存不存在时通过CacheLoader的实现自动加载缓存
                .build(new CacheLoader<K, V>() {
                           @Override
                           public V load(K key) throws Exception {
                               return loader.load(key);
                           }
                       }
                );
    }

    /**
     * 根据key获取value
     *
     * @param key
     * @return
     */
    public V get(K key) {
        V value = null;
        try {
            value = loadingCache.get(key);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            // 允许cache为null
            System.out.println("Guava get Cache为null, key=" + key);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 根据key获取value, 若不存在则value由Callable提供
     *
     * @param key
     * @param c
     * @return
     */
    public V get(K key, Callable<V> c) {
        V value = null;
        try {
            value = loadingCache.get(key, c);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 根据key设置value
     *
     * @param key
     * @param value
     */
    public void set(K key, V value) {
        loadingCache.put(key, value);
    }

    /**
     * 直接删除
     *
     * @param key
     */
    public void delete(K key) {
        loadingCache.invalidate(key);
    }

    /**
     * 先获取新值, 然后更新
     *
     * @param key
     */
    public void refresh(K key) {
        loadingCache.refresh(key);
    }

}
