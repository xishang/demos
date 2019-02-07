package com.demos.structure.jalgorithm;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/17
 */
public class Apple extends Fruit {

    {
        System.out.println("苹果----");
    }

    static {
        System.out.println("苹果----static");
    }

    public Apple(String name) {
        super("");
        System.out.println("苹果----构造函数");
    }

    public static void main(String[] args) {
        System.out.println("开始");
    }

}
