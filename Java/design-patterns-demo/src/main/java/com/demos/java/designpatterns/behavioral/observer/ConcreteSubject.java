package com.demos.java.designpatterns.behavioral.observer;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/7
 * <p>
 * 具体目标类
 */
public class ConcreteSubject extends Subject {

    private int status = 0;

    public void setStatus(int status) {
        if (this.status != status) {
            // 更新并通知
            System.out.println("--- 目标状态更新, 通知观察者 ---");
            this.status = status;
            this.notifyObservers(status);
        }
    }

}
