package com.demo.java.jmx;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/23
 */
public class Looper {

    public static int counter = 0;

    public static void main(String[] args) throws Exception {
        while (true) {
            selfAdd();
            Thread.sleep(3000);
        }
    }

    public static void selfAdd() {
        System.out.println("counter=" + counter++);
    }

}
