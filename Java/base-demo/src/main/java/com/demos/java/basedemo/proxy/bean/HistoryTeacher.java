package com.demos.java.basedemo.proxy.bean;

import java.io.IOException;

public class HistoryTeacher implements Teacher {

    @Override
    public void teach(String lesson, long time) throws IOException, InterruptedException {
        System.out.println("-------------------教历史课-------------------");
    }

    @Override
    public boolean leave(int days) {
        return false;
    }

}
