package com.demos.java.basedemo.proxy.dynamic.cglib;

import net.sf.cglib.core.AbstractClassGenerator;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyEnhancer extends Enhancer {

    // 自定义create(), 使用自定义的MyEnhancer类
    public static Object create1(Class type, Callback callback) {
        Enhancer e = new MyEnhancer();
        e.setSuperclass(type);
        e.setCallback(callback);
        return e.create();
    }

    @Override
    protected Class generate(AbstractClassGenerator.ClassLoaderData data) {
        // 主业务逻辑
        Class clazz = super.generate(data);
        // 输出CGLib生成的字节码文件
        FileOutputStream out = null;
        try {
            byte[] b = getStrategy().generate(this);
            out = new FileOutputStream(new File("/Users/xishang/temp/$proxy1.class"));
            out.write(b);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return clazz;
    }

}
