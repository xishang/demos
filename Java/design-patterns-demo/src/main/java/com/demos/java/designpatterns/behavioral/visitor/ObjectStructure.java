package com.demos.java.designpatterns.behavioral.visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/11
 * <p>
 * 对象结构: 存放元素对象, 并且提供遍历其内部元素的方法
 */
public class ObjectStructure {

    private List<CarElement> list = new ArrayList<>();

    public void accept(Visitor visitor) {
        for (CarElement element : list) {
            element.accept(visitor);
        }
    }

    public void addElement(CarElement element) {
        list.add(element);
    }

    public void removeElement(CarElement element) {
        list.remove(element);
    }

}
