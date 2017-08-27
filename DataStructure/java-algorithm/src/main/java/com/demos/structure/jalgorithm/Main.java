package com.demos.structure.jalgorithm;

import com.demos.structure.jalgorithm.list.ArrayList;
import com.demos.structure.jalgorithm.list.LinkedList;
import com.demos.structure.jalgorithm.list.List;

import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        testLinkedList();
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
