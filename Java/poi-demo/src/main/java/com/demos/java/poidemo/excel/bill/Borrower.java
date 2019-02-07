package com.demos.java.poidemo.excel.bill;

import com.demos.java.poidemo.excel.ExcelCell;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/7
 */
public class Borrower {

    @ExcelCell(cellIndex = 0)
    private String name;

    @ExcelCell(cellIndex = 1)
    private int age;

    @ExcelCell(cellIndex = 2)
    private String uuid;

    public Borrower(String name, int age, String uuid) {
        this.name = name;
        this.age = age;
        this.uuid = uuid;
    }

}
