package com.demos.java.poidemo.util.excel;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/6
 */
public class FontUtils {

    public static Font getChineseFont(float size, BaseColor color) {
        Font font = null;
        try {
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            font = new Font(bf, size, Font.NORMAL);// 设置字体大小
        } catch (Exception e) {
            e.printStackTrace();
        }
        font.setColor(color);
        return font;
    }

}
