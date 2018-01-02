package com.demos.java.designpatterns.structural.facade;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 门面模式的模块
 */
public class CPU {

    public void freeze() {
        System.out.println("CPU freeze");
    }

    public void jump() {
        System.out.println("CPU jump");
    }

    public void execute() {
        System.out.println("CPU execute");
    }

}
