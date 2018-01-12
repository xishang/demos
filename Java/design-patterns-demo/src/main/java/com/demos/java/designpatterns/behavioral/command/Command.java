package com.demos.java.designpatterns.behavioral.command;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/2
 * <p>
 * 命令模式: 命令的发送者和接收者解耦, 使系统具有更好的灵活性和可扩展性
 * 优点: 系统解耦, 容易添加新命令, 容易设计组合命令
 * 缺点: 过多的具体命令类
 * <p>
 * 命令接口类
 */
public interface Command {

    void execute();

}
