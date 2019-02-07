package com.demo.java.jmx;

import com.sun.tools.attach.VirtualMachine;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/23
 */
public class AttachMain {

    public static void main(String[] args) throws Exception {
        String targetPid = args[0];
        String agentArgs = args[1];
        System.out.println("targetPid=" + targetPid + ", agentArgs=" + agentArgs);
        VirtualMachine vm = VirtualMachine.attach(targetPid);
        vm.loadAgent("/Users/xishang/IdeaProjects/demos/Java/jmx/target/jmx.jar", agentArgs);
    }

}
