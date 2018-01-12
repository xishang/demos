package com.demos.java.designpatterns.creational;

import com.demos.java.designpatterns.creational.builder.ConcreteBuilder;
import com.demos.java.designpatterns.creational.builder.Director;
import com.demos.java.designpatterns.creational.builder.Product;
import com.demos.java.designpatterns.creational.factory.AbstractFactory;
import com.demos.java.designpatterns.creational.factory.ButtonFactoryMethod;
import com.demos.java.designpatterns.creational.prototype.Document;
import com.demos.java.designpatterns.creational.prototype.PrototypeManager;
import com.demos.java.designpatterns.creational.singleton.EnumSingleton;
import com.demos.java.designpatterns.creational.singleton.HungrySingleton;
import com.demos.java.designpatterns.creational.singleton.InnerClassSingleton;
import com.demos.java.designpatterns.creational.singleton.LazySingleton;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 创建型
 */
public class Main {

    public static void main(String[] args) {
        // 单例模式
        HungrySingleton.getInstance().show();
        LazySingleton.getInstance().show();
        InnerClassSingleton.getInstance().show();
        EnumSingleton.INSTANCE.show();
        // 工厂方法模式
        ButtonFactoryMethod.createButton("Mac").click();
        ButtonFactoryMethod.createButton("Win").click();
        // 抽象工厂模式
        AbstractFactory macFactory = AbstractFactory.getInstance("Mac");
        macFactory.createBorder().show();
        macFactory.createButton().click();
        AbstractFactory winFactory = AbstractFactory.getInstance("Win");
        winFactory.createBorder().show();
        winFactory.createButton().click();
        // 建造者模式
        Product product = new Director(new ConcreteBuilder()).construct();
        System.out.println(product.getPartA() + "-" + product.getPartB() + "-" + product.getPartC());
        // 原型模式
        Document analysis1 = PrototypeManager.getInstance().clone("analysis");
        analysis1.display();
        Document analysis2 = PrototypeManager.getInstance().clone("analysis");
        analysis2.display();
        Document analysis3 = PrototypeManager.getInstance().deepClone("analysis");
        analysis3.display();
        Document analysis4 = PrototypeManager.getInstance().deepClone("analysis");
        analysis4.display();
    }

}
