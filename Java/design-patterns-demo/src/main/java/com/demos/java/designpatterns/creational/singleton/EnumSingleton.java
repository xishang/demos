package com.demos.java.designpatterns.creational.singleton;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 单元素枚举类型: Singleton最佳实现方式
 */
public enum EnumSingleton {

    INSTANCE;

    public void show() {
        System.out.println("Singleton实现: 单元素枚举类型");
    }

}
