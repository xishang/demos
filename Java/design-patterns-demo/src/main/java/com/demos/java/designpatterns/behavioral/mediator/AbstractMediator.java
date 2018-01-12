package com.demos.java.designpatterns.behavioral.mediator;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 中介者模式: 将网状的系统结构变成以中介者对象为中心的星形结构
 * 优点: 简化了同事类对象的交互, 将同事类之间解耦, 可以更灵活地添加或删除同事类, 符合"开闭原则"
 * 缺点: 具体中介者类包含了大量同事类之间的交互细节, 导致具体中介者类过于复杂
 * 适用场景: GUI应用软件等类与类之间关联关系比较复杂的系统
 * 注意事项: 中介者模式只适合于类之间关系特别复杂的场景, 避免滥用
 * <p>
 * 抽象中介者类: 定义同事类之间交互的接口
 */
public abstract class AbstractMediator {

    /**
     * 组件变化后更新操作
     *
     * @param component
     */
    public abstract void componentChanged(Component component);

}
