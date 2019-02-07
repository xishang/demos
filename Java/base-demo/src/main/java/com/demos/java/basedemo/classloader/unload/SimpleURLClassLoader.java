package com.demos.java.basedemo.classloader.unload;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 */
public class SimpleURLClassLoader extends URLClassLoader {
    //工程class类所在的路径
    public static String projectClassPath = "E:/IDE/work_place/ZJob-Note/bin/";
    //所有的测试的类都在同一个包下
    public static String packagePath = "testjvm/testclassloader/";

    public SimpleURLClassLoader() {
        //设置ClassLoader加载的路径
        super(getMyURLs());
    }

    private static URL[] getMyURLs() {
        URL url = null;
        try {
            url = new File(projectClassPath).toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new URL[]{url};
    }

    public Class load(String name) throws Exception {
        return loadClass(name);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * 重写loadClass，不采用双亲委托机制("java."开头的类还是会由系统默认ClassLoader加载)
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        //查看HotSwapURLClassLoader实例缓存下，是否已经加载过class
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            return (clazz);
        }

        //如果类的包名为"java."开始，则有系统默认加载器AppClassLoader加载
        if (name.startsWith("java.")) {
            try {
                //得到系统默认的加载cl，即AppClassLoader
                ClassLoader system = ClassLoader.getSystemClassLoader();
                clazz = system.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }
        return customLoad(name, this);
    }

    /**
     * 自定义加载
     *
     * @param name
     * @param cl
     * @return
     * @throws ClassNotFoundException
     */
    public Class customLoad(String name, ClassLoader cl) throws ClassNotFoundException {
        return customLoad(name, false, cl);
    }

    /**
     * 自定义加载
     *
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    public Class customLoad(String name, boolean resolve, ClassLoader cl) throws ClassNotFoundException {
        //findClass()调用的是URLClassLoader里面重载了ClassLoader的findClass()方法
        Class clazz = ((SimpleURLClassLoader) cl).findClass(name);
        if (resolve)
            ((SimpleURLClassLoader) cl).resolveClass(clazz);
        return clazz;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
