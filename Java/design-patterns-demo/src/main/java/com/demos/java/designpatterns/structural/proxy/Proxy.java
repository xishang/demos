package com.demos.java.designpatterns.structural.proxy;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 代理类
 */
public class Proxy implements Subject {

    private RealSubject subject;

    public Proxy() { // 静态代理: 不需要外部传入Subject
        subject = new RealSubject();
    }

    @Override
    public void doAction() {
        System.out.println("代理开始 -----------");
        subject.doAction();
        System.out.println("代理结束 -----------");
    }

}
