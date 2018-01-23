package com.demos.java.basedemo.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/16
 */
public class Bird extends Animal implements Flyable {

    public static int age = 2;

    public String name = "bird";

    public Bird(String name) {
        this.name = name;
    }

    @Override
    public void fly() {
        System.out.println("Bird can fly!");
    }

    @Override
    public void run() {
        System.out.println("Bird can run!");
    }

    public int sleep() {
        System.out.println("Bird is sleeping!");
        return 0;
    }

    public Object generic(Object num, Object desc) {
        System.out.println("Bird generic test");
        return null;
    }

    public void varargs(int num, String desc, int[] args) {
        System.out.println("Bird varargs test");
    }

    private void eat() {
        System.out.println("Bird is eating!");
    }

    public MethodHandle getEatMethodHandle() throws Throwable {
        MethodType mt = MethodType.fromMethodDescriptorString("()V", ClassLoader.getSystemClassLoader());
        MethodHandle mh = MethodHandles.lookup().findSpecial(Bird.class, "eat", mt, Bird.class);
        return mh;
    }

    public MethodHandle unreflectEatMethod() throws Throwable {
        // 反射Method(private) -> MethodHandle, 与findSpecial类似, 最后也要传入一个调用点类型以验证访问权限
        Method privateMethod = Bird.class.getDeclaredMethod("eat");
        return MethodHandles.lookup().unreflectSpecial(privateMethod, Bird.class);
    }

    public void invokeSpecial() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType voidReturn = MethodType.methodType(void.class);
        try {
            // invokespecial: 访问父类方法
            MethodHandle mh = lookup.findSpecial(Animal.class, "run", voidReturn, this.getClass());
            mh.invokeWithArguments(this);
            // 访问private方法
            MethodHandle mh2 = lookup.findSpecial(Bird.class, "eat", voidReturn, this.getClass());
            mh2.invoke(this);
            // 访问其他方法
            MethodHandle mh3 = lookup.findSpecial(Bird.class, "sleep", MethodType.methodType(int.class), this.getClass());
            mh3.invoke(this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void sing(String song) {
        System.out.println("Bird can sing " + song);
    }

}
