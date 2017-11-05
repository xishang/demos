package com.demos.java.basedemo.proxy.bean;

import java.io.IOException;

public interface Teacher {

    void teach(String lesson, long time) throws IOException, InterruptedException;

    boolean leave(int days);

}
