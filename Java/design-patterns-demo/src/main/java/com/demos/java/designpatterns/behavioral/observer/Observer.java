package com.demos.java.designpatterns.behavioral.observer;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/7
 * <p>
 * 观察者模式: 建立一种对象与对象之间的依赖关系, 一个对象发生改变时将自动通知其他对象
 * 别名:
 * 1.发布-订阅(Publish-Subscribe)模式
 * 2.模型-视图(Model-View)模式
 * 3.源-监听器(Source-Listener)模式
 * 4.从属者(Dependents)模式
 * 模型分类:
 * 1.推模型: 主题对象向观察者推送主题的详细信息, 不管观察者是否需要, 推送的信息通常是主题对象的全部或部分数据
 * => 推模型假定主题对象知道观察者需要的数据, 可能使得观察者对象难以复用
 * 2.拉模型: 主题对象在通知观察者的时候只传递少量信息, 由观察者主动到主题对象中获取详细
 * => 拉模型把自身传递给观察者, 让观察者自己去按需要取值
 * <p>
 * 抽象观察者类
 */
public interface Observer {

    /**
     * 使用拉模型: 传递目标对象和状态信息
     *
     * @param subject 观察目标对象
     * @param arg     状态参数
     */
    void update(Subject subject, Object arg);

}
