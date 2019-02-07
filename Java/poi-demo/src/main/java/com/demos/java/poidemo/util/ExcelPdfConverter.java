package com.demos.java.poidemo.util;

import com.demos.java.poidemo.util.excel.Excel2Pdf;
import com.demos.java.poidemo.util.excel.ExcelObject;
import com.itextpdf.text.pdf.PdfPCell;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/6
 */
public class ExcelPdfConverter {

    public static String convertExcelToPdf(String excelFilePath) throws Exception {
        InputStream is = new FileInputStream(excelFilePath);
        Workbook wb = WorkbookFactory.create(is);
        try {
            // 获取第一个工作薄
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                return null;
            }
            // excel行数
            int rowCount = sheet.getLastRowNum();
            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i + 1);
                if (row == null)
                    continue;
                int cellCount = row.getLastCellNum();
                for (int j = 0; j < cellCount; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null)
                        continue;
                    cell.setCellValue("填充值: " + i + "-" + j);
                    // PDF cell
//                    PdfPCell pdfpCell = new PdfPCell();
//                    pdfpCell.setBackgroundColor(new BaseColor(getBackgroundColorByExcel(cell.getCellStyle())));
//                    pdfpCell.setColspan(colspan);
//                    pdfpCell.setRowspan(rowspan);
//                    pdfpCell.setVerticalAlignment(getVAlignByExcel(cell.getCellStyle().getVerticalAlignment()));
//                    pdfpCell.setHorizontalAlignment(getHAlignByExcel(cell.getCellStyle().getAlignment()));
//                    pdfpCell.setPhrase(getPhrase(cell));
//                    pdfpCell.setFixedHeight(this.getPixelHeight(row.getHeightInPoints()));
//                    addBorderByExcel(pdfpCell, cell.getCellStyle());
//                    addImageByPOICell(pdfpCell , cell , cw);
                }

            }


            // 保存修改
            FileOutputStream fos=new FileOutputStream(excelFilePath);
            wb.write(fos);
            fos.close();
        } finally {
            if (wb != null) {
                wb.close();
            }
            if (null != is) {
                is.close();
            }
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
        convertExcelToPdf("/Users/xishang/temp/list1.xlsx");
    }

    public static void main1(String[] args) throws Exception {
        FileInputStream fis1 = new FileInputStream(new File("/Users/xishang/temp/test.xlsx"));
        FileInputStream fis2 = new FileInputStream(new File("/Users/xishang/temp/test1.xlsx"));
        //
        FileOutputStream fos = new FileOutputStream(new File("/Users/xishang/temp/testpdf.pdf"));
        //
        List<ExcelObject> objects = new ArrayList<>();
        objects.add(new ExcelObject("1.MAD 5-3-05-Octavia NF-20131025.xls", fis1));
        objects.add(new ExcelObject("2.MAD 6-1-47-Octavia NF-20131025.xls", fis2));
        //
        Excel2Pdf pdf = new Excel2Pdf(objects, fos);
        pdf.convert();
    }

}
