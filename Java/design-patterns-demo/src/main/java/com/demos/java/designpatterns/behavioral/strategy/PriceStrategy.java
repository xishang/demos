package com.demos.java.designpatterns.behavioral.strategy;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 策略模式: 针对一组算法, 用于算法的自由切换和扩展
 * 优点:
 * 1.完美支持"开闭原则", 用户可以在不修改原有系统的基础上, 灵活添加新的算法或行为
 * 2.抽象策略类定义了算法族, 使用继承可以避免代码重复
 * 3.策略模式可以避免多重条件(if-else)语句, 子类只实现算法部分, 将逻辑和算法分离, 符合"单一职责原则"
 * 缺点:
 * 1.客户端需要知道所有的策略类, 并自行决定使用哪一种策略类
 * 2.产生过多的具体策略类
 * <p>
 * 抽象策略类: 价格策略接口
 */
public interface PriceStrategy {

    double calculate(double price);

}
