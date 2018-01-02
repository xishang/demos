package com.demos.java.designpatterns.structural.composite;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 组合模式: Leaf
 */
public class Developer implements Employee {

    @Override
    public void work() {
        System.out.println("Developer执行任务");
    }

    @Override
    public void add(Employee employee) {

    }

    @Override
    public void remove(Employee employee) {

    }

}
