package com.demos.java.poidemo.util.excel;

import com.itextpdf.text.Document;

import java.io.OutputStream;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/6
 */
public class PdfTool {

    protected Document document;

    protected OutputStream os;

    public Document getDocument() {
        if (document == null) {
            document = new Document();
        }
        return document;
    }

}
