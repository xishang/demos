package com.demos.java.basedemo.proxy.dynamic.my;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/3
 * <p>
 * 自定义ClassLoader, 添加加载class的方法
 * 原因: java.lang.ClassLoader加载class的方法为"protected"方法, 只能被子类或相同包下的对象访问
 */
public class MyClassLoader extends ClassLoader {

    private static MyClassLoader loader;

    private MyClassLoader() {

    }

    public static MyClassLoader getInstance() {
        if (loader == null) {
            synchronized (MyClassLoader.class) {
                // 得到锁首先检查loader是否已经存在, 避免重复创建
                if (loader == null) {
                    loader = new MyClassLoader();
                }
            }
        }
        return loader;
    }

    /**
     * 加载class文件
     *
     * @param filePath
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public Class<?> findClass(String filePath, String className) throws ClassNotFoundException {
        try {
            byte[] classBytes = Files.readAllBytes(Paths.get(filePath));
            Class<?> clazz = defineClass(className, classBytes, 0, classBytes.length);
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ClassNotFoundException(className);
    }

}
