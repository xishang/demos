package com.demos.java.basedemo.classloader.hotswap;

import java.lang.reflect.Method;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 */
public class MonitorHotSwap implements Runnable {
    // Hot就是用于修改，用来测试热加载
    private String className = "testjvm.testclassloader.Hot";
    private Class hotClazz = null;
    private HotSwapURLClassLoader hotSwapCL = null;

    @Override
    public void run() {
        try {
            while (true) {
                initLoad();
                Object hot = hotClazz.newInstance();
                Method m = hotClazz.getMethod("hot");
                m.invoke(hot, null); //打印出相关信息
                // 每隔10秒重新加载一次
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载class
     */
    public void initLoad() throws Exception {
        hotSwapCL = HotSwapURLClassLoader.getClassLoader();
        // 如果Hot类被修改了，那么会重新加载，hotClass也会返回新的
        hotClazz = hotSwapCL.loadClass(className);
    }
}
