package com.demos.java.basedemo.reflect;

import com.demos.java.basedemo.proxy.bean.Teacher;

import javax.annotation.Resource;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/4
 * <p>
 * 课程信息: 用于反射示例
 */
public final class Lesson {

    private String name;
    private Integer count;
    private Teacher teacher;

    public Lesson(String name, Integer count, Teacher teacher) {
        this.name = name;
        this.count = count;
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        return String.format("name: %s, count: %s, teacher: %s", name, count, teacher.getClass().getName());
    }

    @Resource
    public void teach(int num) throws InterruptedException {
        System.out.println(name + " begin!");
    }

}
