package com.demos.java.designpatterns.creational.singleton;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 懒汉模式: 第一次使用时创建, 双重检查锁
 */
public class LazySingleton {

    private static volatile LazySingleton INSTANCE = null;

    private LazySingleton() {
    }

    public static LazySingleton getInstance() {
        if (INSTANCE == null) {
            synchronized (LazySingleton.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LazySingleton();
                }
            }
        }
        return INSTANCE;
    }

    public void show() {
        System.out.println("Singleton实现: 懒汉模式");
    }

}
