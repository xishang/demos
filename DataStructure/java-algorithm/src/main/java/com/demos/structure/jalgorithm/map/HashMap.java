package com.demos.structure.jalgorithm.map;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 自定义的HashMap简单实现
 */
public class HashMap<K, V> implements Map<K, V> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(K key) {
        return false;
    }

    @Override
    public boolean containsValue(V v) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public V get(K k) {
        return null;
    }

    @Override
    public void put(K k, V v) {

    }

    @Override
    public V remove(K k) {
        return null;
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry> entrySet() {
        return null;
    }

    static class HashEntry<K, V> implements Entry<K, V> {

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V value) {
            return null;
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {

        }

    }

}
