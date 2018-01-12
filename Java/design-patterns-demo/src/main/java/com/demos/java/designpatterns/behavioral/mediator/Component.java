package com.demos.java.designpatterns.behavioral.mediator;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/6
 * <p>
 * 抽象同事类
 */
public abstract class Component {

    protected AbstractMediator mediator;

    public void setMediator(AbstractMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * 通知中介者组件改变事件
     */
    public void notifyChanged() {
        mediator.componentChanged(this);
    }

    public abstract void update();

}
