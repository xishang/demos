package com.demos.java.designpatterns.creational.singleton;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 饿汉模式: 类加载时创建
 */
public class HungrySingleton {

    private static final HungrySingleton INSTANCE = new HungrySingleton();

    private HungrySingleton() {

    }

    public static HungrySingleton getInstance() {
        return INSTANCE;
    }

    public void show() {
        System.out.println("Singleton实现: 饿汉模式");
    }

}
