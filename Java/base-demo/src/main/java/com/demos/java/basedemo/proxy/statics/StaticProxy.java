package com.demos.java.basedemo.proxy.statics;

import com.demos.java.basedemo.proxy.bean.HistoryTeacher;
import com.demos.java.basedemo.proxy.bean.Teacher;

import java.io.IOException;

/**
 * 静态代理
 */
public class StaticProxy implements Teacher {

    private HistoryTeacher teacher;

    public StaticProxy() {
        teacher = new HistoryTeacher();
    }

    @Override
    public void teach(String lesson, long time) throws IOException, InterruptedException {
        System.out.println("-------------------老师好[静态代理]-------------------");
        teacher.teach(lesson, time);
        System.out.println("-------------------老师再见[静态代理]-------------------");
    }

    @Override
    public boolean leave(int days) {
        System.out.println("-------------------请假申请-------------------");
        boolean flag = teacher.leave(days);
        System.out.println("-------------------请假结束-------------------");
        return flag;
    }

}
