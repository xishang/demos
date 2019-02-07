package com.demos.java.basedemo.classloader.unload;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 * <p>
 * Class被回收的条件:
 * -> 该类所有的实例都已经被GC
 * -> 加载该类的ClassLoader实例已经被GC
 * -> 该类的java.lang.Class对象没有在任何地方被引用
 */
public class TestClassUnLoad {

    public static void main(String[] args) throws Exception {
        SimpleURLClassLoader loader = new SimpleURLClassLoader();
        // 用自定义的加载器加载A
        Class clazzA = loader.load("testjvm.testclassloader.A");
        Object a = clazzA.newInstance();
        // 清除相关引用
        a = null;
        clazzA = null;
        loader = null;
        // 执行一次gc垃圾回收
        System.gc();
        System.out.println("GC over");
    }

}
