package com.demos.java.framework.asm.reflect;

import com.demos.java.framework.asm.Teacher;
import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/16
 * <p>
 * reflect asm
 */
public class ReflectASM {

    public static void main(String[] args) {

        MethodAccess ma = MethodAccess.get(Teacher.class);
//        ma.invoke()
    }

}
