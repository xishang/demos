package com.demos.java.poidemo.util.excel;

import java.io.InputStream;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/6
 */
public class ExcelObject {

    /**
     * 锚名称
     */
    private String anchorName;
    /**
     * Excel Stream
     */
    private InputStream inputStream;
    /**
     * POI Excel
     */
    private Excel excel;

    public ExcelObject(InputStream inputStream) {
        this.inputStream = inputStream;
        this.excel = new Excel(this.inputStream);
    }

    public ExcelObject(String anchorName, InputStream inputStream) {
        this.anchorName = anchorName;
        this.inputStream = inputStream;
        this.excel = new Excel(this.inputStream);
    }

    public String getAnchorName() {
        return anchorName;
    }

    public void setAnchorName(String anchorName) {
        this.anchorName = anchorName;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    Excel getExcel() {
        return excel;
    }

}
