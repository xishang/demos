package com.demos.java.basedemo.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/24
 */
public class PerformanceTest {

    private static Animal animal = new Animal();

    public static void main(String[] args) throws Throwable {
        MethodType methodType = MethodType.methodType(void.class, String.class);
        MethodHandle methodHandle = MethodHandles.lookup().findVirtual(Animal.class, "setName", methodType);
        MethodHandle setter = MethodHandles.lookup().findSetter(Animal.class, "name", String.class);
        Method method = Animal.class.getMethod("setName", String.class);
        testSetter();
        testInvokeExact(methodHandle);
        testInvoke(methodHandle);
        testSetterInvoke(setter);
        testSetterInvokeExact(setter);
        testReflect(method);
    }

    public static void testSetter() throws Throwable {
        long startTs = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            animal.setName("鸟");
        }
        System.out.println("set: " + (System.currentTimeMillis() - startTs));
    }

    public static void testSetterInvoke(MethodHandle methodHandle) throws Throwable {
        long startTs = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            methodHandle.invoke(animal, "鸟");
        }
        System.out.println("MethodHandle setter invoke: " + (System.currentTimeMillis() - startTs));
    }

    public static void testSetterInvokeExact(MethodHandle methodHandle) throws Throwable {
        long startTs = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            methodHandle.invokeExact(animal, "鸟");
        }
        System.out.println("MethodHandle setter invokeExact: " + (System.currentTimeMillis() - startTs));
    }

    public static void testInvokeExact(MethodHandle methodHandle) throws Throwable {
        long startTs = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            methodHandle.invokeExact(animal, "鸟");
        }
        System.out.println("MethodHandle invokeExact: " + (System.currentTimeMillis() - startTs));
    }

    public static void testInvoke(MethodHandle methodHandle) throws Throwable {
        long startTs = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            methodHandle.invoke(animal, "鸟");
        }
        System.out.println("MethodHandle invoke: " + (System.currentTimeMillis() - startTs));
    }

    public static void testReflect(Method method) throws Throwable {
        long startTs = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            method.invoke(animal, "鸟");
        }
        System.out.println("Reflect invoke: " + (System.currentTimeMillis() - startTs));
    }

}
