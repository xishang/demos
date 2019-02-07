package com.demos.structure.jalgorithm.map;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 */
public interface Map<K, V> {

    int size();

    boolean isEmpty();

    boolean containsKey(K key);

    boolean containsValue(V v);

    void clear();

    V get(K k);

    void put(K k, V v);

    V remove(K k);

    Set<K> keySet();

    Collection<V> values();

    Set<Map.Entry> entrySet();

    interface Entry<K, V> {

        K getKey();

        V getValue();

        V setValue(V value);

        void forEach(BiConsumer<? super K, ? super V> action);
    }

}
