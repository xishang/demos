package com.demo.java.jmx;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/23
 */
public class AttachAgent {

    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("Attach Agent Version, args=" + args);

        try {
            Class targetMain = Class.forName("com.demos.java.basedemo.Main");
            Field counter = targetMain.getDeclaredField("counter");
            counter.setAccessible(true);
            System.out.println("Main counter=" + counter.get(targetMain));
            counter.set(targetMain, 1000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void agentmain(String args) {
        System.out.println();
    }

}
