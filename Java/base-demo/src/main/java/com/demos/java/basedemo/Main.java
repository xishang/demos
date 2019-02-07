package com.demos.java.basedemo;

import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/23
 * <p>
 * 重写规则:
 * 1.重写方法不能比被重写方法限制有更严格的访问级别
 * 2.参数列表必须与被重写方法的相同(不能是子类型)
 * 3.返回类型必须与被重写方法的返回类型相同或者是其子类型
 * 4.重写方法不能抛出新的异常或者比被重写方法声明的检查异常更广的检查异常, 但是可以抛出更少、更有限或者不抛出异常(RuntimeException不受限制)
 * 5.不能重写被标识为final的方法
 * 6.不能重写无法被继承的方法, 如: private方法
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println(new Integer(10).toString());
        System.out.println(new Integer(10).hashCode());
        System.out.println(new Integer(10).equals(new Integer(10)));
        System.out.println(Integer.toHexString(20320));
        String a = new String("你好!");
        System.out.println(Character.SIZE);
        System.out.println(a.length());
        System.out.println(Arrays.toString(a.getBytes()));
        System.out.println(Arrays.toString(a.getBytes("utf-8")));
        System.out.println(Arrays.toString(a.getBytes("gbk")));
    }

}
