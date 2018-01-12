package com.demos.java.designpatterns.behavioral.memento;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/10
 * <p>
 * 备忘录模式: 在不破坏封装的前提下, 捕获一个对象的内部状态, 并在该对象之外保存这个状态, 以便在适当的时候将对象恢复到原先保存的状态
 * 别名: 快照模式(Snapshot Pattern)、Token模式
 * 实现方式:
 * => 白箱实现: 备忘录角色对任何对象都提供一个接口, 即宽接口, 备忘录角色的内部所存储的状态就对所有对象公开
 * => 黑箱实现: 备忘录角色对发起人(Originator)对象提供一个宽接口, 而为其他对象提供一个窄接口
 * 多重检查点: 备忘录存储一个历史状态的列表, 用来恢复一个指定的历史状态
 * 自述历史模式(History-On-Self Pattern): 备忘录模式的一个变种, 发起人(Originator)兼任负责人(Caretaker)角色
 * 适用场景: 提供撤销操作, 如: 字处理软件、图像编辑软件、数据库管理系统等
 * <p>
 * 备忘录: 仅为标记接口, 不提供访问或修改状态的方法
 */
public interface Memento {
}
