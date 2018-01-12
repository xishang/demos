package com.demos.java.designpatterns.behavioral.state;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/8
 * <p>
 * 状态模式: 允许一个对象在其内部状态改变时改变它的行为, 对象看起来似乎修改了它的类
 * 别名: 状态对象模式(Pattern of Objects for States)
 * 优点: 状态模式将一个对象的状态从该对象中分离出来, 将不同状态下的不同行为封装在一个个具体状态类中, 状态转换的细节对客户端是透明的
 * 缺点: 增加了系统的复杂性, 新增状态类需要修改状态转换的源代码, 对"开闭原则"支持较差
 * <p>
 * 抽象状态类: 定义与环境类中某个特定状态相关的行为接口
 */
public abstract class AccountState {

    public abstract void handle(Account account);

}
