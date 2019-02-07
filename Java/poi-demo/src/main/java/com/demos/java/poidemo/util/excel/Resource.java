package com.demos.java.poidemo.util.excel;

import com.itextpdf.text.pdf.BaseFont;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/6
 */
public class Resource {
    /**
     * 中文字体支持
     */

    protected static BaseFont BASE_FONT_CHINESE;


    static {
        try {
            BASE_FONT_CHINESE = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
