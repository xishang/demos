package com.demos.java.designpatterns.structural.proxy;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 实现类
 */
public class RealSubject implements Subject {

    @Override
    public void doAction() {
        System.out.println("代理实现");
    }

}
