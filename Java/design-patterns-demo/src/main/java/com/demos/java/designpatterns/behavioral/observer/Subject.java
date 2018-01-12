package com.demos.java.designpatterns.behavioral.observer;

import java.util.Vector;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/7
 * <p>
 * 抽象目标类
 */
public abstract class Subject {

    // 观察者集合
    protected Vector<Observer> observers = new Vector<>();

    /**
     * 注册观察者对象
     *
     * @param observer
     */
    public void attach(Observer observer) {
        observers.add(observer);
    }

    /**
     * 注销观察者对象
     *
     * @param observer
     */
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * 通知观察者更新
     */
    public void notifyObservers() {
        notifyObservers(null);
    }

    /**
     * 通知观察者更新(带上状态信息)
     *
     * @param arg
     */
    public void notifyObservers(Object arg) {
        for (Observer observer : observers) {
            observer.update(this, arg);
        }
    }

}
