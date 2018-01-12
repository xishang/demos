package com.demos.java.designpatterns.behavioral.observer;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/7
 * <p>
 * 具体观察者类
 */
public class ConcreteObserverA implements Observer {

    public ConcreteObserverA() {

    }

    /**
     * 构造方法: 传入要观察的目标, 并注册自己
     *
     * @param subject
     */
    public ConcreteObserverA(Subject subject) {
        subject.attach(this);
    }

    @Override
    public void update(Subject subject, Object arg) {
        System.out.println("观察者A更新");
    }

}
