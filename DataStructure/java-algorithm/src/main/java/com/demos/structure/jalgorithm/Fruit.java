package com.demos.structure.jalgorithm;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/17
 */
public class Fruit {

    {
        System.out.println("水果----");
    }

    static {
        System.out.println("水果----static");
    }

    public Fruit(String name) {
        System.out.println("水果----构造函数");
    }

}
