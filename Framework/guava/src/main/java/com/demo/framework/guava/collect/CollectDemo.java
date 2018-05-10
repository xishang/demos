package com.demo.framework.guava.collect;

import com.google.common.base.Charsets;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/5
 */
public class CollectDemo {

    /**
     * Guava BloomFilter: 超大map过滤
     */
    public static void bloomFilter() {
        BloomFilter<String> existKeys = BloomFilter.create(
                Funnels.stringFunnel(Charsets.UTF_8),
                // 预计容量, 应尽量准确, 当插入数量接近容量时误判率会上升
                1024 * 1024,
                // 误判率
                0.0001D);
        for (int i = 0; i < 1024 * 512; i++) {
            existKeys.put("key-" + i);
        }
        System.out.println(existKeys.mightContain("key-1"));
        System.out.println(existKeys.mightContain("key--1"));
    }

    /**
     * Guava Intern
     */
    public static void stringIntern() {
        System.out.printf("ab = new(ab) ? %s\n", "ab" == new String("ab"));
        Interner<String> stringPool = Interners.newWeakInterner();
        String internStr = stringPool.intern("ab");
        String internNew = stringPool.intern(new String("ab"));
        System.out.printf("intern:ab = intern:new(ab) ? %s\n", internStr == internNew);
    }

    public static void main(String[] args) {
        bloomFilter();
        stringIntern();
    }

}
