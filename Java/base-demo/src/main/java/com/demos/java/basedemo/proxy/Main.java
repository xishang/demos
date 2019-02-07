package com.demos.java.basedemo.proxy;

import com.demos.java.basedemo.proxy.bean.HistoryTeacher;
import com.demos.java.basedemo.proxy.bean.Teacher;
import com.demos.java.basedemo.proxy.dynamic.cglib.CGLibDynamicProxy;
import com.demos.java.basedemo.proxy.dynamic.jdk.JdkDynamicProxy;
import com.demos.java.basedemo.proxy.dynamic.my.MyDynamicProxy;
import com.demos.java.basedemo.proxy.dynamic.my.MyInvocationHandler;
import com.demos.java.basedemo.proxy.statics.StaticProxy;
import sun.misc.ProxyGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public class Main {

    public static void main(String[] args) throws Exception {
        /*-------------------静态代理 start-------------------*/
        Teacher proxy1 = new StaticProxy();
        proxy1.teach("", 1l);
        /*-------------------静态代理 end-------------------*/

        /*-------------------动态代理-JDK start-------------------*/
        Teacher proxy2 = (Teacher) Proxy.newProxyInstance(Teacher.class.getClassLoader(),
                new Class[]{Teacher.class}, new JdkDynamicProxy(new HistoryTeacher()));
        proxy2.teach("", 1l);
        // 打印JDK动态代理生成文件
//        outputJdkProxyFile();
        /*-------------------动态代理-JDK end-------------------*/

        /*-------------------动态代理-CGLib start-------------------*/
        HistoryTeacher proxy3 = CGLibDynamicProxy.newInstance(new HistoryTeacher());
        proxy3.teach("", 1l);
        /*-------------------动态代理-CGLib end-------------------*/

        /*-------------------动态代理-自定义 start-------------------*/
        Teacher proxy4 = MyDynamicProxy.newProxyInstance(Teacher.class, new MyInvocationHandler(new HistoryTeacher()));
        proxy4.teach("", 1l);
        System.out.println("请假结果: " + proxy4.leave(2));
        /*-------------------动态代理-自定义 end-------------------*/
    }

    /**
     * 打印JDK动态代理生成的代理文件
     * 核心代码: ProxyGenerator.generateProxyClass()
     */
    public static void outputJdkProxyFile() throws Exception {
        byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
                "com.test.$proxy0.class", new Class[]{Teacher.class}, Modifier.FINAL);
        FileOutputStream out = new FileOutputStream(new File("/Users/xishang/temp/$proxy0.class"));
        out.write(proxyClassFile);
        out.flush();
        out.close();
    }

}
