package com.demos.java.basedemo.invoke;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/18
 */
public class ExceptionHandler {

    public int parseAndAdd(String a, String b) {
        return Integer.parseInt(a) + Integer.parseInt(b);
    }

    public int handleException(Exception e/*, String a, String b*/) {
        System.out.println(e.getMessage());
        return 0;
    }

}
