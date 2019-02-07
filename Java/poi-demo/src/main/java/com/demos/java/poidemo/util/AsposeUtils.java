package com.demos.java.poidemo.util;

import com.aspose.cells.License;
import com.aspose.cells.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/6
 */
public class AsposeUtils {

    private static boolean getLicense() {
        boolean result = false;
        try {
            InputStream is = AsposeUtils.class.getClassLoader().getResourceAsStream("license.xml"); //  license.xml应放在..\WebRoot\WEB-INF\classes路径下
            License aposeLic = new License();
            aposeLic.setLicense(is);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*public static void doc2pdf(String wordPath, String pdfPath) {
        if (!getLicense()) {          // 验证License 若不验证则转化出的pdf文档会有水印产生
            return;
        }
        try {
            long old = System.currentTimeMillis();
            File file = new File(pdfPath);  //新建一个pdf文档
            FileOutputStream os = new FileOutputStream(file);
            Document doc = new Document(wordPath);                    //Address是将要被转化的word文档
            doc.save(os, com.aspose.words.SaveFormat.PDF);//全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
            long now = System.currentTimeMillis();
            os.close();
            System.out.println("共耗时：" + ((now - old) / 1000.0) + "秒");  //转化用时
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * @param excelPath
     * @param pdfPath
     */
    public static void excel2pdf(String excelPath, String pdfPath) {
        if (!getLicense()) {          // 验证License 若不验证则转化出的pdf文档会有水印产生
            return;
        }
        try {
            long old = System.currentTimeMillis();
            Workbook wb = new Workbook(excelPath);// 原始excel路径
            FileOutputStream fileOS = new FileOutputStream(new File(pdfPath));
            wb.save(fileOS, com.aspose.cells.SaveFormat.PDF);
            fileOS.close();
            long now = System.currentTimeMillis();
            System.out.println("共耗时：" + ((now - old) / 1000.0) + "秒");  //转化用时
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        excel2pdf("/Users/xishang/temp/bank1.xlsx", "/Users/xishang/temp/bank.pdf");
    }

}
