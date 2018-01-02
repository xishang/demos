package com.demos.java.designpatterns.structural.composite;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 组合模式: 一种分区设计模式, 将对象“合成”到树结构中以表示部分-整体层次结构, 使客户端统一处理各个对象和组合
 * Component角色
 */
public interface Employee {

    void work();

    void add(Employee employee);

    void remove(Employee employee);

}
