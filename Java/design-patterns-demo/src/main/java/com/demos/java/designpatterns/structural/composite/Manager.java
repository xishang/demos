package com.demos.java.designpatterns.structural.composite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/22
 * <p>
 * 组合模式: Composite
 */
public class Manager implements Employee {

    private List<Employee> employees = new ArrayList<>();

    @Override
    public void work() {
        System.out.println("Manager分配任务");
        for (Employee employee : employees) {
            employee.work();
        }
    }

    @Override
    public void add(Employee employee) {
        employees.add(employee);
    }

    @Override
    public void remove(Employee employee) {
        employees.remove(employee);
    }

}
