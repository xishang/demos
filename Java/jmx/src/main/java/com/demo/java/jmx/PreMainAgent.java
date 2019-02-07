package com.demo.java.jmx;

import java.lang.instrument.Instrumentation;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/23
 */
public class PreMainAgent {

    public static void premain(String args, Instrumentation inst) {
        System.out.println("Agent load premain, args=" + args);
    }

    public static void premain(String args) {
        System.out.println();
    }

}
