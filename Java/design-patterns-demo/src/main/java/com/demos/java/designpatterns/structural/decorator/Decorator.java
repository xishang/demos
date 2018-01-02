package com.demos.java.designpatterns.structural.decorator;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 装饰器(可传递: 本身类型作为构造参数)
 * 优点: 需要新的功能时, 只需实现一个新的具体装饰器类, 就可以用来装饰所有不同的具体组件
 * 缺点: 使用中会产生更多的对象
 */
public abstract class Decorator implements Component {

    private Component component;

    public Decorator(Component component) { // 装饰器: 传入需要装饰的Component
        this.component = component;
    }

    public void operation() {
        component.operation();
    }

}
