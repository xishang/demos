package com.demos.java.basedemo.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/1
 */
public class WeakDemo {

    // 引用队列
    private static final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    public static void main(String[] args) {
        testWeakReference();
        testWeakHashMap();
    }

    public static void testWeakReference() {
        Holder holder = new Holder("value");
        WeakReference<Holder> reference = new WeakReference<>(holder, queue);
        System.out.println("reference.get = " + reference.get());
        handle(reference);
        holder = null;
        System.out.println(holder);
        // holder设置为null: 此时Holder对象已不存在强引用, 只存在弱引用, GC时会将引用加入引用队列
        handle(reference);
    }

    private static void handle(WeakReference<Holder> reference) {
        System.out.println("before tryGet: " + reference.get());
        System.gc();
        System.out.println("after tryGet: " + reference.get());
        Object x = queue.poll();
        if (x != null) {
            // 对象被回收之后会将Reference加入queue
            WeakReference<Holder> ref = (WeakReference<Holder>) x;
            System.out.println("queue ref = " + ref);
        }
    }

    /**
     * === WeakHashMap中的Entry继承`WeakReference`, 回收后会将Entry加入`ReferenceQueue`
     * -> 在`size()`, `getTable() -> get, set`等方法时都会将`ReferenceQueue`中Entry的value设置null
     * === 使用ThreadLocal时, 每个Thread对应的ThreadLocalMap的Entry也是继承`WeakReference`
     * -> 在`put`等方法会清理key为null(已被回收)的Entry
     */
    public static void testWeakHashMap() {
        // WeakHashMap中的Entry继承`WeakReference`, 回收后会将Entry加入`ReferenceQueue`
        //
        WeakHashMap<Holder, Integer> weakHashMap = new WeakHashMap<>();
        weakHashMap.put(new Holder("key-1"), 1);
        weakHashMap.put(new Holder("key-2"), 2);
        weakHashMap(weakHashMap);
    }

    public static void weakHashMap(WeakHashMap weakHashMap) {
        System.out.println("before gc: " + weakHashMap.size()); // 2
        System.gc();
        System.out.println("after gc: " + weakHashMap.size()); // 0
    }

}
