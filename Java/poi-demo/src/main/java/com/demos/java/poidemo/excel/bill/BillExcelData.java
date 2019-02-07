package com.demos.java.poidemo.excel.bill;

import com.demos.java.poidemo.excel.ExcelData;
import com.demos.java.poidemo.excel.ExcelElement;
import com.demos.java.poidemo.excel.PdfUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/7
 * 记账凭证
 */
public class BillExcelData extends ExcelData {

    private List<Bill> billList;

    public static final String MONEY_FORMAT = "%016.2f";

    public static BigDecimal getRandom() {
        double val = Math.random() * 1000000;
        return new BigDecimal(val);
    }

    public static void main(String[] args) throws Exception {
        List<Bill> list = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            String summary = "摘要" + i;
            String subject = "科目" + i;
            list.add(new Bill(summary, subject, getRandom(), getRandom()));

        }
        ExcelData excelData = new BillExcelData(list);
        List<String> files = excelData.generateFiles();
        System.out.println("final pdf: " + PdfUtil.excel2Pdf(files));
    }

    public BillExcelData(List<Bill> billList) {
        super("/Users/xishang/temp/bill.xlsx", 16);
        this.billList = billList;
    }

    @Override
    public List<ExcelElement> prepareStaticData() {
        List<ExcelElement> list = new ArrayList<>();
        // 静态数据起始行索引
        int index = 7; // 从0开始
        for (Bill bill : billList) {
            int colIndex = 0;
            list.add(new ExcelElement(index, colIndex, bill.getSummary()));
            colIndex += 2;
            list.add(new ExcelElement(index, colIndex, bill.getSubject()));
            colIndex += 5;
            // 借方金额
            BigDecimal borrowAmount = bill.getBorrowAmount();
            String borrowStr = String.format(MONEY_FORMAT, borrowAmount.doubleValue()).replace(".", "");
            for (char c : borrowStr.toCharArray()) {
                list.add(new ExcelElement(index, colIndex++, c));
            }
            // 贷方金额
            BigDecimal lendAmount = bill.getLendAmount();
            String lendStr = String.format(MONEY_FORMAT, lendAmount.doubleValue()).replace(".", "");
            for (char c : lendStr.toCharArray()) {
                list.add(new ExcelElement(index, colIndex++, c));
            }
            index++;
        }
        return list;
    }

    @Override
    public List<Object> prepareDynamicData() {
        List list = new ArrayList<>();
        IntStream.range(0, 200).forEach(index -> {
            list.add(new Borrower("name" + index, index + 20, UUID.randomUUID().toString()));
        });
        return list;
    }

    @Override
    public Map<Integer, CellStyle> getCellStyleMap(XSSFSheet sheet) {
        // 以第8行为模板
        XSSFRow row = sheet.getRow(7);
        XSSFCell cell = row.getCell(0);
        Map<Integer, CellStyle> styleMap = new HashMap<>();
        styleMap.put(0, cell.getCellStyle());
        return styleMap;
    }

}
