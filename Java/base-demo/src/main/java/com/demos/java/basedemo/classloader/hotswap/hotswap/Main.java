package com.demos.java.basedemo.classloader.hotswap.hotswap;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/7
 */
public class Main {

    public static final AtomicLong modifiedTime = new AtomicLong(System.currentTimeMillis());

    public static void main(String[] args) {
        while (!Thread.currentThread().isInterrupted()) {
            hotSwap();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("interrupted: " + Thread.currentThread().isInterrupted());
                Thread.interrupted();
            }
        }
    }

    public static void hotSwap() {
        // 1.获取当前路径下需要热替换的class文件
        File classFile = new File("Foo.class");
        System.out.println(classFile.getAbsolutePath());
        // 2.文件是否修改(比较文件时间戳)
        long lastModifiedTime = modifiedTime.get();
        if (lastModifiedTime == classFile.lastModified()) { // 文件未修改
            return;
        }
        modifiedTime.set(System.currentTimeMillis());
    }

    public static void invokeMethod() {
//        try {
//            // 每次都创建出一个新的类加载器
//            HowswapCL cl = new HowswapCL("../swap", new String[]{"Foo"});
//            Class cls = cl.loadClass("Foo");
//            Object foo = cls.newInstance();
//
//            Method m = foo.getClass().getMethod("sayHello", new Class[]{});
//            m.invoke(foo, new Object[]{});
//
//        }  catch(Exception ex) {
//            ex.printStackTrace();
//        }
    }

}
