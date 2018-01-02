package com.demos.java.designpatterns.creational.singleton;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 类级内部类来创建单例对象: 延迟加载和线程安全(JVM加载机制保证)
 */
public class InnerClassSingleton {

    private InnerClassSingleton() {

    }

    public static InnerClassSingleton getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 静态内部类, 第一次调用时加载. 有JVM保证线程安全
     */
    private static class SingletonHolder {
        private static InnerClassSingleton instance = new InnerClassSingleton();
    }

    public void show() {
        System.out.println("Singleton实现: 类级内部类");
    }

}
