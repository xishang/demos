package com.demos.spring.completedemo.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义主键生成策略
 */
public class MyKeyGenerator extends SimpleKeyGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 生成缓存的key
     */
    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params == null || params.length == 0) {
            return SimpleKey.EMPTY;
        }
        StringBuilder buffer = new StringBuilder();
        Class<? extends Object> entityClass = target.getClass();
        buffer.append(entityClass.getName()).append(":");
        for (Object obj : params) {
            if (obj != null) {
                if (obj instanceof AtomicInteger || obj instanceof AtomicLong || obj instanceof BigDecimal
                        || obj instanceof BigInteger || obj instanceof Byte || obj instanceof Double
                        || obj instanceof Float || obj instanceof Integer || obj instanceof Long
                        || obj instanceof Short) {
                    buffer.append(obj);
                } else if (obj instanceof List || obj instanceof Set || obj instanceof Map) {
                    buffer.append(obj);
                } else {
                    buffer.append(obj.hashCode());
                }
            }
        }
        logger.debug("key-buffer:{}, method:{}", buffer.toString(), method.getName());
        return buffer.toString().hashCode();
    }

}  