package com.demos.java.designpatterns.creational.prototype;

import java.io.*;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 原型模式: 通过复制一个已经存在的实例来返回新的实例, 多用于创建复杂的或者耗时的实例
 * 抽象原型类
 */
public abstract class Document implements Cloneable, Serializable {

    protected String name;

    public Document(String name) {
        this.name = name;
    }

    public abstract void display();

    public Document clone() {
        try {
            return (Document) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Document deepClone() {
        try {
            // 将对象写到流里
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            // 从流里读回来
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (Document) ois.readObject();
        } catch (Exception e) {
            return null;
        }

    }

}
