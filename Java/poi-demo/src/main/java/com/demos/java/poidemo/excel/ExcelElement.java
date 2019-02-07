package com.demos.java.poidemo.excel;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/7
 * excel单元格值
 */
public class ExcelElement {

    // 行索引
    private int rowIndex;
    // 列索引
    private int colIndex;
    // 值
    private Object data;

    public ExcelElement(int rowIndex, int colIndex, Object data) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.data = data;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
