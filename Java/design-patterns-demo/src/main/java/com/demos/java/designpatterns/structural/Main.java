package com.demos.java.designpatterns.structural;

import com.demos.java.designpatterns.structural.adapter.Adaptee;
import com.demos.java.designpatterns.structural.adapter.ClassAdapter;
import com.demos.java.designpatterns.structural.adapter.ObjectAdapter;
import com.demos.java.designpatterns.structural.adapter.Target;
import com.demos.java.designpatterns.structural.bridge.*;
import com.demos.java.designpatterns.structural.composite.Developer;
import com.demos.java.designpatterns.structural.composite.Manager;
import com.demos.java.designpatterns.structural.decorator.Component;
import com.demos.java.designpatterns.structural.decorator.ConcreteComponent;
import com.demos.java.designpatterns.structural.decorator.ConcreteDecoratorA;
import com.demos.java.designpatterns.structural.decorator.ConcreteDecoratorB;
import com.demos.java.designpatterns.structural.facade.Computer;
import com.demos.java.designpatterns.structural.flyweight.GoChess;
import com.demos.java.designpatterns.structural.flyweight.GoChessFactory;
import com.demos.java.designpatterns.structural.proxy.Proxy;
import com.demos.java.designpatterns.structural.proxy.Subject;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 */
public class Main {

    public static void main(String[] args) {
        // 代理模式: 客户不知道Proxy代理了其他的类
        Subject proxy = new Proxy();
        proxy.doAction();
        // 装饰器模式: A装饰B, B装饰具体组件
        Component component = new ConcreteDecoratorA(new ConcreteDecoratorB(new ConcreteComponent()));
        component.operation();
        // 适配器模式: 类适配器
        Target target1 = new ClassAdapter();
        target1.request();
        // 适配器模式: 对象适配器
        Target target2 = new ObjectAdapter(new Adaptee());
        target2.request();
        // 桥接模式: 解耦抽象与行为
        Shape triangle1 = new Triangle(new RedDrawAPI());
        triangle1.apply();
        Shape triangle2 = new Triangle(new BlackDrawAPI());
        triangle2.apply();
        Shape rectangle1 = new Rectangle(new RedDrawAPI());
        rectangle1.apply();
        Shape rectangle2 = new Rectangle(new BlackDrawAPI());
        rectangle2.apply();
        // 组合模式: 部分-整体层次结构
        Manager manager = new Manager();
        manager.add(new Developer());
        manager.add(new Developer());
        manager.work();
        // 门面模式: 提供统一高层接口
        Computer computer = new Computer();
        computer.start();
        // 享元模式
        GoChess black1 = GoChessFactory.getInstance().getGoChess("black");
        System.out.println("棋子1: " + black1.display());
        GoChess black2 = GoChessFactory.getInstance().getGoChess("black");
        System.out.println("棋子2: " + black2.display());
        System.out.println("棋子1 == 棋子2 : " + (black1 == black2));
    }

}
