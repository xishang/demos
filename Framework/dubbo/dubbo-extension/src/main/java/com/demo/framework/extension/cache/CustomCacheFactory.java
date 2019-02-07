package com.demo.framework.extension.cache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.support.AbstractCacheFactory;
import com.alibaba.dubbo.common.URL;

/**
 * @author xishang
 * @version 1.0
 * @see com.alibaba.dubbo.cache.support.lru.LruCacheFactory
 * @see com.alibaba.dubbo.cache.support.threadlocal.ThreadLocalCacheFactory
 * @see com.alibaba.dubbo.cache.support.jcache.JCacheFactory
 * @since 2018/6/25
 * <p>
 * 缓存工厂: 也可直接实现CacheFactory接口
 */
public class CustomCacheFactory extends AbstractCacheFactory {

    @Override
    protected Cache createCache(URL url) {
        return null;
    }

}
