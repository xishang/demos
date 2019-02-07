package com.demos.java.basedemo.classloader.hotswap;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 */
public class TestHotSwap {

    public static void main(String[] args) throws Exception {
        //开启线程，如果class文件有修改，就热替换
        Thread t = new Thread(new MonitorHotSwap());
        t.start();
    }

}
