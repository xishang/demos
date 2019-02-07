package com.demos.java.basedemo.classloader.hotswap;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 * <p>
 * 被用来热替换的类
 */
public class Hot {

    public void hot() {
        System.out.println("Hot class, classLoader: " + this.getClass().getClassLoader());
    }

}
