/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/7
 * <p>
 * 设计模式6大原则:
 * 1.开闭原则(Open Closed Principle): 软件实体应对扩展开放，而对修改关闭. [最重要的面向对象设计原则, 抽象化是开闭原则的关键]
 * 2.单一职责原则(Single Responsibility Principle): 一个类只负责一个功能领域中的相应职责. [实现高内聚、低耦合的指导方针]
 * 3.里氏替换原则(Liskov Substitution Principle): 所有引用基类对象的地方能够透明地使用其子类的对象. [实现开闭原则的重要方式之一]
 * 4.依赖倒置原则(Dependence Inversion Principle): 抽象不应该依赖于细节, 细节应该依赖于抽象. [针对抽象层编程，而将具体类的对象通过依赖注入的方式注入到其他对象中]
 * => 开闭原则是目标, 里氏替换原则是基础, 依赖倒转原则是手段, 它们相辅相成, 相互补充, 只是分析问题时所站角度不同
 * 5.接口隔离原则(Interface Segregation Principle): 使用多个专门的接口, 而不使用单一的总接口. [使接口的职责单一]
 * 6.迪米特法则(Law Of Demeter): 一个软件实体应当尽可能少地与其他实体发生相互作用. [降低系统的耦合度]
 * 另: 合成复用原则(Composite Reuse Principle): 尽量使用对象组合, 而不是继承来达到复用的目的.
 * => 使用组合可以降低类与类之间的耦合度, 继承是"白箱"复用(基类的内部细节通常对子类来说是可见), 组合是"黑箱"复用. 主要还是应该考虑"Has-A"还是"Is-A"的关系
 */
package com.demos.java.designpatterns;
