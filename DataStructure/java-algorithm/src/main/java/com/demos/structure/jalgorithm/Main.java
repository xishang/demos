package com.demos.structure.jalgorithm;

import com.demos.structure.jalgorithm.list.ArrayList;
import com.demos.structure.jalgorithm.list.LinkedList;
import com.demos.structure.jalgorithm.list.List;

import java.util.Iterator;

public class Main {

    public static void main(String[] args) throws Exception {
//        ListOperate operate = new ListOperate();
//        operate.calculateExpression("(10+15-3*4)*(5*(1+2)/(2+3))+2*(5-1)");
//        Class clazz = Class.forName("com.demos.structure.jalgorithm.Apple");
        System.out.println(System.getProperty("java.ext.dirs"));
        System.out.println(System.getProperty("java.class.path"));
        System.out.println(System.getProperty("java.home"));
        System.out.println(Main.class.getClassLoader().getClass());
        System.out.println(Main.class.getClassLoader().getParent().getClass());
        // ${java.home}/lib, 主要是rt.jar: 由BootstrapClassLoader加载, 由JVM直接控制, 没有对应的Class文件
        System.out.println(java.lang.invoke.MethodHandle.class.getClassLoader());
    }

    public static void testArrayList() {
        List<String> list = new ArrayList<>(1 << 2);
        testList(list);
    }

    public static void testLinkedList() {
        List<String> list = new LinkedList<>();
        testList(list);
    }

    public static void testList(List list) {
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");
        list.get(3);
        list.add(2, "insert");
        list.add("insert");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.equals("insert")) {
                iterator.remove();
            }
        }
        list.clear();
    }


}
