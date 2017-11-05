package com.demos.java.basedemo.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Person {

    private String name;
    private int age;
    private int sex; // 0: male, 1:female

    public Person(String name, int age, int sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    // 生成Person列表
    public static List<Person> generateList(int count) {
        List<Person> list = new ArrayList<>();
        IntStream.range(1, count).forEach(n -> {
            String name = "person:" + n;
            int age = new Random().nextInt(20) + 10;
            int sex = age % 2;
            list.add(new Person(name, age, sex));
        });
        return list;
    }

}
