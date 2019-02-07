package com.demos.java.framework.asm.util;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/15
 */
public class ClassLoaderUtils extends ClassLoader {

    private static ClassLoaderUtils loader = new ClassLoaderUtils();

    /**
     * 加载class文件
     *
     * @param className
     * @param classBytes
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> findClass(String className, byte[] classBytes) throws ClassNotFoundException {
        return loader.defineClass(className, classBytes, 0, classBytes.length);
    }

}
