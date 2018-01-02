package com.demos.java.designpatterns.creational.builder;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 建造者模式: 将复杂对象的构造与其表示分开, 使得同样的构建过程可以创建不同的表示
 * 抽象构建类: 定义了构建产品部件的方法
 */
public abstract class Builder {

    protected Product product = new Product();

    public abstract void buildPartA();

    public abstract void buildPartB();

    public abstract void buildPartC();

    public Product getResult() {
        return product;
    }

}
